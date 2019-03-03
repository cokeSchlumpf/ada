package ada.cli.testutil;

import ada.cli.configuration.AdaConfiguration;
import ada.cli.configuration.SpringConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ //
    AboutResourceClient.class, //
    AdaConfiguration.class, //
    SpringConfiguration.class //
})
public class AdaTestRootConfiguration {
}
