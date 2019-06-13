Spring Cloud Gateway


Routes available at:

```bash
http://localhost:8080/actuator/gateway/routes
```

Exposed metrics: (`spring.cloud.gateway.metrics.enabled` is set to `true` by default)

```bash
http://localhost:8080/actuator/metrics/gateway.requests
```

These metrics are then available to be scraped from `/actuator/metrics/gateway.requests` and can be easily integated with 
`Prometheus` to create a `Grafana` dashboard.

##### Prometheus

The `management.endpoint.prometheus.enabled` is set to `true` by default.
You need to include `prometheus` to the `management.endpoints.web.exposure.include` 

Then the your application will have the followin endpoint: `http://localhost:8080/actuator/prometheus`.
This will be scraped by [Prometheus](https://prometheus.io/) to get the information from your application. 

##### Grafana

Credentials: `admin`/`password`


Start `Grafana` and `Prometheus` together with `docker-compose` from the `new-way` folder.

```bash
$ docker-compose up
```

Verify that the targets are visible to `Prometheus` at `http://localhost:9090/targets`

Import this [JVM Micrometer Dashboard](https://grafana.com/dashboards/4701) into Grafana.






##### `RequestRateLimiter` feature of Spring Cloud Gateway 

Start Redis

```bash
$ docker run --name request-rate-limiter -p 6379:6379 -d redis
```

Demo configuration:

```yaml
filters:
- name: RequestRateLimiter
  args:
  key-resolver: '#{@userKeyResolver}'
  redis-rate-limiter.replenishRate: 2
  redis-rate-limiter.burstCapacity: 2
```

Test it with `ab` tool

```bash
$ ab -v 2 -c 1 -n 3 localhost:8080/customers/1

---
LOG: header received:
HTTP/1.1 200 OK
transfer-encoding: chunked
X-RateLimit-Remaining: 1
X-RateLimit-Burst-Capacity: 2
X-RateLimit-Replenish-Rate: 2
X-Some-Header: foo
Content-Type: application/json;charset=UTF-8
Date: Thu, 13 Jun 2019 08:49:57 GMT
connection: close


LOG: header received:
HTTP/1.1 200 OK
transfer-encoding: chunked
X-RateLimit-Remaining: 0
X-RateLimit-Burst-Capacity: 2
X-RateLimit-Replenish-Rate: 2
X-Some-Header: foo
Content-Type: application/json;charset=UTF-8
Date: Thu, 13 Jun 2019 08:49:57 GMT
connection: close


LOG: header received:
HTTP/1.0 429 Too Many Requests
X-RateLimit-Remaining: 0
X-RateLimit-Burst-Capacity: 2
X-RateLimit-Replenish-Rate: 2
content-length: 0
```
