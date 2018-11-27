package ada.web.api.resources.about;

import ada.web.api.resources.about.model.Application;
import ada.web.api.resources.about.model.User;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.reactivestreams.Publisher;

import java.util.function.Function;

public interface AboutResource {

    Application getApplicationAsObject();

    Publisher<String> getApplicationAsStream();

    User getUser(User user);

    Publisher<String> getUserAsStream(User user);

    @Value
    @AllArgsConstructor(staticName = "apply")
    class FakeImpl implements AboutResource {

        private final Application applicationAsObject;

        private final Publisher<String> applicationAsStream;

        private final Function<User, Publisher<String>> createPublisher;

        @Override
        public User getUser(User user) {
            return user;
        }

        @Override
        public Publisher<String> getUserAsStream(User user) {
            return createPublisher.apply(user);
        }

    }

}
