package ada.web.impl.resources.about;

import ada.web.api.resources.about.AboutResource;
import akka.stream.Materializer;

public class AboutResourceFactory {

    public static AboutResource create(AboutConfiguration config, Materializer mat) {
        return new AboutResourceImpl(config, mat);
    }

}
