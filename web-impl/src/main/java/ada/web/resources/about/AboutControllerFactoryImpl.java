package ada.web.resources.about;

import ada.web.resources.about.model.AboutUser;
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
public final class AboutControllerFactoryImpl implements AboutControllerFactory {

    public final AboutControllerConfiguration configuration;

    public final AboutUser user;

    public final Materializer materializer;

    public static AboutControllerFactory apply(AboutControllerConfiguration configuration, AboutUser user, ActorSystem system) {
        return apply(configuration, user, ActorMaterializer.create(system));
    }

    @Override
    public AboutController create() {
        return AboutControllerImpl.apply(
            this.configuration,
            this.user,
            this.materializer);
    }

}
