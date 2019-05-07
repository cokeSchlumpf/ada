package ada.vcs.server;

import ada.vcs.server.api.RepositoriesResource;
import ada.vcs.server.directives.ServerDirectives;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
public final class Server extends HttpApp {

    private final ServerDirectives directives;

    private final RepositoriesResource repositories;

    @Override
    protected Route routes() {
        return extractMaterializer(materializer ->
            concat(
                directives.resource(namespace ->
                    directives.resource(repository ->
                        concat(
                            put(() ->
                                withoutSizeLimit(() ->
                                    directives.records((details, records) ->
                                        repositories
                                            .push(namespace, repository, details, records)
                                            .thenApply(directives::complete)))),
                            directives.refSpec(refSpec ->
                                get(() ->
                                    directives.complete(
                                        repositories
                                            .pull(namespace, repository, refSpec))))
                        ))),
                get(() ->
                    complete(HttpEntities.create(ContentTypes.TEXT_HTML_UTF8, "<h1>Say hello to akka-http</h1>")))));
    }

}
