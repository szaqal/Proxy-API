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



TODO:
---
* OpenAPI
* OpenTelemetry
* Retrues


