package pt.isep.aisafe.airport.shared.forwarding;

import jakarta.servlet.http.HttpServletRequest;

/**
 * HTTP headers used for distribution, security and traceability.
 */
public final class ForwardingHeaders {

    /** Set by a replica when it forwards a request to a peer. A forwarded request is never forwarded again. */
    public static final String FORWARDED_BY = "X-Forwarded-By";

    /** Shared secret proving that the caller is an AISafe service (required on internal calls). */
    public static final String SERVICE_KEY = "X-Service-Key";

    /** Correlation id propagated across replicas and services for tracing in the logs. */
    public static final String CORRELATION_ID = "X-Correlation-Id";

    /** "true" when at least one peer could not be contacted, so the answer may be incomplete. */
    public static final String PARTIAL_RESULT = "X-Partial-Result";

    /** Replica that handled the request (set on every response). */
    public static final String SERVED_BY = "X-Served-By";

    /** "local" or the URL of the peer that held the data. */
    public static final String DATA_SOURCE = "X-Data-Source";

    /** Total number of items of an aggregated collection, before pagination. */
    public static final String TOTAL_COUNT = "X-Total-Count";

    /** Request attribute holding the correlation id. */
    public static final String CORRELATION_ATTRIBUTE = "aisafe.correlationId";

    /** Request attribute set to true when the service key was validated. */
    public static final String INTERNAL_CALL_ATTRIBUTE = "aisafe.internalCall";

    private ForwardingHeaders() {
    }

    public static boolean isForwarded(HttpServletRequest request) {
        String value = request.getHeader(FORWARDED_BY);
        return value != null && !value.isBlank();
    }

    /** Path plus query string exactly as received, so the peer gets the same request. */
    public static String pathAndQuery(HttpServletRequest request) {
        String query = request.getQueryString();
        return query == null ? request.getRequestURI() : request.getRequestURI() + "?" + query;
    }
}
