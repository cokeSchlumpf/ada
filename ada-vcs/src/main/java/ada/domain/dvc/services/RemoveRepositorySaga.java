package ada.domain.dvc.services;

import ada.commons.util.Either;
import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourcePath;
import ada.domain.dvc.entities.Repository;
import ada.domain.dvc.protocol.api.RepositoryMessage;
import ada.domain.dvc.protocol.commands.RemoveRepository;
import ada.domain.dvc.protocol.events.RepositoryRemoved;
import ada.domain.dvc.services.registry.RemoveResource;
import ada.domain.dvc.services.registry.RegisterResource;
import ada.domain.dvc.services.registry.ResourceRegistryCommand;
import akka.Done;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.ShardingEnvelope;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Duration;

public final class RemoveRepositorySaga {

    private static long TIMEOUT_IN_SECONDS = 30;

    private RemoveRepositorySaga() {
        // Do not make any instances
    }

    public static Behavior<RemoveRepositoryMessage> createBehavior(
        ActorRef<ShardingEnvelope<RepositoryMessage>> repositories,
        ActorRef<ResourceRegistryCommand> registry,
        RemoveRepository remove) {

        return Behaviors.setup(actor -> unregistering(actor, repositories, registry, remove));
    }

    private static Behavior<RemoveRepositoryMessage> unregistering(
        ActorContext<RemoveRepositoryMessage> actor,
        ActorRef<ShardingEnvelope<RepositoryMessage>> repositories,
        ActorRef<ResourceRegistryCommand> registry,
        RemoveRepository remove) {

        final ActorRef<Done> doneAdapter = actor.messageAdapter(Done.class, done -> RegisterRemoved.apply());
        final ResourcePath toRegister = ResourcePath.apply(remove.getNamespace(), remove.getRepository());

        registry.tell(RemoveResource.apply(toRegister, doneAdapter));

        return Behaviors
            .withTimers(timers -> {
                timers.startSingleTimer(Timeout.class, Timeout.apply(), Duration.ofMinutes(TIMEOUT_IN_SECONDS));

                return Behaviors
                    .receive(RemoveRepositoryMessage.class)
                    .onMessage(
                        RegisterRemoved.class,
                        (ctx, result) -> {
                            /*
                             * When the repository already exists, we forward the message to the repository
                             * to provide a correct response - this is required to ensure idempotence.
                             */
                            String entityId = Repository.createEntityId(remove.getNamespace(), remove.getRepository());
                            repositories.tell(ShardingEnvelope.apply(entityId, remove));

                            return removing(ctx, repositories, registry, remove);
                        }
                    )
                    .onMessage(
                        Timeout.class,
                        (ctx, timeout) -> {
                            ctx.getLog().warning("Registering resource timed out, try to roll-back registry ...");
                            return rollingBack(ctx, registry, remove);
                        }
                    )
                    .build();
            });
    }

    private static Behavior<RemoveRepositoryMessage> removing(
        ActorContext<RemoveRepositoryMessage> actor,
        ActorRef<ShardingEnvelope<RepositoryMessage>> repositories,
        ActorRef<ResourceRegistryCommand> registry,
        RemoveRepository remove) {

        final ActorRef<RepositoryRemoved> repositoryRemovedAdapter = actor.messageAdapter(
            RepositoryRemoved.class, removed -> RemoveResult.apply(Either.left(removed)));

        final ActorRef<ErrorMessage> errorMessageAdapter = actor.messageAdapter(
            ErrorMessage.class, error -> RemoveResult.apply(Either.right(error)));

        final String entityId = Repository.createEntityId(
            remove.getNamespace(), remove.getRepository());

        final RemoveRepository wrappedMessage = remove
            .withReplyTo(repositoryRemovedAdapter)
            .withErrorTo(errorMessageAdapter);

        repositories.tell(ShardingEnvelope.apply(entityId, wrappedMessage));

        return Behaviors.withTimers(timers -> {
            timers.startSingleTimer(Timeout.class, Timeout.apply(), Duration.ofSeconds(TIMEOUT_IN_SECONDS));

            return Behaviors
                .receive(RemoveRepositoryMessage.class)
                .onMessage(
                    RemoveResult.class,
                    (ctx, result) -> result.getValue().map(
                        removed -> {
                            remove.getReplyTo().tell(removed);
                            return Behaviors.stopped();
                        },
                        error -> {
                            remove.getErrorTo().tell(error);
                            return rollingBack(ctx, registry, remove);
                        }
                    ))
                .onMessage(
                    Timeout.class,
                    (ctx, timeout) -> {
                        ctx.getLog().warning("CreateRepository request timed out. Rolling back registry ...");
                        return rollingBack(ctx, registry, remove);
                    }
                )
                .build();
        });
    }

    private static Behavior<RemoveRepositoryMessage> rollingBack(
        ActorContext<RemoveRepositoryMessage> actor,
        ActorRef<ResourceRegistryCommand> registry,
        RemoveRepository remove) {

        ActorRef<Boolean> doneAdapter = actor.messageAdapter(Boolean.class, done -> Registered.apply());
        final ResourcePath toRegister = ResourcePath.apply(remove.getNamespace(), remove.getRepository());
        registry.tell(RegisterResource.apply(toRegister, doneAdapter));

        return Behaviors.withTimers(timers -> {
            timers.startSingleTimer(Timeout.class, Timeout.apply(), Duration.ofSeconds(TIMEOUT_IN_SECONDS));

            return Behaviors
                .receive(RemoveRepositoryMessage.class)
                .onMessage(Registered.class, (ctx, removed) -> Behavior.stopped())
                .onMessage(Timeout.class, (ctx, timeout) -> {
                    ctx.getLog().warning("Unsuccessful roll back, will now terminate saga ...");
                    return Behavior.stopped();
                })
                .build();
        });
    }

    public interface RemoveRepositoryMessage {

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class Registered implements RemoveRepositoryMessage {

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class RegisterRemoved implements RemoveRepositoryMessage {

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class RemoveResult implements RemoveRepositoryMessage {

        private final Either<RepositoryRemoved, ErrorMessage> value;

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class Timeout implements RemoveRepositoryMessage {

    }

}
