package ada.vcs.server.api;

import ada.commons.util.ActorPatterns;
import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.vcs.shared.repository.api.*;
import ada.vcs.shared.repository.api.version.VersionDetails;
import ada.vcs.server.domain.repository.Protocol;
import akka.actor.ActorSystem;
import akka.actor.typed.ActorRef;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletionStage;

import static ada.vcs.server.domain.repository.Protocol.*;

@AllArgsConstructor(staticName = "apply")
public final class RepositoriesResource {

    private final ActorRef<Protocol.RepositoryManagerMessage> repositories;

    private final RepositorySinkFactory sinkFactory;

    private final RepositorySourceFactory sourceFactory;

    private final ActorSystem system;

    private final ActorPatterns patterns;

    public CompletionStage<VersionDetails> push(
        ResourceName namespace, ResourceName repository, VersionDetails details,
        Source<ByteString, CompletionStage<VersionDetails>> records) {

        final String correlationId = Operators.hash();

        return patterns
            .ask(
                repositories,
                (ActorRef<RepositoryCreated> actor) ->
                    CreateRepository.apply(correlationId, namespace, repository, actor))
            .thenCompose(created -> patterns.ask(
                repositories,
                (ActorRef<RepositorySinkMemento> actor, ActorRef<RefSpecAlreadyExistsError> error) ->
                    Push.apply(correlationId, namespace, repository, details.memento(), actor, error)))
            .thenCompose(sinkMemento -> {
                Sink<ByteString, CompletionStage<VersionDetails>> sink = sinkFactory.create(sinkMemento).get();

                ActorMaterializer mat = ActorMaterializer.create(system);

                return records
                    .runWith(sink, mat)
                    .thenApply(versionDetails -> {
                        mat.shutdown();
                        return versionDetails;
                    });
            });
    }

    public Source<ByteString, CompletionStage<VersionDetails>> pull(
        ResourceName namespace, ResourceName repository, RefSpec refSpec) {

        final String correlationId = Operators.hash();

        return Source
            .fromSourceCompletionStage(patterns
                .ask(
                    repositories,
                    (ActorRef<RepositoryCreated> actor) ->
                        CreateRepository.apply(correlationId, namespace, repository, actor))
                .thenCompose(created -> patterns.ask(
                    repositories,
                    (ActorRef<RepositorySourceMemento> actor, ActorRef<RefSpecNotFoundError> error) ->
                        Pull.apply(correlationId, namespace, repository, refSpec, actor, error)))
                .thenApply(sourceMemento ->
                    sourceFactory.create(sourceMemento).get()))
            .mapMaterializedValue(cs -> cs.thenCompose(vd -> vd));
    }

}
