package pt.isep.aisafe.aircraft.shared.forwarding;

public final class ForwardingHeaders {
    public static final String FORWARDED_BY = "X-Forwarded-By";
    public static final String FORWARDED_FROM = "X-Forwarded-From";
    public static final String SERVICE_KEY = "X-Service-Key";
    public static final String INTERNAL_CALL_ATTRIBUTE = "isInternalCall";

    private ForwardingHeaders() {}
}
