"""Proxy service for fetching weather data from upstream API."""

import logging
from typing import Any

import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

from proxy.exceptions import NotFoundError, UpstreamServerError, InvalidRequestError
from proxy.cache import CacheClient

logger = logging.getLogger(__name__)


class ProxyService:
    """Service for proxying weather data requests to Open-Meteo API."""

    def __init__(
        self,
        upstream_base_url: str,
        cache_client: CacheClient | None = None,
        cache_ttl: int = 60,
    ):
        """
        Initialize the ProxyService.

        Args:
            upstream_base_url: Base URL for the upstream weather API.
            cache_client: Optional Redis cache client.
            cache_ttl: Cache time-to-live in seconds.
        """
        self.upstream_base_url = upstream_base_url
        self.cache_client = cache_client
        self.cache_ttl = cache_ttl

        # Configure retry strategy for HTTP requests
        retry_strategy = Retry(
            total=3,
            backoff_factor=1,
            allowed_methods=["GET"],
            status_forcelist=[500, 502, 503, 504],
        )
        adapter = HTTPAdapter(max_retries=retry_strategy)
        self.session = requests.Session()
        self.session.mount("http://", adapter)
        self.session.mount("https://", adapter)

    def load_weather_data(
        self,
        longitude: float,
        latitude: float,
        source_params: dict[str, str],
    ) -> dict[str, Any] | None:
        """
        Load weather data from upstream API with caching support.

        Args:
            longitude: Longitude coordinate.
            latitude: Latitude coordinate.
            source_params: Additional query parameters for the upstream API.

        Returns:
            Weather data dictionary or None if not found.

        Raises:
            NotFoundError: If no data is returned from upstream.
            UpstreamServerError: If upstream returns a server error.
            InvalidRequestError: If upstream rejects the request.
        """
        cache_key = f"{longitude}:{latitude}"

        # Try to get from cache first
        if self.cache_client:
            cached_data = self.cache_client.get(cache_key)
            if cached_data:
                logger.info(f"Cache hit for key: {cache_key}")
                return cached_data

        try:
            response = self._call_upstream(longitude, latitude, source_params)

            if response is None:
                raise NotFoundError()

            # Store in cache
            if self.cache_client:
                self.cache_client.set(cache_key, response, self.cache_ttl)
                logger.info(f"Cached data for key: {cache_key}")

            logger.info(f"Loaded weather data for params: {source_params}")
            return response

        except requests.exceptions.HTTPError as e:
            status_code = e.response.status_code
            if 400 <= status_code < 500:
                raise InvalidRequestError(
                    f"Upstream rejected the request: {status_code}"
                )
            elif status_code >= 500:
                raise UpstreamServerError()
            raise
        except requests.exceptions.RequestException as e:
            logger.warning(
                f"Network error reaching upstream for params {source_params}: {e}"
            )
            raise

    def _call_upstream(
        self,
        longitude: float,
        latitude: float,
        params: dict[str, str],
    ) -> dict[str, Any] | None:
        """
        Make the actual call to the upstream API.

        Args:
            longitude: Longitude coordinate.
            latitude: Latitude coordinate.
            params: Query parameters.

        Returns:
            Parsed JSON response or None.
        """
        url = f"{self.upstream_base_url}/v1/forecast"

        # Merge provided params with coordinates
        query_params = {
            "latitude": latitude,
            "longitude": longitude,
            **params,
        }

        response = self.session.get(url, params=query_params, timeout=10)
        response.raise_for_status()

        if response.status_code == 204 or not response.content:
            return None

        return response.json()
