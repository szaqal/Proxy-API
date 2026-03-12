"""Custom exceptions for Proxy API."""


class ProxyError(Exception):
    """Base exception for Proxy API errors."""

    pass


class NotFoundError(ProxyError):
    """Exception raised when resource is not found."""

    pass


class InvalidRequestError(ProxyError):
    """Exception raised for invalid client requests."""

    pass


class UpstreamServerError(ProxyError):
    """Exception raised when upstream server returns an error."""

    pass
