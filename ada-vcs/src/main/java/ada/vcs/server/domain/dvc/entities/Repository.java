package ada.vcs.server.domain.dvc.entities;

import ada.commons.util.ResourceName;
import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.server.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.server.domain.dvc.protocol.commands.GrantAccessToRepository;
import ada.vcs.server.domain.dvc.protocol.commands.Push;
import ada.vcs.server.domain.dvc.protocol.commands.RevokeAccessToRepository;
import ada.vcs.server.domain.dvc.protocol.errors.RefSpecAlreadyExistsError;
import ada.vcs.server.domain.dvc.protocol.errors.RefSpecNotFoundError;
import ada.vcs.server.domain.dvc.protocol.errors.UserNotAuthorizedError;
import ada.vcs.server.domain.dvc.protocol.queries.Pull;
import ada.vcs.server.domain.dvc.protocol.queries.RepositorySummaryRequest;
import ada.vcs.server.domain.dvc.protocol.queries.RepositorySummaryResponse;
import ada.vcs.server.domain.dvc.values.GrantedAuthorization;
import ada.vcs.server.domain.dvc.values.RepositoryAuthorizations;
import ada.vcs.server.domain.dvc.values.RepositorySummary;
import ada.vcs.shared.repository.api.RefSpec;
import ada.vcs.shared.repository.api.RepositorySinkMemento;
import ada.vcs.shared.repository.api.RepositorySourceMemento;
import ada.vcs.shared.repository.api.RepositoryStorageAdapter;
import ada.vcs.shared.repository.api.version.VersionDetails;
import ada.vcs.shared.repository.watcher.WatcherSinkMemento;
import ada.vcs.shared.repository.watcher.WatcherSourceMemento;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor(staticName = "apply")
public final class Repository extends AbstractBehavior<RepositoryMessage> {

    private final ActorContext<RepositoryMessage> actor;

    private final CommandContext context;

    private final RepositoryStorageAdapter repositoryStorageAdapter;

    private final ResourceName namespace;

    private final ResourceName name;

    private final Map<RefSpec.VersionRef, VersionDetails> versions;

    private final Map<RefSpec.TagRef, RefSpec.VersionRef> tags;

    private RepositoryAuthorizations authorizations;

    private Date created;

    public static Behavior<RepositoryMessage> createBehavior(
        CommandContext context, RepositoryStorageAdapter repositoryStorageAdapter,
        ResourceName namespace, ResourceName name, Date created) {

        return Behaviors.setup(ctx -> apply(
            ctx, context, repositoryStorageAdapter, namespace,
            name, Maps.newHashMap(), Maps.newHashMap(),
            RepositoryAuthorizations.apply(namespace, name), created));
    }

    @Override
    public Receive<RepositoryMessage> createReceive() {
        return newReceiveBuilder()
            .onMessage(GrantAccessToRepository.class, whenChecked(this::onGrant)::apply)
            .onMessage(Pull.class, whenChecked(this::onPull)::apply)
            .onMessage(Push.class, whenChecked(this::onPush)::apply)
            .onMessage(RepositorySummaryRequest.class, whenResponsible(this::onSummaryRequest)::apply)
            .onMessage(RevokeAccessToRepository.class, whenChecked(this::onRevoke)::apply)
            .build();
    }

    private Behavior<RepositoryMessage> onSummaryRequest(RepositorySummaryRequest request) {
        RepositorySummary summary;

        boolean isAuthorized = authorizations.isAuthorized(request).orElse(false);
        if (isAuthorized) {
            if (versions.isEmpty()) {
                summary = RepositorySummary.apply(namespace, name, created);
            } else {
                VersionDetails details = Ordering
                    .natural()
                    .reverse()
                    .onResultOf(VersionDetails::date)
                    .sortedCopy(versions.values())
                    .get(0);

                summary = RepositorySummary.apply(namespace, name, details.date(), details.id());
            }

            RepositorySummaryResponse response = RepositorySummaryResponse.apply(request.getId(), namespace, name, summary);
            request.getReplyTo().tell(response);
        } else {
            RepositorySummaryResponse response = RepositorySummaryResponse.apply(request.getId(), namespace, name);
            request.getReplyTo().tell(response);
        }

        return this;
    }

