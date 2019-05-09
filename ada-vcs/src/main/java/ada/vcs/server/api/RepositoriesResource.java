package ada.vcs.server.api;

import ada.commons.util.ActorPatterns;
import ada.commons.util.ErrorMessage;
import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.vcs.server.domain.dvc.protocol.api.DataVersionControlMessage;
import ada.vcs.server.domain.dvc.protocol.commands.CreateRepository;
import ada.vcs.server.domain.dvc.protocol.commands.Push;
import ada.vcs.server.domain.dvc.protocol.events.RepositoryCreated;
import ada.vcs.server.domain.dvc.protocol.queries.Pull;
import ada.vcs.server.domain.dvc.protocol.queries.RepositoriesRequest;
import ada.vcs.server.domain.dvc.protocol.queries.RepositoriesResponse;
import ada.vcs.server.domain.dvc.values.User;
import ada.vcs.shared.repository.api.*;
import ada.vcs.shared.repository.api.version.VersionDetails;
import akka.Done;
import akka.actor.ActorSystem;
import akka.actor.typed.ActorRef;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public final class RepositoriesResource {

    private final ActorRef<DataVersionControlMessage> repositories;

    private final RepositorySinkFactory sinkFactory;

    private final RepositorySourceFactory sourceFactory;

    private final ActorSystem system;

    private final ActorPatterns patterns;

    public CompletionStage<Done> createRepository(
        User user, ResourceName namespace, ResourceName repository) {

        final String correlationId = Operators.hash();

        return patterns
            .ask(
                repositories,
                (ActorRef<RepositoryCreated> actor) ->
                    CreateRepository.apply(correlationId, user, namespace, repository, actor))
            .thenApply(created -> Done.getInstance());
    }

    public CompletionStage<RepositoriesResponse> listRepositories(User user) {
        return patterns
            .ask(
                repositories,
                (ActorRef<RepositoriesResponse> actor) -> RepositoriesRequest.apply(user, actor));
    }

    public CompletionStage<VersionDetails> push(
        User user, ResourceName namespace, ResourceName repository, VersionDetails details,
        Source<ByteString, CompletionStage<VersionDetails>> records) {

        final String correlationId = Operators.hash();

        return patterns
            .ask(
                repositories,
                (ActorRef<RepositoryCreated> actor) ->
                    CreateRepository.apply(correlationId, user, namespace, repository, actor))
            .thenCompose(created -> patterns.ask(
                repositories,
                (ActorRef<RepositorySinkMemento> actor, ActorRef<ErrorMessage> error) ->
                    Push.apply(correlationId, user, namespace, repository, details.memento(), actor, error)))
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
        User user, ResourceName namespace, ResourceName repository, RefSpec refSpec) {

        final String correlationId = Operators.hash();

        return Source
            .fromSourceCompletionStage(patterns
                .ask(
                    repositories,
                    (ActorRef<RepositoryCreated> actor) ->
                        CreateRepository.apply(correlationId, user, namespace, repository, actor))
                .thenCompose(created -> patterns.ask(
                    repositories,
                    (ActorRef<RepositorySourceMemento> actor, ActorRef<ErrorMessage> error) ->
                        Pull.apply(correlationId, user, namespace, repository, refSpec, actor, error)))
                .thenApply(sourceMemento ->
                    sourceFactory.create(sourceMemento).get()))
            .mapMaterializedValue(cs -> cs.thenCompose(vd -> vd));
    }

}
