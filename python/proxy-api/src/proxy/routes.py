"""Forecast API routes."""

from datetime import datetime
from flask import Blueprint, request, jsonify

from proxy.exceptions import (
    InvalidRequestError,
    NotFoundError,
    UpstreamServerError,
)
from proxy.services.proxy_service import ProxyService

forecast_bp = Blueprint("forecast", __name__, url_prefix="/forecast")


def get_proxy_service() -> ProxyService:
    """Get proxy service instance (dependency injection via Flask app context)."""
    from flask import current_app

    return current_app.proxy_service


@forecast_bp.route("", methods=["GET"])
def forecast():
    """
    Get weather forecast for given coordinates.

    Query Parameters:
        latitude: Latitude of the location (required)
        longitude: Longitude of the location (required)

    Returns:
        JSON response with weather data including temperature and wind speed.
    """
    params = request.args.to_dict()

    # Validate latitude
    latitude = _parse_coordinate(params.get("latitude"), "latitude")
    longitude = _parse_coordinate(params.get("longitude"), "longitude")

    service = get_proxy_service()

    try:
        result = service.load_weather_data(longitude, latitude, params)
    except NotFoundError:
        return jsonify({"error": "Not found"}), 404
    except UpstreamServerError:
        return jsonify({"error": "Upstream server error"}), 502
    except InvalidRequestError as e:
        return jsonify({"error": str(e)}), 400

    if result is None or result.get("current") is None:
        return jsonify({"error": "Not found"}), 404

    current = result["current"]
    response = {
        "source": "open-meteo",
        "instant": datetime.utcnow().isoformat(),
        "current": {
            "temperatureC": current.get("temperature_2m"),
            "windSpeedKmh": current.get("wind_speed_10m"),
        },
    }

    return jsonify(response), 200


def _parse_coordinate(value: str | None, name: str) -> float:
    """Parse and validate a coordinate value."""
    if value is None:
        raise InvalidRequestError(f"Missing {name}")

    try:
        return float(value)
    except ValueError:
        raise InvalidRequestError(f"Invalid {name}: must be a valid number")
