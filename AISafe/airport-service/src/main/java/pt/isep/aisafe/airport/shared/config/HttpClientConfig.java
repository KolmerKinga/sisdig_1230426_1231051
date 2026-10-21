package pt.isep.aisafe.airport.shared.config;

import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HTTP client used to talk to peer replicas over HTTPS.
 * Uses the same "aisafe" SSL bundle as the server, so it trusts the AISafe certificate.
 */
@Configuration
public class HttpClientConfig {

    public static final String SSL_BUNDLE = "aisafe";

    @Bean
    public RestClient peerRestClient(RestClient.Builder builder, SslBundles sslBundles, AisafeProperties properties) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(properties.forwarding().connectTimeout())
                .withReadTimeout(properties.forwarding().readTimeout())
                .withSslBundle(sslBundles.getBundle(SSL_BUNDLE));
        return builder
                .requestFactory(ClientHttpRequestFactories.get(settings))
                .build();
    }

    /** Thread pool used to query peers in parallel when aggregating collections. */
    @Bean(destroyMethod = "shutdown")
    public ExecutorService peerExecutor() {
        return Executors.newFixedThreadPool(4);
    }
}
