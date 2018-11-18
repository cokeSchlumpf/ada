package ada.cli;

import ada.cli.restclient.RestClient;
import ada.cli.restclient.RestClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public RestClient getRestClient() {
        return RestClientImpl.apply("http://localhost:8080");
    }

}
