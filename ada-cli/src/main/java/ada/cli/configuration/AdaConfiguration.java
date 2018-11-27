package ada.cli.configuration;

import ada.cli.restclient.RestClient;
import ada.cli.restclient.RestClientImpl;
import ada.cli.users.MutualTLSAuthenticatedUser;
import ada.client.output.Output;
import ada.client.output.PrintStreamOutput;
import ada.web.api.resources.about.model.User;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This class implements factories to instantiate dependent classes from Ada (and libraries around, e.g. Akka).
 */
@Configuration
public class AdaConfiguration {

    @Bean
    public ActorSystem getActorSystem() {
        return ActorSystem.create("ada");
    }

    @Bean
    public Materializer getMaterializer(ActorSystem system) {
        return ActorMaterializer.create(system);
    }

    @Bean
    public Output getOutput() {
        return PrintStreamOutput.apply(System.out);
    }

    @Bean
    public RestClient getRestClient(ApplicationConfiguration config, ObjectMapper om) {
        return RestClientImpl.apply(config.getServer().getBaseUrl(), om);
    }

    @Bean
    public User getUser() {
        return MutualTLSAuthenticatedUser.apply();
    }

}
