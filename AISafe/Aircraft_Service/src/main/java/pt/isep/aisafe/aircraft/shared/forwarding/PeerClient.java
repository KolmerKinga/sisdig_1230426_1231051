package pt.isep.aisafe.aircraft.shared.forwarding;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import pt.isep.aisafe.aircraft.shared.config.AisafeProperties;

import java.util.ArrayList;
import java.util.List;

@Component
public class PeerClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final AisafeProperties aisafeProperties;

    public PeerClient(AisafeProperties aisafeProperties) {
        this.aisafeProperties = aisafeProperties;
    }

    public <T> List<T> fetchFromOtherPeers(String path, Class<T> responseType) {
        List<T> results = new ArrayList<>();
        if (aisafeProperties.getPeers() == null || aisafeProperties.getPeers().isEmpty()) {
            return results;
        }

        for (AisafeProperties.PeerConfig peer : aisafeProperties.getPeers()) {
            try {
                String url = peer.getUrl() + path;
                HttpHeaders headers = new HttpHeaders();
                headers.set(ForwardingHeaders.FORWARDED_FROM, aisafeProperties.getInstanceName());
                headers.set(ForwardingHeaders.SERVICE_KEY, aisafeProperties.getServiceKey());

                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<T> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        responseType
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    results.add(response.getBody());
                }
            } catch (Exception ignored) {
                // Ignorar réplicas offline ou inacessíveis durante os testes
            }
        }
        return results;
    }
}
