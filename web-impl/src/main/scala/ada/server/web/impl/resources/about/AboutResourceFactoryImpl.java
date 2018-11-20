package ada.server.web.impl.resources.about;

import ada.server.web.impl.resources.about.AboutResourceImpl;
import ada.web.controllers.AboutResource;
import ada.web.controllers.AboutResourceFactory;
import ada.web.controllers.model.AboutUser;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import lombok.*;

/**
 * @author Michael Wellner (michael.wellner@de.ibm.com)
 */
@Value
@EqualsAndHashCode
@AllArgsConstructor(staticName = "apply")
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public final class AboutResourceFactoryImpl implements AboutResourceFactory {

    public final AboutControllerConfiguration configuration;

    public final AboutUser user;

    public final Materializer materializer;

    public static AboutResourceFactory apply(AboutControllerConfiguration configuration, AboutUser user, ActorSystem system) {
        return apply(configuration, user, ActorMaterializer.create(system));
    }

    @Override
    public AboutResource create() {
        return AboutResourceImpl.apply(
            this.configuration,
            this.user,
            this.materializer);
    }

}
