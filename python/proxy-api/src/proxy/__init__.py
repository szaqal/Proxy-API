"""Flask application factory for Proxy API."""

import logging
import os

from flask import Flask, jsonify

from proxy.cache import CacheClient
from proxy.services.proxy_service import ProxyService

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
)
logger = logging.getLogger(__name__)


def create_app(config_name: str = "development") -> Flask:
    """Create and configure the Flask application."""
    app = Flask(__name__)

    # Load configuration
    config_class = f"proxy.config.{config_name.title()}Config"
    app.config.from_object(config_class)

    # Initialize Redis cache client
    cache_client = None
    redis_host = app.config.get("REDIS_HOST", "localhost")
    redis_port = app.config.get("REDIS_PORT", 6379)
    redis_db = app.config.get("REDIS_DB", 0)

    try:
        cache_client = CacheClient(host=redis_host, port=redis_port, db=redis_db)
        # Test Redis connection
        if cache_client.ping():
            logger.info(f"Redis connection established to {redis_host}:{redis_port}")
        else:
            logger.warning("Redis ping failed, caching disabled")
            cache_client = None
    except Exception as e:
        logger.warning(f"Failed to connect to Redis: {e}. Caching disabled.")
        cache_client = None

    # Initialize ProxyService
    proxy_service = ProxyService(
        upstream_base_url=app.config.get(
            "UPSTREAM_BASE_URL", "https://api.open-meteo.com"
        ),
        cache_client=cache_client,
        cache_ttl=app.config.get("CACHE_TTL", 60),
    )

    # Store service in app context
    app.proxy_service = proxy_service

    # Health check endpoints
    @app.route("/healthz", methods=["GET"])
    @app.route("/healthz/live", methods=["GET"])
    def liveness():
        """Liveness probe - is the application running?"""
        return jsonify({"status": "alive"}), 200

    @app.route("/healthz/ready", methods=["GET"])
    def readiness():
        """Readiness probe - is the application ready to serve traffic?"""
        # Check if Redis is available if caching is enabled
        if proxy_service.cache_client:
            try:
                if not proxy_service.cache_client.ping():
                    logger.warning("Redis is not available")
                    return jsonify(
                        {"status": "not ready", "reason": "redis unavailable"}
                    ), 503
            except Exception as e:
                logger.warning(f"Error checking Redis readiness: {e}")
                return jsonify({"status": "not ready", "reason": "redis error"}), 503

        return jsonify({"status": "ready"}), 200

    # Register blueprints
    from proxy.routes import forecast_bp

    app.register_blueprint(forecast_bp)

    logger.info(f"Application started with config: {config_name}")

    return app


# Application instance for Gunicorn
app = create_app(os.getenv("FLASK_ENV", "development"))
