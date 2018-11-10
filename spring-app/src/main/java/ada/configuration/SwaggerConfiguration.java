package ada.configuration;

import ada.web.resources.about.AboutControllerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Michael Wellner (michael.wellner@de.ibm.com)
 */
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    private final AboutControllerConfiguration about;

    @SuppressWarnings("unused")
    public SwaggerConfiguration(AboutControllerConfiguration about) {
        this.about = about;
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("ada.web.resources"))
            .paths(PathSelectors.ant("/api/v1/**"))
            .build()
            .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
            .title(about.getName())
            .version(about.getBuild())
            .contact(new Contact("Michael Wellner (michael.wellner@de.ibm.com", null, null))
            .description("Ada API")
            .build();
    }

}
