package ada.server.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Contains configurations for Spring itself, not really related to business logic etc.
 */
@Configuration
public class SpringConfiguration {

    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new GuavaModule());
        return om;
    }

    @Bean
    public Docket getSwaggerApiDescription() {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("ada.server"))
            .paths(PathSelectors.ant("/api/v1/**"))
            .build()
            .apiInfo(getSwaggerApiDetails());
    }

    private ApiInfo getSwaggerApiDetails() {
        String description = "Ada REST API Documentation. Ada is a Data Science and Analytics platform blueprint.";

        return new ApiInfoBuilder()
            .title("ada")
            .version("0.0.42")
            .contact(new Contact("Michael Wellner", null, "michael.wellner@de.ibm.com"))
            .description(description)
            .build();
    }

}
