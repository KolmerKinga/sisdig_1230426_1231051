package pt.isep.aisafe.aircraft.shared.forwarding;

import org.springframework.stereotype.Component;
import pt.isep.aisafe.aircraft.shared.config.AisafeProperties;

import java.util.List;
import java.util.Optional;

@Component
public class PeerLookup {

    private final AisafeProperties properties;

    public PeerLookup(AisafeProperties properties) {
        this.properties = properties;
    }

    public String getOwnInstanceName() {
        return properties.getInstanceName();
    }

    public List<AisafeProperties.PeerConfig> getOtherPeers() {
        return properties.getPeers().stream()
                .filter(peer -> !peer.getName().equalsIgnoreCase(properties.getInstanceName()))
                .toList();
    }

    public Optional<AisafeProperties.PeerConfig> findPeerByName(String name) {
        return properties.getPeers().stream()
                .filter(peer -> peer.getName().equalsIgnoreCase(name))
                .findFirst();
    }
}
