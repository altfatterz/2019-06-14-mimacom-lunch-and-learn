Spring Cloud Gateway




`RequestRateLimiter` feature

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
