package pt.isep.aisafe.aircraft.shared.forwarding;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import pt.isep.aisafe.aircraft.shared.config.AisafeProperties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class PeerClient {

    private final RestTemplate restTemplate;
    private final PeerLookup peerLookup;
    private final AisafeProperties properties;

    public PeerClient(RestTemplate restTemplate, PeerLookup peerLookup, AisafeProperties properties) {
        this.restTemplate = restTemplate;
        this.peerLookup = peerLookup;
        this.properties = properties;
    }

    public <T> PeerResponse<T> forwardGet(String peerName, String path, Class<T> responseType) {
        return peerLookup.findPeerByName(peerName)
                .map(peer -> executeGet(peer.getUrl() + path, peer.getName(), responseType))
                .orElseGet(() -> PeerResponse.failure(peerName, "Peer not configured"));
    }

    public <T> List<T> fetchFromOtherPeers(String path, Class<T[]> responseArrayType) {
        List<AisafeProperties.PeerConfig> otherPeers = peerLookup.getOtherPeers();
        List<T> aggregated = new java.util.ArrayList<>();

        for (AisafeProperties.PeerConfig peer : otherPeers) {
            PeerResponse<T[]> response = executeGet(peer.getUrl() + path, peer.getName(), responseArrayType);
            if (response.success() && response.data() != null) {
                aggregated.addAll(Arrays.asList(response.data()));
            }
        }
        return aggregated;
    }

    private <T> PeerResponse<T> executeGet(String url, String peerName, Class<T> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(ForwardingHeaders.FORWARDED_FROM, properties.getInstanceName());
            headers.set(ForwardingHeaders.SERVICE_KEY, properties.getServiceKey());
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
            return PeerResponse.success(peerName, response.getBody());
        } catch (Exception e) {
            return PeerResponse.failure(peerName, e.getMessage());
        }
    }
}
