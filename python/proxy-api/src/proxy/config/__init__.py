"""Configuration classes for Proxy API."""

import os


class BaseConfig:
    """Base configuration."""

    # Flask
    DEBUG = False
    TESTING = False

    # Upstream API
    UPSTREAM_BASE_URL = os.getenv("UPSTREAM_BASE_URL", "https://api.open-meteo.com")

    # Redis
    REDIS_HOST = os.getenv("REDIS_HOST", "localhost")
    REDIS_PORT = int(os.getenv("REDIS_PORT", "6379"))
    REDIS_DB = int(os.getenv("REDIS_DB", "0"))
    CACHE_TTL = int(os.getenv("CACHE_TTL", "60"))

    # Logging
    LOG_LEVEL = os.getenv("LOG_LEVEL", "INFO")


class DevelopmentConfig(BaseConfig):
    """Development configuration."""

    DEBUG = True
    LOG_LEVEL = "DEBUG"


class ProductionConfig(BaseConfig):
    """Production configuration."""

    DEBUG = False
    LOG_LEVEL = "WARNING"


class TestingConfig(BaseConfig):
    """Testing configuration."""

    TESTING = True
    DEBUG = True
