# SweetAlerts tests
Reference at <https://sweetalert2.github.io/>.

### Get started
- Start with a `npm install` (to do once)
    - If behind a firewall:
    ```
    PROXY_HOST=www-proxy.us.oracle.com
    PROXY_PORT=80
    npm config set proxy http://${PROXY_HOST}:${PROXY_PORT}
    npm config set http-proxy http://${PROXY_HOST}:${PROXY_PORT}
    npm config set https-proxy http://${PROXY_HOST}:${PROXY_PORT}
    ```
    - To unset:
    ```
    npm config rm proxy
    npm config rm http-proxy
    npm config rm https-proxy
    ```
- Then `npm start`
- And from a browser, reach <httl://localhost:8080/index.html>.

---

