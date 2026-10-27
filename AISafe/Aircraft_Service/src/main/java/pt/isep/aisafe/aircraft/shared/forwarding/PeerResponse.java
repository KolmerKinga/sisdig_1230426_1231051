package pt.isep.aisafe.aircraft.shared.forwarding;

public record PeerResponse<T>(
        String peerName,
        boolean success,
        T data,
        String errorMessage
) {
    public static <T> PeerResponse<T> success(String peerName, T data) {
        return new PeerResponse<>(peerName, true, data, null);
    }

    public static <T> PeerResponse<T> failure(String peerName, String message) {
        return new PeerResponse<>(peerName, false, null, message);
    }
}
