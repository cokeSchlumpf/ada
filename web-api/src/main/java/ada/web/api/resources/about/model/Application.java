package ada.web.api.resources.about.model;

import lombok.*;

@Value
@AllArgsConstructor(staticName = "apply")
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public final class Application {

    public final String name;

    public final String build;

}
