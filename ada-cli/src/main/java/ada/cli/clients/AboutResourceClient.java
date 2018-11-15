package ada.cli.clients;

import ada.web.controllers.AboutResource;
import ada.web.controllers.model.AboutAnonymousUser;
import ada.web.controllers.model.AboutApplication;
import ada.web.controllers.model.AboutUser;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.AsPublisher;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.reactivestreams.Publisher;

public class AboutResourceClient implements AboutResource {

    private final ActorMaterializer mat;

    private AboutResourceClient(ActorMaterializer mat) {
        this.mat = mat;
    }

    public static AboutResourceClient apply(ActorMaterializer mat) {
        return new AboutResourceClient(mat);
    }

    @Override
    public AboutApplication getAbout() {
        return AboutApplication.apply("foo", "bar");
    }

    @Override
    public Publisher<String> getAboutStream() {
        String sample = "I'am\nIBM Ada";

        return Source
                .from(Lists.newArrayList(sample.split("\n")))
                .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), mat);
    }

    @Override
    public AboutUser getUser() {
        return AboutAnonymousUser.apply(ImmutableList.of());
    }

}
