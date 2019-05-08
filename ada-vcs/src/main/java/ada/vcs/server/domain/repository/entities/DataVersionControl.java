package ada.vcs.server.domain.repository.entities;

import ada.commons.util.ResourceName;
import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.shared.repository.api.RepositoryStorageAdapter;
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

import static ada.vcs.server.domain.repository.entities.Protocol.*;

@AllArgsConstructor(staticName = "apply")
public final class DataVersionControl extends AbstractBehavior<DataVersionControlMessage> {

    private final ActorContext<DataVersionControlMessage> actor;

    private final CommandContext context;

    private final RepositoryStorageAdapter repositoryStorageAdapter;

    private final HashMap<ResourceName, ActorRef<NamespaceMessage>> namespaceToActorRef;

    public static Behavior<DataVersionControlMessage> createBehavior(
        CommandContext context, RepositoryStorageAdapter repositoryStorageAdapter) {
        return Behaviors.setup(ctx -> DataVersionControl.apply(ctx, context, repositoryStorageAdapter, Maps.newHashMap()));
    }

    @Override
    public Receive<DataVersionControlMessage> createReceive() {
        return newReceiveBuilder()
            .onMessage(CreateRepository.class, this::onCreateRepository)
            .onMessage(NamespaceRemoved.class, this::onNamespaceRemoved)
            .onMessage(NamespaceMessage.class, this::forward)
            .build();
    }

    private Behavior<DataVersionControlMessage> forward(NamespaceMessage msg) {
        ActorRef<NamespaceMessage> namespaceActor = namespaceToActorRef.get(msg.getNamespace());

        if (namespaceActor != null) {
            namespaceActor.tell(msg);
        } else {
            actor.getLog().warning("Ignoring message for not existing namespace '{}'", msg.getNamespace().getValue());
        }

        return this;
    }

    private Behavior<DataVersionControlMessage> onCreateRepository(CreateRepository create) {
        ActorRef<NamespaceMessage> namespaceActor = namespaceToActorRef.get(create.getNamespace());

        if (namespaceActor == null) {
            actor.getLog().info("Creating repository namespace '{}'", create.getNamespace().getValue());

            namespaceActor = actor.spawn(
                Namespace.createBehavior(context, repositoryStorageAdapter, create.getNamespace()),
                create.getNamespace().getValue());

            namespaceToActorRef.put(create.getNamespace(), namespaceActor);
            actor.watchWith(namespaceActor, NamespaceRemoved.apply(create.getNamespace()));

        }

        namespaceActor.tell(create);

        return this;
    }

    private Behavior<DataVersionControlMessage> onNamespaceRemoved(NamespaceRemoved removed) {
        actor.getLog().info("Repository namespace '{}' has been removed", removed.namespace.getValue());
        namespaceToActorRef.remove(removed.getNamespace());
        return this;
    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class NamespaceRemoved implements DataVersionControlMessage {

        private final ResourceName namespace;

    }

}
