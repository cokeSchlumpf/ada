package ada.spring.configuration;

import ada.web.controllers.AboutControllerConfiguration;
import akka.actor.ActorSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.Executors;

/**
 * @author Michael Wellner (michael.wellner@de.ibm.com)
 */
@Configuration
public class DefaultConfiguration implements AboutControllerConfiguration {

    @Value("${ada.application.name:ada}")
    private String name;

    @Value("${ada.application.build:0.0.0}")
    private String build;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getBuild() {
        return build;
    }

    @Bean
    public ActorSystem getActorSystem() {
        return ActorSystem.create(getName());
    }

    /**
     * This configuration is required for asynchronuous processes like streaming HTTP.
     */
    @Bean
    @SuppressWarnings("unused")
    protected WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
                configurer.setTaskExecutor(new ConcurrentTaskExecutor(Executors.newFixedThreadPool(5)));
            }
        };
    }

}
