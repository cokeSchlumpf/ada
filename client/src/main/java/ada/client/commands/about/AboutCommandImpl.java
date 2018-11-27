package ada.client.commands.about;

import ada.client.output.Output;
import ada.web.api.resources.about.AboutResource;
import ada.web.api.resources.about.model.User;
import akka.Done;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.reactivestreams.Publisher;

import java.util.concurrent.CompletionStage;

public class AboutCommandImpl implements AboutCommand {

    private final AboutResource aboutResource;

    private final Materializer materializer;

    private final Output out;

    private final User user;

    private AboutCommandImpl(AboutResource aboutResource, Materializer materializer, Output out, User user) {
        this.aboutResource = aboutResource;
        this.materializer = materializer;
        this.out = out;
        this.user = user;
    }

    public static AboutCommandImpl apply(AboutResource aboutResource, Materializer materializer, Output out, User user) {
        return new AboutCommandImpl(aboutResource, materializer, out, user);
    }

    @Override
    public void about() {
        Publisher<String> stringPublisher = aboutResource.getApplicationAsStream();

        CompletionStage<Done> done = Source
            .fromPublisher(stringPublisher)
            .runWith(Sink.foreach(out::message), materializer);

        try {
            done.toCompletableFuture().get();
        } catch (Exception e) {
            out.exception(e);
        }
    }

    @Override
    public void aboutUser() {
        Publisher<String> stringPublisher = aboutResource.getUserAsStream(user);

        CompletionStage<Done> done = Source
            .fromPublisher(stringPublisher)
            .runWith(Sink.foreach(out::message), materializer);

        try {
            done.toCompletableFuture().get();
        } catch (Exception e) {
            out.exception(e);
        }
    }

}
