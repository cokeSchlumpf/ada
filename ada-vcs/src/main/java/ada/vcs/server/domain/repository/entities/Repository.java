package ada.vcs.server.domain.repository.entities;

import ada.commons.util.ResourceName;
import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.server.domain.repository.valueobjects.GrantedAuthorization;
import ada.vcs.server.domain.repository.valueobjects.RepositoryAuthorizations;
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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static ada.vcs.server.domain.repository.entities.Protocol.*;

@AllArgsConstructor(staticName = "apply")
public final class Repository extends AbstractBehavior<RepositoryMessage> {

    private final ActorContext<RepositoryMessage> actor;

    private final CommandContext context;

    private final RepositoryStorageAdapter repositoryStorageAdapter;

    private final ResourceName namespace;

    private final ResourceName name;

    private final List<RefSpec.VersionRef> versions;

    private final Map<RefSpec.TagRef, RefSpec.VersionRef> tags;

    private RepositoryAuthorizations authorizations;

    public static Behavior<RepositoryMessage> createBehavior(
        CommandContext context, RepositoryStorageAdapter repositoryStorageAdapter,
        ResourceName namespace, ResourceName name) {

        return Behaviors.setup(ctx -> apply(
            ctx, context, repositoryStorageAdapter, namespace,
            name, Lists.newArrayList(), Maps.newHashMap(),
            RepositoryAuthorizations.apply(namespace, name)));
    }

    @Override
    public Receive<Protocol.RepositoryMessage> createReceive() {
        return newReceiveBuilder()
            .onMessage(GrantAccessToRepository.class, whenChecked(this::onGrant)::apply)
            .onMessage(Pull.class, whenChecked(this::onPull)::apply)
            .onMessage(Push.class, whenChecked(this::onPush)::apply)
            .onMessage(RevokeAccessToRepository.class, whenChecked(this::onRevoke)::apply)
            .build();
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

            pull.getHandleError().tell(response);
        }

        return this;
    }

    private Behavior<RepositoryMessage> onPush(Push push) {
        RefSpec.VersionRef versionRef = RefSpec.VersionRef.apply(push.getDetails().getId());

        if (!versions.contains(versionRef)) {
            VersionDetails details = context.factories().versionFactory().createDetails(push.getDetails());
            RepositorySinkMemento actualSinkMemento = repositoryStorageAdapter.push(namespace, name, details);
            WatcherSinkMemento watcherSinkMemento = WatcherSinkMemento.apply(actualSinkMemento, actor.getSelf());

            versions.add(versionRef);
            push.getReplyTo().tell(watcherSinkMemento);
        } else {
            RefSpecAlreadyExistsError error = RefSpecAlreadyExistsError.apply(
                push.getId(),
                push.getNamespace(),
                push.getRepository(),
                versionRef);

            push.getHandleError().tell(error);
        }

        return this;
    }

    private Behavior<RepositoryMessage> onRevoke(RevokeAccessToRepository revoke) {
        authorizations = authorizations.remove(revoke.getAuthorization());
        return this;
    }

    private RefSpec.VersionRef refSpecToVersionRef(RefSpec refSpec) {
        if (refSpec instanceof RefSpec.VersionRef && versions.contains(refSpec)) {
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
                    // TODO sendToErrorHandler
                    actor.getLog().warning(
                        "Refusing operation in repository '{}/{}' for user {}",
                        message.getNamespace().getValue(),
                        message.getRepository().getValue(),
                        message.getExecutor());

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
