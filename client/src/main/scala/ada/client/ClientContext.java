package ada.client;

import ada.web.controllers.AboutResource;
import akka.stream.Materializer;

public interface ClientContext extends CommandContext {

    AboutResource getAboutResource();

    Materializer getMaterializer();

}
