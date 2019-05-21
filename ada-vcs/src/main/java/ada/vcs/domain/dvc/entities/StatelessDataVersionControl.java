package ada.vcs.domain.dvc.entities;

import ada.commons.util.FQResourceName;
import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.domain.dvc.protocol.api.DataVersionControlMessage;
import ada.vcs.domain.dvc.protocol.api.NamespaceMessage;
import ada.vcs.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.domain.dvc.protocol.commands.CreateRepository;
import ada.vcs.domain.dvc.protocol.events.RepositoryCreated;
import ada.vcs.domain.dvc.protocol.queries.RepositoriesRequest;
import ada.vcs.domain.dvc.services.Timeout;
import ada.vcs.domain.dvc.services.repositories.RepositoriesQuery;
import ada.vcs.domain.dvc.services.repositories.StartRepositoriesQuery$New;
import ada.vcs.domain.legacy.repository.api.RepositoryStorageAdapter;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.ddata.Key;
import akka.cluster.ddata.ORSet;
import akka.cluster.ddata.ORSetKey;
import akka.cluster.ddata.SelfUniqueAddress;
import akka.cluster.ddata.typed.javadsl.DistributedData;
import akka.cluster.ddata.typed.javadsl.Replicator;
import akka.cluster.sharding.typed.ShardingEnvelope;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatelessDataVersionControl extends AbstractBehavior<DataVersionControlMessage> {

    private static final Key<ORSet<FQResourceName>> DD_KEY = ORSetKey.create("repositories");

    private final ActorContext<DataVersionControlMessage> actor;

    private final SelfUniqueAddress node;

    private final ActorRef<ShardingEnvelope<NamespaceMessage>> namespaceShards;

    private final ActorRef<Replicator.Command> replicator;

    private Set<FQResourceName> repositories;

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

            final Entity<NamespaceMessage, ShardingEnvelope<NamespaceMessage>> nsEntity = Entity
                .ofPersistentEntity(Namespace.ENTITY_KEY, ctx -> {
                    ResourceName name = ResourceName.apply(ctx.getEntityId());

                    return Namespace.createEntity(ctx.getActorContext(), context, name, repositoryShards);
                });

            final ActorRef<ShardingEnvelope<NamespaceMessage>> namespaceShards = sharding.init(nsEntity);

            final ActorRef<Replicator.Changed<ORSet<FQResourceName>>> changedAdapter = actor.messageAdapter(
                (Class<Replicator.Changed<ORSet<FQResourceName>>>) (Object) Replicator.Changed.class,
                InternalChanged::apply);

            final ActorRef<Replicator.Command> replicator = DistributedData.get(actor.getSystem()).replicator();
            replicator.tell(new Replicator.Subscribe<>(DD_KEY, changedAdapter));

            final SelfUniqueAddress node = DistributedData.get(actor.getSystem()).selfUniqueAddress();

            return new StatelessDataVersionControl(actor, node, namespaceShards, replicator, Sets.newHashSet());
        });
    }

    @Override
    public Receive<DataVersionControlMessage> createReceive() {
        return newReceiveBuilder()
            .onMessage(CreateRepository.class, this::onCreate)
            .onMessage(NamespaceMessage.class, this::onForward)
            .onMessage(InternalChanged.class, this::onInternalChanged)
            .onMessage(RepositoriesRequest.class, this::onRepositoriesRequest)
            .build();
    }

    @SuppressWarnings("unchecked")
    private Behavior<DataVersionControlMessage> onCreate(CreateRepository create) {
        ActorRef<Object> onCreated = actor.spawnAnonymous(
            Behaviors.withTimers(timers -> {
                timers.startSingleTimer(Timeout.class, Timeout.apply(), Duration.ofSeconds(10));

                return Behaviors
                    .receive(Object.class)
                    .onMessage(RepositoryCreated.class, (ctx, created) -> {
                        final ActorRef<Replicator.UpdateResponse<ORSet<FQResourceName>>> updateActorRef = actor.spawnAnonymous(Behaviors
                            .receive((Class<Replicator.UpdateResponse<ORSet<FQResourceName>>>) (Object) Replicator.UpdateResponse.class)
                            .onAnyMessage((ctx2, update) -> {
                                create.getReplyTo().tell(created);
                                return Behaviors.stopped();
                            })
                            .build());

                        final FQResourceName fqn = FQResourceName.apply(create.getNamespace(), create.getRepository());

                        replicator.tell(new Replicator.Update<>(
                            DD_KEY, ORSet.empty(), Replicator.writeLocal(), updateActorRef, set -> set.add(node, fqn)));

                        return Behaviors.stopped();
                    })
                    .onMessage(Timeout.class, (ctx, timeout) -> Behaviors.stopped())
                    .build();
            }));


        return onForward(create.withReplyTo(onCreated.unsafeUpcast()));
    }

    private Behavior<DataVersionControlMessage> onForward(NamespaceMessage msg) {
        String entityId = Namespace.createEntityId(msg.getNamespace());
        namespaceShards.tell(ShardingEnvelope.apply(entityId, msg));
        return this;
    }

    private Behavior<DataVersionControlMessage> onInternalChanged(InternalChanged changed) {
        this.repositories = changed.msg.dataValue().getElements();
        return this;
    }

    private Behavior<DataVersionControlMessage> onRepositoriesRequest(RepositoriesRequest request) {
        final Behavior<Object> behavior = RepositoriesQuery.createBehavior();
        final ActorRef<Object> query = actor.spawn(behavior, String.format("repositories-query-%s", Operators.hash()));
        query.tell(StartRepositoriesQuery$New.apply(request.getExecutor(), request.getReplyTo(), this.repositories));

        return this;
    }

    @AllArgsConstructor(staticName = "apply")
    private static class InternalChanged implements DataVersionControlMessage {

        Replicator.Changed<ORSet<FQResourceName>> msg;

    }

}
