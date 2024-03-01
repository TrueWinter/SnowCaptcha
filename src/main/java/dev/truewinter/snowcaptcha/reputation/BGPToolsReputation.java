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

            // Format: 2a0e:8f02:2151::/48 211869
            List<String> table = new ArrayList<>();
            // Format: AS211869
            List<String> badASNs = new ArrayList<>();
            List<String> badPrefixes = new ArrayList<>();

            try {
                String h = get("https://bgp.tools/table.jsonl");
                // Format: {"CIDR":"2a0e:8f02:2151::/48","ASN":211869,"Hits":2235}
                String[] lines = h.split("\n");

                for (String line : lines) {
                    JsonNode jsonNode = new ObjectMapper().readTree(line);
                    String prefix = jsonNode.get("CIDR").textValue();
                    int asn = jsonNode.get("ASN").intValue();
                    int hits = jsonNode.get("Hits").asInt();

                    if (hits >= 100) {
                        table.add(prefix + " " + asn);
                    }
                }

                for (String category : categories) {
                    String c = get("https://bgp.tools/tags/" + category + ".txt");
                    badASNs.addAll(Arrays.asList(c.split("\n")));
                }

                badASNs.addAll(bgpToolsConfig.getBlacklist());
                badASNs.removeAll(bgpToolsConfig.getWhitelist());

                for (String s : table) {
                    String[] parts = s.split(" ");
                    String prefix = parts[0];
                    String asn = parts[1];

                    if (badASNs.contains("AS" + asn)) {
                        badPrefixes.add(prefix);
                    }
                }

                redisBGPToolsReputationDatabase.setData(badPrefixes);
            } catch (IOException e) {
                SnowCaptcha.getLogger().warn("Failed to update BGP.tools data", e);
            }
        }

        private String get(String url) throws IOException {
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
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
                content.append("\n");
            }
            in.close();
            con.disconnect();

            return content.toString();
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
