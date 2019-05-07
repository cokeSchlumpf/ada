package ada.vcs.server.domain.repository;

import ada.commons.util.ResourceName;
import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.core.repository.api.RefSpec;
import ada.vcs.client.core.repository.api.RepositorySinkMemento;
import ada.vcs.client.core.repository.api.RepositorySourceMemento;
import ada.vcs.client.core.repository.api.RepositoryStorageAdapter;
import ada.vcs.client.core.repository.api.version.VersionDetails;
import ada.vcs.client.core.repository.watcher.WatcherSinkMemento;
import ada.vcs.client.core.repository.watcher.WatcherSourceMemento;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

import static ada.vcs.server.domain.repository.Protocol.*;

@AllArgsConstructor(staticName = "apply")
public final class Repository extends AbstractBehavior<RepositoryMessage> {

    private final ActorContext<RepositoryMessage> actor;

    private final CommandContext context;

    private final RepositoryStorageAdapter repositoryStorageAdapter;

    private final ResourceName namespace;

    private final ResourceName name;

    private final List<RefSpec.VersionRef> versions;

    private final Map<RefSpec.TagRef, RefSpec.VersionRef> tags;

    public static Behavior<RepositoryMessage> createBehavior(
        CommandContext context, RepositoryStorageAdapter repositoryStorageAdapter,
        ResourceName namespace, ResourceName name) {

        return Behaviors.setup(ctx -> apply(
            ctx, context, repositoryStorageAdapter, namespace,
            name, Lists.newArrayList(), Maps.newHashMap()));
    }

    @Override
    public Receive<Protocol.RepositoryMessage> createReceive() {
        return newReceiveBuilder()
            .onMessage(Pull.class, this::onPull)
            .onMessage(Push.class, this::onPush)
            .build();
    }

    private Behavior<RepositoryMessage> onPull(Pull pull) {
        if (pull.getNamespace().equals(namespace) && pull.getRepository().equals(name)) {
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
        } else {
            actor.getLog().warning(
                "Ignoring message for repository '{}/{}'",
                pull.getNamespace().getValue(),
                pull.getRepository().getValue());
        }

        return this;
    }

    private Behavior<RepositoryMessage> onPush(Push push) {
        if (push.getNamespace().equals(namespace) && push.getRepository().equals(name)) {
            RefSpec.VersionRef versionRef = RefSpec.VersionRef.apply(push.getDetails().getId());

            if (!versions.contains(versionRef)) {
                VersionDetails details = context.factories().versionFactory().createDetails(push.getDetails());
                RepositorySinkMemento actualSinkMemento = repositoryStorageAdapter.push(namespace, name, details);
                WatcherSinkMemento watcherSinkMemento = WatcherSinkMemento.apply(actualSinkMemento, actor.getSelf());

                push.getReplyTo().tell(watcherSinkMemento);
            } else {
                RefSpecAlreadyExistsError error = RefSpecAlreadyExistsError.apply(
                    push.getId(),
                    push.getNamespace(),
                    push.getRepository(),
                    versionRef);

                push.getHandleError().tell(error);
            }
        } else {
            actor.getLog().warning(
                "Ignoring message for repository '{}/{}'",
                push.getNamespace().getValue(),
                push.getRepository().getValue());
        }

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

}
