package ada.vcs.domain.dvc;

import ada.commons.util.FQResourceName;
import ada.commons.util.Operators;
import ada.commons.util.ResourcePath;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.domain.dvc.entities.Repository;
import ada.vcs.domain.dvc.protocol.api.DataVersionControlMessage;
import ada.vcs.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.domain.dvc.protocol.commands.CreateRepository;
import ada.vcs.domain.dvc.protocol.commands.RemoveRepository;
import ada.vcs.domain.dvc.protocol.queries.RepositoriesRequest;
import ada.vcs.domain.dvc.services.CreateRepositorySaga;
import ada.vcs.domain.dvc.services.RemoveRepositorySaga;
import ada.vcs.domain.dvc.services.RepositoriesQuery;
import ada.vcs.domain.dvc.services.registry.ResourceRegistry;
import ada.vcs.domain.dvc.services.registry.ResourceRegistryCommand;
import ada.vcs.domain.legacy.repository.api.RepositoryStorageAdapter;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.ddata.ORSet;
import akka.cluster.ddata.typed.javadsl.DistributedData;
import akka.cluster.ddata.typed.javadsl.Replicator;
import akka.cluster.sharding.typed.ShardingEnvelope;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.typed.ClusterSingleton;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataVersionControl extends AbstractBehavior<DataVersionControlMessage> {

    private final ActorContext<DataVersionControlMessage> actor;

    private final ActorRef<ShardingEnvelope<RepositoryMessage>> repositories;

    private final ActorRef<ResourceRegistryCommand> registry;

    private Set<ResourcePath> resources;

    @SuppressWarnings("unchecked")
    public static Behavior<DataVersionControlMessage> createBehavior(
        CommandContext context, RepositoryStorageAdapter repositoryStorageAdapter) {

        return Behaviors.setup(actor -> {
            final ClusterSharding sharding = ClusterSharding.get(actor.getSystem());

            final Entity<RepositoryMessage, ShardingEnvelope<RepositoryMessage>> repoEntity = Entity
                .ofPersistentEntity(Repository.ENTITY_KEY, ctx -> {
                    FQResourceName repoName = FQResourceName.apply(ctx.getEntityId());

                    return Repository.create(
                        ctx.getActorContext(), context, repositoryStorageAdapter,
                        repoName.getNamespace(), repoName.getName());
                });

            final ActorRef<ShardingEnvelope<RepositoryMessage>> repositoryShards = sharding.init(repoEntity);

            final ActorRef<Replicator.Changed<ORSet<ResourcePath>>> changedAdapter = actor.messageAdapter(
                (Class<Replicator.Changed<ORSet<ResourcePath>>>) (Object) Replicator.Changed.class,
                RegisteredResourcesUpdated::apply);

            final ActorRef<Replicator.Command> replicator = DistributedData.get(actor.getSystem()).replicator();
            replicator.tell(new Replicator.Subscribe<>(ResourceRegistry.DD_REPOSITORIES_KEY, changedAdapter));

            ClusterSingleton singleton = ClusterSingleton.createExtension(actor.getSystem());
            ActorRef<ResourceRegistryCommand> resourceRegistry = singleton.init(ResourceRegistry.createSingleton());

            return new DataVersionControl(actor, repositoryShards, resourceRegistry, Sets.newHashSet());
        });
    }

    @Override
    public Receive<DataVersionControlMessage> createReceive() {
        return newReceiveBuilder()
            .onMessage(CreateRepository.class, this::onCreate)
            .onMessage(RemoveRepository.class, this::onRemove)
            .onMessage(RepositoryMessage.class, this::onForward)
            .onMessage(RegisteredResourcesUpdated.class, this::onInternalChanged)
            .onMessage(RepositoriesRequest.class, this::onRepositoriesRequest)
            .build();
    }

    private Behavior<DataVersionControlMessage> onCreate(CreateRepository create) {
        actor.spawn(
            CreateRepositorySaga.createBehavior(repositories, registry, create),
            String.format("create-%s-%s", create.getId(), Operators.hash()));

        return this;
    }

    private Behavior<DataVersionControlMessage> onForward(RepositoryMessage message) {
        String entityId = Repository.createEntityId(message.getNamespace(), message.getRepository());
        repositories.tell(ShardingEnvelope.apply(entityId, message));
        return this;
    }

    private Behavior<DataVersionControlMessage> onInternalChanged(RegisteredResourcesUpdated changed) {
        this.resources = changed.msg.dataValue().getElements();
        return this;
    }

    private Behavior<DataVersionControlMessage> onRemove(RemoveRepository remove) {
        actor.spawn(
            RemoveRepositorySaga.createBehavior(repositories, registry, remove),
            String.format("remove-%s-%s", remove.getId(), Operators.hash()));

        return this;
    }

    private Behavior<DataVersionControlMessage> onRepositoriesRequest(RepositoriesRequest request) {
        actor.spawn(
            RepositoriesQuery.createBehavior(repositories, request, resources),
            String.format("repositories-%s", Operators.hash()));

        return this;
    }

    @AllArgsConstructor(staticName = "apply")
    private static class RegisteredResourcesUpdated implements DataVersionControlMessage {

        Replicator.Changed<ORSet<ResourcePath>> msg;

    }

}
