package ada.cli;

import ada.cli.restclient.RestClient;
import ada.cli.restclient.RestClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    private final String baseUrl;

    public ApplicationConfiguration(@Value("${ada.server.baseUrl}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Bean
    public RestClient getRestClient() {
        return RestClientImpl.apply(baseUrl);
    }

}
