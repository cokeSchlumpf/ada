package ada.vcs.server;

import ada.vcs.server.directives.ServerDirectives;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
public final class Server extends HttpApp {

    private final ServerDirectives directives;

    @Override
    protected Route routes() {
        return extractMaterializer(materializer ->
            concat(
                directives.repository(repository ->
                    concat(
                        put(() ->
                            withoutSizeLimit(() ->
                                directives.pushRecords((details, records) -> records
                                    .runWith(repository.insert(details), materializer)
                                    .thenApply(directives::complete)))),
                        directives.refSpec(refSpec ->
                            directives.fromRecords(repository.read(refSpec))))),
                get(() ->
                    complete(HttpEntities.create(ContentTypes.TEXT_HTML_UTF8, "<h1>Say hello to akka-http</h1>")))));
    }

}
