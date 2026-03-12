"""Health check endpoints for Kubernetes probes."""

import logging

logger = logging.getLogger(__name__)


def liveness() -> bool:
    """
    Liveness probe - is the application running?

    Returns:
        True if the application is alive.
    """
    return True


def readiness() -> bool:
    """
    Readiness probe - is the application ready to serve traffic?

    Returns:
        True if the application is ready.
    """
    from flask import current_app

    # Check if Redis is available if caching is enabled
    if hasattr(current_app, "proxy_service") and current_app.proxy_service.cache_client:
        try:
            if not current_app.proxy_service.cache_client.ping():
                logger.warning("Redis is not available")
                return False
        except Exception as e:
            logger.warning(f"Error checking Redis readiness: {e}")
            return False

    return True
