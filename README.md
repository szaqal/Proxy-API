# Proxy-API

![Maven Build](https://github.com/szaqal/Proxy-API/actions/workflows/maven.yml/badge.svg)
![Checkov](https://img.shields.io/github/actions/workflow/status/szaqal/Proxy-API/maven.yml?job=checkov&label=Checkov&logo=checkmarx&logoColor=white&color=brightgreen)
![OWASP](https://img.shields.io/github/actions/workflow/status/szaqal/Proxy-API/maven.yml?job=owasp&label=OWASP&logo=owasp&logoColor=white&color=purple)

Assumptions:
---

* No security requiremensts hence no AuthN or AuthZ implemented

What could be considered (ADRs):
---

* Circuit breaker however this is external system so we may not be that much concerned, unless API rate limiting is implemented on the other side.

* Reactive Webclient instead of RestClient if load specific justifies it. Without such information I value more maintainability.

* Cache hierarchy Local -> Redis to increas a bit resilience if Redis get unavailable. (Skip for now)



TODO:
---
* OpenAPI
* OpenTelemetry
* Retrues


