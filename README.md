# SnowCaptcha

SnowCaptcha is designed to collect and process as little data as possible while still achieving its goal of stopping bots.

## How does it work?

When a user tries to complete the captcha, SnowCaptcha checks the reputation associated with the IP address. If the IP address has a bad reputation, the user will have to complete a visual challenge. A proof-of-work challenge is always issued.

Currently, the following reputation sources are used:
- Local: when a user completes the captcha, their IP prefix (/24 for IPv4, /48 for IPv6) is stored and will have a bad reputation for 10 minutes
- [BGP.tools](https://bgp.tools): networks that are categorized by BGP.tools as being a server host, VPN host, Tor services host, or an event network will have all of their prefixes added to SnowCaptcha's bad reputation list (this data is updated at most once every 2 hours)
- [Project Honey Pot](https://www.projecthoneypot.org): IPv4 only, requires an API key

The user has 5 minutes to solve all challenges, at which point the server will have 5 minutes to validate the token. Tokens can only be validated once.

## Requirements

- A web server with the ability to proxy requests to a backend and add headers (HAProxy is recommended for multi-instance deployments)
- SSL certificate
- 1GB memory minimum, 2GB recommended
  - It is recommended to allocate 256MB memory per SnowCaptcha instance, plus extra for Java off-heap, OS, and database use
- Redis
- MySQL
- Linux server (recommended)
- Java 17+

It has been tested on modern versions of Firefox, Chrome, and Safari.

## Usage

After starting SnowCaptcha for the first time, a configuration file will be created. Modify this as needed and then start SnowCaptcha again. The recommended JVM flags are `-XX:+UseG1GC -Xms384M -Xmx384M` (adjust memory as needed).

Configure your web server to terminate SSL and proxy requests to SnowCaptcha. You will need to set up a header that contains the user's IP address (ensuring that the header contains only the IP address, and that IPv4 addresses are not in the IPv4-mapped IPv6 address format), and configure this header in the SnowCaptcha config file.

Then, log in to the SnowCaptcha dashboard using the username `admin` and password `snowcaptcha`. It is recommended to change the password as soon as possible. Add a widget, taking note of the site key and secret key (which will only be shown once).

SnowCaptcha is designed to support multiple instances connected to the same Redis and MySQL database. It is recommended to run at least two instances for redundancy and to allow for zero-downtime updates. You should ensure that your load balancer only considers an instance down if it receives a non-200 response from the `/health` endpoint or the connection times out.

Implementing a widget on your website is easy:
```html
<form>
    <!-- Put this where you'd like the captcha to appear. data-mode can be "light" (default) or "dark" -->
    <div class="snowcaptcha" data-mode="dark"></div>
</form>

<script src="https://{snowcaptcha_instance}/build/captcha/captcha.js" data-sitekey="{ your_site_key }" async></script>
```

The script can also have the following optional attributes:
- `data-callback`: The name of the callback function that will be called when the captcha is loaded, solved, errors, reset, or expires
  - The callback data will be one of: `LOADED`, `SOLVED`, `RESET`, `ERROR`, `EXPIRED`
- `data-host`: The URL of the SnowCaptcha instance, if it is different to the script `src` domain

The script can also be lazy loaded, if necessary.

It is possible to load SnowCaptcha from a CDN. However, as the captcha files intentionally do not contain hashes in the names and have a no-cache header, this requires additional configuration to ensure that the files get cached on the CDN and need to be manually purged after updating SnowCaptcha.

SnowCaptcha will inject a hidden input element with the name `snowcaptcha` before the `snowcaptcha` div. This input field will only be populated after the user completes a captcha. To validate the captcha token server-side, send a POST request to `/validate-token` with the following data:
```json
{
    "token": "token from client-side input",
    "sitekey": "your site key",
    "secretkey": "your secret key"
}
```