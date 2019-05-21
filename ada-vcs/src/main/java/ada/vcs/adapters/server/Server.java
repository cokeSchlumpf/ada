package ada.vcs.adapters.server;

import ada.vcs.api.RepositoriesResource;
import ada.vcs.adapters.server.directives.ServerDirectives;
import ada.vcs.domain.dvc.protocol.values.Authorization;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.RejectionHandler;
import akka.http.javadsl.server.Route;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
public final class Server extends HttpApp {

    private final ServerDirectives directives;

    private final RepositoriesResource repositories;

    @Override
    protected Route routes() {
        return extractMaterializer(materializer ->
            directives.user(user ->
                concat(
                    pathEndOrSingleSlash(() ->
                        get(() -> onSuccess(
                            repositories.listRepositories(user),
                            directives::complete))),
                    directives.resource(namespace ->
                        directives.resource(repository ->
                            concat(
                                pathPrefix("access", () ->
                                    concat(
                                        put(() ->
                                            directives.jsonEntity(Authorization.class, authorization ->
                                                onSuccess(
                                                    repositories.grant(user, namespace, repository, authorization),
                                                    directives::complete
                                                ))),
                                        delete(() ->
                                            directives.jsonEntity(Authorization.class, authorization ->
                                                onSuccess(
                                                    repositories.revoke(user, namespace, repository, authorization),
                                                    directives::complete))))),
                                put(() ->
                                    onSuccess(
                                        repositories.createRepository(user, namespace, repository),
                                        done -> complete(StatusCodes.OK))),
                                post(() ->
                                    withoutSizeLimit(() ->
                                        directives.records((details, records) ->
                                            repositories
                                                .push(user, namespace, repository, details, records)
                                                .thenApply(directives::complete)))),
                                get(() ->
                                    directives.complete(repositories.details(user, namespace, repository))),
                                directives.refSpec(refSpec ->
                                    get(() ->
                                        directives.complete(
                                            repositories
                                                .pull(user, namespace, repository, refSpec))))
                            ))),
                    get(() ->
                        complete(HttpEntities.create(ContentTypes.TEXT_HTML_UTF8, "<h1>Say hello to akka-http</h1>"))))))
            .seal(RejectionHandler.defaultHandler(), directives.exceptionHandler());
    }

}
