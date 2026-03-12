# Proxy-API

![Maven Build](https://github.com/szaqal/Proxy-API/actions/workflows/maven.yml/badge.svg)
![Checkov](https://img.shields.io/github/actions/workflow/status/szaqal/Proxy-API/maven.yml?job=checkov&label=Checkov&logo=checkmarx&logoColor=white&color=brightgreen)
![OWASP](https://img.shields.io/github/actions/workflow/status/szaqal/Proxy-API/maven.yml?job=owasp&label=OWASP&logo=owasp&logoColor=white&color=purple)

Assumptions:
---

* No security requiremensts hence no AuthN or AuthZ implemented

* Since it the API is specified and it's mean to be proxy all query parameters will be passed AS-IS to origin service. Since the only thing we know is that longitude and latitude are required and validated. Since there the expeced output is provided on the other hand it feels a bit odd since it modifies origin service output. So it's a proxy to some externd and to some not :).

What could be considered (ADRs):
---

* Spring Boot there werent any constraints there alternative approach may be, selected mainy due to all building blocks in place. 
  * No frameworks simple servlet with manuall connection handling - smaller resource footprint at a cost of maintanability and work effort (obervability etc..)
  * Some none blocking server like Netty some as above
  * Some other DI frameworks would probably be dependent on collective knowledge.



* NOT adding (NOW) Circuit breaker now since this is external system so we may not be that much concerned, unless API rate limiting is implemented on the other side.

* NOT adding (NOW) Reactive Webclient instead of RestClient if load specific justifies it. Without such information I value more maintainability, but could be swtiched if needed.

* NOT adding (NOW) Cache hierarchy Local -> Redis to increas a bit resilience if Redis get unavailable. (Skip for now)

* NOT adding (NOW) [Spring config](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/) server and automatic bean reload. 

* NOT adding (NOW) Externalize client config socker/read timeout to env variable (configMap)

* NOT adding (NOW) Externalize TTL for cache as config

* NOT adding (NOW) any gitops ArgoCD, Flux etc...

[Swagger](http://localhost:8080/swagger-ui/index.html)

Example
---

```bash
make ab-bench
ab -n 100 -c 100 "http://localhost:8080/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m"
This is ApacheBench, Version 2.3 <$Revision: 1903618 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking localhost (be patient).....done


Server Software:        
Server Hostname:        localhost
Server Port:            8080

Document Path:          /forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m
Document Length:        117 bytes

Concurrency Level:      100
Time taken for tests:   0.025 seconds
Complete requests:      100
Failed requests:        0
Total transferred:      22200 bytes
HTML transferred:       11700 bytes
Requests per second:    3953.82 [#/sec] (mean)
Time per request:       25.292 [ms] (mean)
Time per request:       0.253 [ms] (mean, across all concurrent requests)
Transfer rate:          857.18 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    3   0.7      3       4
Processing:     7   14   1.8     14      17
Waiting:        3   14   2.0     14      17
Total:          7   18   1.9     17      20

Percentage of the requests served within a certain time (ms)
  50%     17
  66%     19
  75%     19
  80%     19
  90%     20
  95%     20
  98%     20
  99%     20
 100%     20 (longest request)
```

TODO:
---
* OpenTelemetry
* Retrues


