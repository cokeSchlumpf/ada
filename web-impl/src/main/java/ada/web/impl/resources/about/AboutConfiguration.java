package ada.web.impl.resources.about;

import lombok.AllArgsConstructor;
import lombok.Value;

public interface AboutConfiguration {

    String getName();

    String getBuild();

    @Value
    @AllArgsConstructor(staticName = "apply")
    class FakeImpl implements AboutConfiguration {

        public final String name;

        public final String build;

    }

}