    private Behavior<RepositoryMessage> onGrant(GrantAccessToRepository grant) {
        GrantedAuthorization granted = GrantedAuthorization.apply(
            grant.getExecutor(), new Date(), grant.getAuthorization());

        authorizations = authorizations.add(granted);

        return this;
    }

    private Behavior<RepositoryMessage> onPull(Pull pull) {
        RefSpec.VersionRef versionRef = refSpecToVersionRef(pull.getRefSpec());

        if (versionRef != null) {
            RepositorySourceMemento actualSourceMemento = repositoryStorageAdapter.pull(namespace, name, versionRef);
            WatcherSourceMemento watcherSourceMemento = WatcherSourceMemento.apply(actualSourceMemento, actor.getSelf());

            pull.getReplyTo().tell(watcherSourceMemento);
        } else {
            RefSpecNotFoundError response = RefSpecNotFoundError.apply(
                pull.getId(),
                pull.getNamespace(),
                pull.getRepository(),
                pull.getRefSpec());

            pull.getErrorTo().tell(response);
        }

        return this;
    }

    private Behavior<RepositoryMessage> onPush(Push push) {
        RefSpec.VersionRef versionRef = RefSpec.VersionRef.apply(push.getDetails().getId());

        if (!versions.containsKey(versionRef)) {
            VersionDetails details = context.factories().versionFactory().createDetails(push.getDetails());
            RepositorySinkMemento actualSinkMemento = repositoryStorageAdapter.push(namespace, name, details);
            WatcherSinkMemento watcherSinkMemento = WatcherSinkMemento.apply(actualSinkMemento, actor.getSelf());

            versions.put(versionRef, details);
            push.getReplyTo().tell(watcherSinkMemento);
        } else {
            RefSpecAlreadyExistsError error = RefSpecAlreadyExistsError.apply(
                push.getId(),
                push.getNamespace(),
                push.getRepository(),
                versionRef);

            push.getErrorTo().tell(error);
        }

        return this;
    }

    private Behavior<RepositoryMessage> onRevoke(RevokeAccessToRepository revoke) {
        authorizations = authorizations.remove(revoke.getAuthorization());
        return this;
    }

    private RefSpec.VersionRef refSpecToVersionRef(RefSpec refSpec) {
        if (refSpec instanceof RefSpec.VersionRef && versions.containsKey(refSpec)) {
            return (RefSpec.VersionRef) refSpec;
        } else if (refSpec instanceof RefSpec.TagRef) {
            return tags.get(refSpec);
        } else {
            return null;
        }
    }

    private <T extends RepositoryMessage> Function<T, Behavior<RepositoryMessage>> whenChecked(
        Function<T, Behavior<RepositoryMessage>> then) {

        return whenResponsible(whenAuthorized(then));
    }

    private <T extends RepositoryMessage> Function<T, Behavior<RepositoryMessage>> whenAuthorized(
        Function<T, Behavior<RepositoryMessage>> then) {

        return message -> authorizations
            .isAuthorized(message)
            .map(authorized -> {
                if (authorized) {
                    return then.apply(message);
                } else {
                    actor.getLog().warning(
                        "Refusing operation in repository '{}/{}' for user {}",
                        message.getNamespace().getValue(),
                        message.getRepository().getValue(),
                        message.getExecutor());

                    message.getErrorTo().tell(UserNotAuthorizedError.apply(message.getId(), message.getExecutor()));
                    return this;
                }
            })
            .orElseGet(() -> {
                actor.getLog().warning(
                    "No authorization result provided in repository '{}/{}' for operation {}",
                    message.getNamespace().getValue(),
                    message.getRepository().getValue(),
                    message);

                return this;
            });
    }

    private <T extends RepositoryMessage> Function<T, Behavior<RepositoryMessage>> whenResponsible(
        Function<T, Behavior<RepositoryMessage>> then) {

        return message -> {
            if (message.getNamespace().equals(namespace) && message.getRepository().equals(name)) {
                return then.apply(message);
            } else {
                actor.getLog().warning(
                    "Ignoring message for repository '{}/{}'",
                    message.getNamespace().getValue(),
                    message.getRepository().getValue());

                return this;
            }
        };
    }

}
