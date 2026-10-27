package pt.isep.aisafe.aircraft.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "aisafe")
public class AisafeProperties {

    private String instanceName = "aircraft-1";
    private String serviceKey = "secret-service-key";
    private String jwtSecret = "default-secret-key-that-is-at-least-32-bytes-long!";
    private List<PeerConfig> peers = new ArrayList<>();

    public static class PeerConfig {
        private String name;
        private String url;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    public String getInstanceName() { return instanceName; }
    public void setInstanceName(String instanceName) { this.instanceName = instanceName; }

    public String getServiceKey() { return serviceKey; }
    public void setServiceKey(String serviceKey) { this.serviceKey = serviceKey; }

    public String getJwtSecret() { return jwtSecret; }
    public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }

    public List<PeerConfig> getPeers() { return peers; }
    public void setPeers(List<PeerConfig> peers) { this.peers = peers; }
}
