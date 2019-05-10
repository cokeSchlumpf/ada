package ada.vcs.server.domain.dvc.entities;

import ada.commons.util.ResourceName;
import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.server.domain.dvc.protocol.commands.CreateRepository;
import ada.vcs.server.domain.dvc.protocol.api.NamespaceMessage;
import ada.vcs.server.domain.dvc.protocol.events.RepositoryCreated;
import ada.vcs.server.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.server.domain.dvc.protocol.queries.RepositoriesInNamespaceRequest;
import ada.vcs.server.domain.dvc.protocol.queries.RepositoriesInNamespaceResponse;
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

import java.util.Date;
import java.util.HashMap;

@AllArgsConstructor(staticName = "apply")
public final class Namespace extends AbstractBehavior<NamespaceMessage> {

    private final ActorContext<NamespaceMessage> actor;

    private final CommandContext context;

    private final RepositoryStorageAdapter repositoryStorageAdapter;

    private final ResourceName name;

    private final HashMap<ResourceName, ActorRef<RepositoryMessage>> nameToActorRef;

    public static Behavior<NamespaceMessage> createBehavior(
        CommandContext context, RepositoryStorageAdapter repositoryStorageAdapter, ResourceName name) {

        return Behaviors.setup(actor -> apply(actor, context, repositoryStorageAdapter, name, Maps.newHashMap()));
    }

    @Override
    public Receive<NamespaceMessage> createReceive() {
        return newReceiveBuilder()
            .onMessage(CreateRepository.class, this::onCreateRepository)
            .onMessage(RepositoriesInNamespaceRequest.class, this::onRepositoriesRequest)
            .onMessage(RepositoryRemoved.class, this::onRepositoryRemoved)
            .onMessage(RepositoryMessage.class, this::forward)
            .build();
    }

    private Behavior<NamespaceMessage> onRepositoriesRequest(RepositoriesInNamespaceRequest request) {
        RepositoriesInNamespaceResponse response = RepositoriesInNamespaceResponse.apply(name, Maps.newHashMap(nameToActorRef));
        request.getReplyTo().tell(response);
        return this;
    }

    private Behavior<NamespaceMessage> forward(RepositoryMessage msg) {
        if (msg.getNamespace().equals(name)) {
            ActorRef<RepositoryMessage> repo = nameToActorRef.get(msg.getRepository());

            if (repo != null) {
                repo.tell(msg);
            } else {
                actor.getLog().warning(
                    "Ignoring message for not existing repository '{}/{}'",
                    msg.getNamespace().getValue(), msg.getRepository().getValue());
            }
        } else {
            actor.getLog().warning(
                "Ignoring message for namespace '{}' - Not responsible", name.getValue());
        }

        return this;
    }

    private Behavior<NamespaceMessage> onCreateRepository(CreateRepository create) {
        if (create.getNamespace().equals(name)) {
            ActorRef<RepositoryMessage> repo = nameToActorRef.get(create.getRepository());

            if (repo == null) {
                actor.getLog().info(
                    "Creating repository '{}/{}'",
                    create.getNamespace().getValue(), create.getRepository().getValue());

                Behavior<RepositoryMessage> repoBehavior = Repository.createBehavior(
                    context, repositoryStorageAdapter, create.getNamespace(), create.getRepository(), new Date());

                repo = actor.spawn(repoBehavior, create.getRepository().getValue());
                nameToActorRef.put(create.getRepository(), repo);

                actor.watchWith(repo, RepositoryRemoved.apply(create.getNamespace(), create.getRepository()));
            }

            RepositoryCreated created = RepositoryCreated.apply(
                create.getId(), create.getNamespace(), create.getRepository(), repo);

            create.getReplyTo().tell(created);
        } else {
            actor.getLog().warning(
                "Ignoring message for namespace '{}' - Not responsible", name.getValue());
        }

        return this;
    }

    private Behavior<NamespaceMessage> onRepositoryRemoved(RepositoryRemoved removed) {
        actor.getLog().info(
            "Repository '{}/{}' has been removed",
            removed.getNamespace().getValue(), removed.name.getValue());
        nameToActorRef.remove(removed.getName());
        return this;
    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class RepositoryRemoved implements NamespaceMessage {

        private final ResourceName namespace;

        private final ResourceName name;

    }

}