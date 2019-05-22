package ada.vcs.domain.dvc.services;

import ada.commons.util.Either;
import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourcePath;
import ada.vcs.domain.dvc.entities.Repository;
import ada.vcs.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.domain.dvc.protocol.commands.CreateRepository;
import ada.vcs.domain.dvc.protocol.events.RepositoryCreated;
import ada.vcs.domain.dvc.services.registry.RegisterResource;
import ada.vcs.domain.dvc.services.registry.RemoveResource;
import ada.vcs.domain.dvc.services.registry.ResourceRegistryCommand;
import akka.Done;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.ShardingEnvelope;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Duration;

public final class CreateRepositorySaga {

    private static long TIMEOUT_IN_SECONDS = 5;

    private CreateRepositorySaga() {
        // Do not make any instances
    }

    public static Behavior<CreateRepositoryMessage> createBehavior(
        ActorRef<ShardingEnvelope<RepositoryMessage>> repositories,
        ActorRef<ResourceRegistryCommand> registry,
        CreateRepository create) {

        return Behaviors.setup(actor -> registering(actor, repositories, registry, create));
    }

    private static Behavior<CreateRepositoryMessage> registering(
        ActorContext<CreateRepositoryMessage> actor,
        ActorRef<ShardingEnvelope<RepositoryMessage>> repositories,
        ActorRef<ResourceRegistryCommand> registry,
        CreateRepository create) {

        final ActorRef<Boolean> booleanAdapter = actor.messageAdapter(Boolean.class, RegisterResult::apply);
        final ResourcePath toRegister = ResourcePath.apply(create.getNamespace(), create.getRepository());

        registry.tell(RegisterResource.apply(toRegister, booleanAdapter));

        return Behaviors
            .withTimers(timers -> {
                timers.startSingleTimer(Timeout.class, Timeout.apply(), Duration.ofMinutes(TIMEOUT_IN_SECONDS));

                return Behaviors
                    .receive(CreateRepositoryMessage.class)
                    .onMessage(
                        RegisterResult.class,
                        result -> result.success,
                        (ctx, result) -> creating(ctx, repositories, registry, create))
                    .onMessage(
                        RegisterResult.class,
                        result -> !result.success,
                        (ctx, result) -> {
                            /*
                             * When the repository already exists, we forward the message to the repository
                             * to provide a correct response - this is required to ensure idempotence.
                             */
                            String entityId = Repository.createEntityId(create.getNamespace(), create.getRepository());
                            repositories.tell(ShardingEnvelope.apply(entityId, create));

                            return Behaviors.stopped();
                        }
                    )
                    .onMessage(
                        Timeout.class,
                        (ctx, timeout) -> {
                            ctx.getLog().warning("Registering resource timed out, try to roll-back registry ...");
                            return rollingBack(ctx, registry, create);
                        }
                    )
                    .build();
            });
    }

    private static Behavior<CreateRepositoryMessage> creating(
        ActorContext<CreateRepositoryMessage> actor,
        ActorRef<ShardingEnvelope<RepositoryMessage>> repositories,
        ActorRef<ResourceRegistryCommand> registry,
        CreateRepository create) {

        final ActorRef<RepositoryCreated> repositoryCreatedAdapter = actor.messageAdapter(
            RepositoryCreated.class, created -> CreateResult.apply(Either.left(created)));

        final ActorRef<ErrorMessage> errorMessageAdapter = actor.messageAdapter(
            ErrorMessage.class, error -> CreateResult.apply(Either.right(error)));

        final String entityId = Repository.createEntityId(
            create.getNamespace(), create.getRepository());

        final CreateRepository wrappedMessage = create
            .withReplyTo(repositoryCreatedAdapter)
            .withErrorTo(errorMessageAdapter);

        repositories.tell(ShardingEnvelope.apply(entityId, wrappedMessage));

        return Behaviors.withTimers(timers -> {
            timers.startSingleTimer(Timeout.class, Timeout.apply(), Duration.ofSeconds(TIMEOUT_IN_SECONDS));

            return Behaviors
                .receive(CreateRepositoryMessage.class)
                .onMessage(
                    CreateResult.class,
                    (ctx, result) -> result.getValue().map(
                        created -> {
                            create.getReplyTo().tell(created);
                            return Behaviors.stopped();
                        },
                        error -> {
                            create.getErrorTo().tell(error);
                            return Behavior.stopped();
                        }
                    ))
                .onMessage(
                    Timeout.class,
                    (ctx, timeout) -> {
                        ctx.getLog().warning("CreateRepository request timed out. Rolling back registry ...");
                        return rollingBack(ctx, registry, create);
                    }
                )
                .build();
        });
    }

    private static Behavior<CreateRepositoryMessage> rollingBack(
        ActorContext<CreateRepositoryMessage> actor,
        ActorRef<ResourceRegistryCommand> registry,
        CreateRepository create) {

        ActorRef<Done> doneAdapter = actor.messageAdapter(Done.class, done -> RegisterRemoved.apply());
        final ResourcePath toRegister = ResourcePath.apply(create.getNamespace(), create.getRepository());
        registry.tell(RemoveResource.apply(toRegister, doneAdapter));

        return Behaviors.withTimers(timers -> {
            timers.startSingleTimer(Timeout.class, Timeout.apply(), Duration.ofSeconds(TIMEOUT_IN_SECONDS));

            return Behaviors
                .receive(CreateRepositoryMessage.class)
                .onMessage(RegisterRemoved.class, (ctx, removed) -> Behavior.stopped())
                .onMessage(Timeout.class, (ctx, timeout) -> {
                    ctx.getLog().warning("Unsuccessful roll back, will now terminate saga ...");
                    return Behavior.stopped();
                })
                .build();
        });
    }

    public interface CreateRepositoryMessage {

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class RegisterResult implements CreateRepositoryMessage {

        private final boolean success;

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class RegisterRemoved implements CreateRepositoryMessage {

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class CreateResult implements CreateRepositoryMessage {

        private final Either<RepositoryCreated, ErrorMessage> value;

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class Timeout implements CreateRepositoryMessage {

    }

}
