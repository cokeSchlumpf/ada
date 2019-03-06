package ada.cli.restclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.xml.internal.ws.api.server.HttpEndpoint;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@AllArgsConstructor(staticName = "apply")
public class WebClientFactory {

    private final ObjectMapper om;

    public WebClient create(MediaType accept, MediaType contentType) {
        WebClient.Builder builder = WebClient
            .builder()
            .exchangeStrategies(exchangeStrategies())
            .defaultHeader(HttpHeaders.ACCEPT, accept.toString());

        if (contentType != null) {
            builder.defaultHeader(HttpHeaders.CONTENT_TYPE, contentType.toString());
        }

        return builder.build();
    }

    public WebClient create(MediaType accept) {
        return create(accept, null);
    }

    public WebClient create() {
        return create(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
    }

    private ExchangeStrategies exchangeStrategies() {
        Jackson2JsonDecoder decoder = new Jackson2JsonDecoder(om, APPLICATION_JSON);
        Jackson2JsonEncoder encoder = new Jackson2JsonEncoder(om, APPLICATION_JSON);

        return ExchangeStrategies
            .builder()
            .codecs(config -> {
                config
                    .defaultCodecs()
                    .jackson2JsonEncoder(encoder);
                config
                    .defaultCodecs()
                    .jackson2JsonDecoder(decoder);
            })
            .build();
    }

}
