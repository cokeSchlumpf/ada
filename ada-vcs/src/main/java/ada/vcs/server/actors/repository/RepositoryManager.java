package ada.vcs.server.actors.repository;

import ada.commons.util.ResourceName;
import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.core.repository.api.RepositoryStorageAdapter;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.HashMap;

import static ada.vcs.server.actors.repository.Protocol.*;

@AllArgsConstructor(staticName = "apply")
public final class RepositoryManager extends AbstractBehavior<RepositoryManagerMessage> {

    private final ActorContext<RepositoryManagerMessage> actor;

    private final CommandContext context;

    private final RepositoryStorageAdapter repositoryStorageAdapter;

    private final HashMap<ResourceName, ActorRef<RepositoryNamespaceMessage>> namespaceToActorRef;

    public static Behavior<RepositoryManagerMessage> createBehavior(
        CommandContext context, RepositoryStorageAdapter repositoryStorageAdapter) {
        return Behaviors.setup(ctx -> RepositoryManager.apply(ctx, context, repositoryStorageAdapter, Maps.newHashMap()));
    }

    @Override
    public Receive<RepositoryManagerMessage> createReceive() {
        return newReceiveBuilder()
            .onMessage(CreateRepository.class, this::onCreateRepository)
            .onMessage(NamespaceRemoved.class, this::onNamespaceRemoved)
            .onMessage(RepositoryNamespaceMessage.class, this::forward)
            .build();
    }

    private Behavior<RepositoryManagerMessage> forward(RepositoryNamespaceMessage msg) {
        ActorRef<RepositoryNamespaceMessage> namespaceActor = namespaceToActorRef.get(msg.getNamespace());

        if (namespaceActor != null) {
            namespaceActor.tell(msg);
        } else {
            actor.getLog().warning("Ignoring message for not existing namespace '{}'", msg.getNamespace().getValue());
        }

        return this;
    }

    private Behavior<RepositoryManagerMessage> onCreateRepository(CreateRepository create) {
        ActorRef<RepositoryNamespaceMessage> namespaceActor = namespaceToActorRef.get(create.getNamespace());

        if (namespaceActor == null) {
            actor.getLog().info("Creating repository namespace '{}'", create.getNamespace().getValue());

            namespaceActor = actor.spawn(
                RepositoryNamespace.createBehavior(context, repositoryStorageAdapter, create.getNamespace()),
                create.getNamespace().getValue());

            namespaceToActorRef.put(create.getNamespace(), namespaceActor);
            actor.watchWith(namespaceActor, NamespaceRemoved.apply(create.getNamespace()));

        }

        namespaceActor.tell(create);

        return this;
    }

    private Behavior<RepositoryManagerMessage> onNamespaceRemoved(NamespaceRemoved removed) {
        actor.getLog().info("Repository namespace '{}' has been removed", removed.namespace.getValue());
        namespaceToActorRef.remove(removed.getNamespace());
        return this;
    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class NamespaceRemoved implements RepositoryManagerMessage {

        private final ResourceName namespace;

    }

}
