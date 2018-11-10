package ada.web.resources.about;

import lombok.*;

/**
 * @author Michael Wellner (michael.wellner@de.ibm.com)
 */
public interface AboutControllerConfiguration {

    String getName();

    String getBuild();

    @Value
    @EqualsAndHashCode
    @AllArgsConstructor(staticName = "apply")
    @NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
    class AboutControllerConfigurationFakeImpl implements AboutControllerConfiguration {

        public final String name;

        public final String build;

    }

}
