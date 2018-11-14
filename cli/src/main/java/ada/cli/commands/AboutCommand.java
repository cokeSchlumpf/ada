package ada.cli.commands;

import ada.cli.output.CliOutput;
import ada.web.controllers.AboutResource;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;
import lombok.*;

/**
 * @author Michael Wellner (michael.wellner@de.ibm.com)
 */
@Value
@EqualsAndHashCode
@AllArgsConstructor(staticName = "apply")
public class AboutCommand implements Runnable {

    private final CliOutput out;

    private final AboutResource resource;

    private final Materializer materializer;

    @Override
    public void run() {
        Source
            .fromPublisher(resource.getAboutStream())
            .runForeach(
                out::println,
                materializer);
    }
}
