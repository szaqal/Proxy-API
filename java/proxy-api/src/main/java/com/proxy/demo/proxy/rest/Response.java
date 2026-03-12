package com.proxy.demo.proxy.rest;

import java.time.Instant;

public record Response(String source, Instant instant, Current current) {}

record Current(double temperatureC, double windSpeedKmh) {}
