package ada.cli;

import ada.client.ClientContext;
import ada.client.output.ClientOutput;
import ada.web.controllers.AboutResource;
import akka.stream.Materializer;
import lombok.*;

@Value
@EqualsAndHashCode
@AllArgsConstructor(staticName = "apply")
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class ApplicationContext implements ClientContext {

    private final AboutResource aboutResource;

    private final Materializer materializer;

    private final ClientOutput output;

    @Override
    public AboutResource getAboutResource() {
        return aboutResource;
    }

    @Override
    public Materializer getMaterializer() {
        return materializer;
    }

    @Override
    public ClientOutput getOutput() {
        return output;
    }

}
