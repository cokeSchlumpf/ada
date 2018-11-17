package ada.client;

import ada.client.output.ClientOutput;
import ada.web.controllers.AboutResource;
import akka.stream.Materializer;

public interface ClientContext {

    AboutResource getAboutResource();

    Materializer getMaterializer();

    ClientOutput getOutput();

}
