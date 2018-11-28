package ada.web.impl.resources.about;

import ada.web.api.resources.about.AboutResource;
import ada.web.api.resources.about.model.Application;
import ada.web.api.resources.about.model.AuthenticatedUser;
import ada.web.api.resources.about.model.User;
import akka.stream.Materializer;
import akka.stream.javadsl.AsPublisher;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.google.common.collect.Lists;
import org.reactivestreams.Publisher;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

class AboutResourceImpl implements AboutResource {

    private final AboutConfiguration config;
    private final Materializer mat;

    AboutResourceImpl(AboutConfiguration config, Materializer mat) {
        this.config = config;
        this.mat = mat;
    }

    public Application getApplicationAsObject() {
        return Application.apply(config.getName(), config.getBuild());
    }

    @Override
    public Publisher<String> getApplicationAsStream() {
        String about = String.format(
            getAdaAsciiArt(),
            config.getBuild(),
            config.getName());

        return Source.from(Arrays.asList(about.split("\n")))
                     .map(s -> s)
                     .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), mat);
    }

    private String getCompliment() {
        final String[] compliments = {
            "Your last commit was so beautiful.",
            "You're an awesome data scientist my friend.",
            "You're a gift to those who can work with you.",
            "You're a smart dude.",
            "You are awesome!",
            "You have impeccable manners, jus like Donald Trump.",
            "Your code style is great.",
            "You have the best smile when committing your work.",
            "You are the most perfect you there is.",
            "You're strong, you never give up while fixing bugs.",
            "Your algorithms are refreshing.",
            "Are your colleagues grateful to know you? They should.",
            "You light up the room and fix all issues.",
            "You deserve a better notebook.",
            "You should be proud of your last model trained.",
            "You're more helpful than you realize.",
            "You have a great sense of humor in your code.",
            "You've got an awesome sense of clean code!",
            "You are really courageous trying to reach code coverage.",
            "On a scale from 1 to 10, you're an 11-graded data scientist.",
            "Your projects are art, not just code.",
            "Your code is inspiring.",
            "You're like a ray of sunshine on a really dreary day with many bugs.",
            "You are making a difference in every development team.",
            "You bring out the best in other hackers."
        };

        return compliments[new Random().nextInt(compliments.length)];
    }

    private String getAdaAsciiArt() {
        return "              _       \n" +
            "     /\\      | |      \n" +
            "    /  \\   __| | __ _ \n" +
            "   / /\\ \\ / _` |/ _` |\n" +
            "  / ____ \\ (_| | (_| |\n" +
            " /_/    \\_\\__,_|\\__,_| v%s\n" +
            "                      \n" +
            " instance name: %s";
    }

    private String getRolesAsString(Iterable<String> roles) {
        String result;

        if (!roles.iterator().hasNext()) {
            result = "You have no assigned roles.";
        } else {
            result = "Your assigned roles are:\n";
            result = result + Lists.newArrayList(roles)
                                   .stream()
                                   .map(s -> "  * " + s)
                                   .collect(Collectors.joining("\n"));
        }

        return result;
    }

    @Override
    public User getUser(User user) {
        return user;
    }

    @Override
    public Publisher<String> getUserAsStream(User user) {
        String message;

        if (user instanceof AuthenticatedUser) {
            AuthenticatedUser a = (AuthenticatedUser) user;
            message = "You are authenticated as " + a.getUsername() + "\n"
                      + "  - " + getCompliment()
                      + "\n"
                      + getRolesAsString(a.getRoles());
        } else {
            message = "You are not authenticated\n"
                      + getRolesAsString(user.getRoles());
        }

        return Source.from(Arrays.asList(message.split("\n")))
                     .map(s -> s)
                     .runWith(
                         Sink.asPublisher(AsPublisher.WITHOUT_FANOUT),
                         mat);
    }

}
