package dev.truewinter.snowcaptcha.reputation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.truewinter.snowcaptcha.SnowCaptcha;
import dev.truewinter.snowcaptcha.config.BGPToolsConfig;
import dev.truewinter.snowcaptcha.database.RedisBGPToolsReputationDatabase;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Checks if the IP address belongs to an ASN hosting servers, VPNs, Tor infrastructure, or is an
 * event network, as categorised by BGP.tools.
 */
public class BGPToolsReputation implements ReputationSource {
    private final RedisBGPToolsReputationDatabase redisBGPToolsReputationDatabase = SnowCaptcha.getRedis()
            .getRedisBGPToolsReputationDatabase();
    private final Updater updater;

    public BGPToolsReputation() {
        this.updater = new Updater(redisBGPToolsReputationDatabase,
                SnowCaptcha.getReputationConfig().getBgpToolsConfig());
    }

    private static class Updater implements Runnable {
        private final List<String> categories = new ArrayList<>(){{
            add("vpsh");
            add("event");
            add("vpn");
            add("tor");
        }};
        private final RedisBGPToolsReputationDatabase redisBGPToolsReputationDatabase;
        private final BGPToolsConfig bgpToolsConfig;

        public Updater(RedisBGPToolsReputationDatabase bgpToolsReputationDatabase, BGPToolsConfig bgpToolsConfig) {
            this.redisBGPToolsReputationDatabase = bgpToolsReputationDatabase;
            this.bgpToolsConfig = bgpToolsConfig;
        }

        @Override
        public void run() {
            try {
                if (!redisBGPToolsReputationDatabase.isDataExpired() &&
                        redisBGPToolsReputationDatabase.getData().isPresent()) return;

                if (redisBGPToolsReputationDatabase.getData().isEmpty()) {
                    redisBGPToolsReputationDatabase.setData(new ArrayList<>());
                }
            } catch (JsonProcessingException e) {
                SnowCaptcha.getLogger().error("Failed to parse BGP.tools data from Redis", e);
                return;
            }

            SnowCaptcha.getLogger().info("Updating BGP.tools data");
            /*
                Immediately updating the expiry ensures that only one updater is running at a time.
                It also ensures that if a request to update the data failed, that we won't attempt
                to download the data every time we receive a request until it succeeds.
             */
            redisBGPToolsReputationDatabase.updateExpiry();

            List<String> badASNs = new ArrayList<>();
            try {
                for (String category : categories) {
                    String c = get("https://bgp.tools/tags/" + category + ".txt");
                    badASNs.addAll(Arrays.asList(c.split("\n")));
                }

                badASNs.addAll(bgpToolsConfig.getBlacklist());
                badASNs.removeAll(bgpToolsConfig.getWhitelist());

                List<String> badPrefixes = get("https://bgp.tools/table.jsonl", l -> {
                    try {
                        JsonNode jsonNode = new ObjectMapper().readTree(l);
                        String prefix = jsonNode.get("CIDR").textValue();
                        int asn = jsonNode.get("ASN").intValue();
                        int hits = jsonNode.get("Hits").asInt();

                        if (badASNs.contains("AS" + asn) && hits >= 100) {
                            return prefix;
                        }
                        // This function is essentially a loop, no need to have the catch block run every time
                    } catch (JsonProcessingException ignored) {}

                    return null;
                });

                redisBGPToolsReputationDatabase.setData(badPrefixes);
            } catch (IOException e) {
                SnowCaptcha.getLogger().warn("Failed to update BGP.tools data", e);
            }
        }

        private List<String> get(String url, Function<String, String> function) throws IOException {
            URL url1 = new URL(url);
            HttpURLConnection con = (HttpURLConnection) url1.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "SnowCaptcha - github@truewinter.dev");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setInstanceFollowRedirects(true);

            int status = con.getResponseCode();
            if (status >= 400) throw new IOException("Received error " + status + " from BGP.tools");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            List<String> output = new ArrayList<>();
            while ((inputLine = in.readLine()) != null) {
                String o = function.apply(inputLine);
                if (o != null) {
                    output.add(o);
                }
            }
            in.close();
            con.disconnect();

            return output;
        }

        private String get(String url) throws IOException {
            return String.join("\n", get(url, c -> c));
        }
    }

    @Override
    public boolean hasBadReputation(IPAddress ip) {
        Optional<List<String>> list;
        try {
            list = redisBGPToolsReputationDatabase.getData();
        } catch (JsonProcessingException e) {
            SnowCaptcha.getLogger().error("Failed to parse BGP.tools data from Redis", e);
            return false;
        }

        if (list.isEmpty() || redisBGPToolsReputationDatabase.isDataExpired()) {
            new Thread(updater).start();
            if (list.isEmpty()) return false;
        }

        String prefix = ip.setPrefixLength(ip.isIPv4() ? 24 : 48).toPrefixBlock().toString();

        for (String s : list.get()) {
            if (s.equals(prefix)) {
                return true;
            }

            IPAddress sPrefix = new IPAddressString(s).getAddress();
            if (sPrefix.contains(ip)) {
                return true;
            }
        }

        return false;
    }
}
