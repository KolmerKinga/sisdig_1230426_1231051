package pt.isep.aisafe.airport.shared.forwarding;

import org.springframework.http.MediaType;

/**
 * Raw answer of a peer replica (status, content type and body bytes), so it can be
 * passed through to the client unchanged.
 */
public record PeerResponse(String peer, int status, MediaType contentType, byte[] body) {

    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }

    public boolean isNotFound() {
        return status == 404;
    }
}
