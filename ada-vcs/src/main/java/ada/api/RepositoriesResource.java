package ada.api;

import ada.commons.util.ActorPatterns;
import ada.commons.util.ErrorMessage;
import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.domain.dvc.protocol.api.DataVersionControlMessage;
import ada.domain.dvc.protocol.commands.*;
import ada.domain.dvc.protocol.queries.*;
import ada.domain.dvc.values.repository.*;
import ada.domain.dvc.protocol.events.GrantedAccessToRepository;
import ada.domain.dvc.protocol.events.RepositoryCreated;
import ada.domain.dvc.protocol.events.RepositoryRemoved;
import ada.domain.dvc.protocol.events.RevokedAccessFromRepository;
import ada.domain.dvc.values.Authorization;
import ada.domain.dvc.values.GrantedAuthorization;
import ada.domain.dvc.values.User;
import ada.domain.dvc.values.repository.version.VersionDetails;
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
                (ActorRef<RepositoryCreated> actor, ActorRef<ErrorMessage> error) ->
                    CreateRepository.apply(correlationId, user, namespace, repository, actor, error))
            .thenApply(created -> Done.getInstance());
    }

    public CompletionStage<RepositoryDetailsResponse> details(
        User user, ResourceName namespace, ResourceName repository) {

        final String correlationId = Operators.hash();

        return patterns
            .ask(
                repositories,
                (ActorRef<RepositoryDetailsResponse> replyTo, ActorRef<ErrorMessage> errorTo) ->
                    RepositoryDetailsRequest.apply(correlationId, user, namespace, repository, replyTo, errorTo));
    }

    public CompletionStage<GrantedAuthorization> grant(
        User user, ResourceName namespace, ResourceName repository, Authorization authorization) {

        final String correlationId = Operators.hash();

        return patterns
            .ask(
                repositories,
                (ActorRef<GrantedAccessToRepository> replyTo, ActorRef<ErrorMessage> errorTo) ->
                    GrantAccessToRepository.apply(correlationId, user, namespace, repository, authorization, replyTo, errorTo))
            .thenApply(GrantedAccessToRepository::getAuthorization);
    }

    public CompletionStage<RepositoryRemoved> remove(
        User user, ResourceName namespace, ResourceName repository) {

        final String correlationId = Operators.hash();

        return patterns
            .ask(
                repositories,
                (ActorRef<RepositoryRemoved> replyTo, ActorRef<ErrorMessage> errorTo) ->
                    RemoveRepository.apply(correlationId, user, namespace, repository, replyTo, errorTo));
    }

    public CompletionStage<GrantedAuthorization> revoke(
        User user, ResourceName namespace, ResourceName repository, Authorization authorization) {

        final String correlationId = Operators.hash();

        return patterns
            .ask(
                repositories,
                (ActorRef<RevokedAccessFromRepository> replyTo, ActorRef<ErrorMessage> errorTo) ->
                    RevokeAccessFromRepository.apply(correlationId, user, namespace, repository, authorization, replyTo, errorTo))
            .thenApply(RevokedAccessFromRepository::getAuthorization);
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
                (ActorRef<RepositoryCreated> actor, ActorRef<ErrorMessage> error) ->
                    CreateRepository.apply(correlationId, user, namespace, repository, actor, error))
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
            })
            .thenCompose(versionDetails -> patterns.ask(
                repositories,
                (ActorRef<VersionStatus> actor, ActorRef<ErrorMessage> error) -> {
                    RefSpec.VersionRef versionRef = RefSpec.fromId(versionDetails.id());
                    return SubmitPushInRepository.apply(
                        correlationId, user, namespace, repository, versionRef,
                        actor, error);
                })
                .thenApply(ignore -> versionDetails));
    }

    public Source<ByteString, CompletionStage<VersionDetails>> pull(
        User user, ResourceName namespace, ResourceName repository, RefSpec refSpec) {

        final String correlationId = Operators.hash();

        return Source
            .fromSourceCompletionStage(patterns
                .ask(
                    repositories,
                    (ActorRef<RepositoryCreated> actor, ActorRef<ErrorMessage> error) ->
                        CreateRepository.apply(correlationId, user, namespace, repository, actor, error))
                .thenCompose(created -> patterns.ask(
                    repositories,
                    (ActorRef<RepositorySourceMemento> actor, ActorRef<ErrorMessage> error) ->
                        Pull.apply(correlationId, user, namespace, repository, refSpec, actor, error)))
                .thenApply(sourceMemento ->
                    sourceFactory.create(sourceMemento).get()))
            .mapMaterializedValue(cs -> cs.thenCompose(vd -> vd));
    }

}
