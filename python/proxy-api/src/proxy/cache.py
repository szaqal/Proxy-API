"""Redis cache client for weather data caching."""

import json
import logging
from typing import Any

import redis

logger = logging.getLogger(__name__)


class CacheClient:
    """Redis cache client for storing weather data."""

    def __init__(self, host: str = "localhost", port: int = 6379, db: int = 0):
        """
        Initialize the Redis cache client.

        Args:
            host: Redis host address.
            port: Redis port number.
            db: Redis database number.
        """
        self.client = redis.Redis(
            host=host,
            port=port,
            db=db,
            decode_responses=True,
        )

    def get(self, key: str) -> dict[str, Any] | None:
        """
        Get a value from cache.

        Args:
            key: Cache key.

        Returns:
            Cached value as dictionary, or None if not found.
        """
        try:
            value = self.client.get(key)
            if value:
                return json.loads(value)
            return None
        except redis.RedisError as e:
            logger.warning(f"Redis get error for key {key}: {e}")
            return None

    def set(self, key: str, value: dict[str, Any], ttl: int = 60) -> bool:
        """
        Set a value in cache with TTL.

        Args:
            key: Cache key.
            value: Value to store (will be JSON serialized).
            ttl: Time-to-live in seconds.

        Returns:
            True if successful, False otherwise.
        """
        try:
            serialized = json.dumps(value)
            return self.client.setex(key, ttl, serialized)
        except redis.RedisError as e:
            logger.warning(f"Redis set error for key {key}: {e}")
            return False

    def delete(self, key: str) -> bool:
        """
        Delete a key from cache.

        Args:
            key: Cache key.

        Returns:
            True if successful, False otherwise.
        """
        try:
            return bool(self.client.delete(key))
        except redis.RedisError as e:
            logger.warning(f"Redis delete error for key {key}: {e}")
            return False

    def ping(self) -> bool:
        """
        Check if Redis is available.

        Returns:
            True if Redis is reachable, False otherwise.
        """
        try:
            return self.client.ping()
        except redis.RedisError:
            return False
