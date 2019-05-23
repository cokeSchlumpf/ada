package ada.vcs.domain.dvc.services;

import ada.commons.util.ResourceName;
import ada.vcs.domain.legacy.repository.api.RefSpec;
import ada.vcs.domain.legacy.repository.api.RepositoryStorageAdapter;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class RemoveRepositoryDataSaga {

    private static final long TIMEOUT_IN_SECONDS = 30;

    private RemoveRepositoryDataSaga() {

    }

    public static Behavior<RemoveRepositoryDataSagaMessage> createBehavior(
        ResourceName namespace,
        ResourceName name,
        RepositoryStorageAdapter adapter,
        Set<RefSpec.VersionRef> versions) {

        return Behaviors.setup(actor -> deletingVersions(actor, namespace, name, adapter, versions));
    }

    private static Behavior<RemoveRepositoryDataSagaMessage> deletingVersions(
        ActorContext<RemoveRepositoryDataSagaMessage> actor,
        ResourceName namespace,
        ResourceName name,
        RepositoryStorageAdapter adapter,
        Set<RefSpec.VersionRef> versions) {

        return Behaviors.withTimers(timers -> {
            if (versions.isEmpty()) {
                return Behavior.stopped();
            } else {
                final AtomicInteger deletedCount = new AtomicInteger(0);

                final Supplier<Behavior<RemoveRepositoryDataSagaMessage>> next = () -> {
                    if (deletedCount.intValue() >= versions.size()) {
                        return deletingRepository(actor, namespace, name, adapter);
                    } else {
                        return Behavior.same();
                    }
                };

                timers.startSingleTimer(Timeout.class, Timeout.apply(), Duration.ofSeconds(TIMEOUT_IN_SECONDS));

                versions.forEach(version -> adapter
                    .clean(version)
                    .handle((done, ex) -> {
                        if (ex != null) {
                            Error error = Error.apply(namespace, name, ex, version);
                            actor.getSelf().tell(error);
                        } else {
                            VersionDeleted deleted = VersionDeleted.apply(namespace, name, version);
                            actor.getSelf().tell(deleted);
                        }

                        return done;
                    }));

                return Behaviors
                    .receive(RemoveRepositoryDataSagaMessage.class)
                    .onMessage(VersionDeleted.class, (ctx, deleted) -> {
                        deletedCount.incrementAndGet();

                        ctx.getLog().debug(
                            String.format(
                                "Successfully deleted repository data %s/%s/%s",
                                deleted.name, deleted.namespace, deleted.version));

                        return next.get();
                    })
                    .onMessage(Error.class, (ctx, error) -> {
                        deletedCount.incrementAndGet();

                        ctx.getLog().warning(
                            error.ex,
                            String.format(
                                "An exception occurred while deleting repository data %s/%s/%s",
                                error.name, error.namespace, error.version));

                        return next.get();
                    })
                    .onMessage(Timeout.class, (ctx, timeout) -> Behavior.stopped())
                    .build();
            }
        });
    }

    private static Behavior<RemoveRepositoryDataSagaMessage> deletingRepository(
        ActorContext<RemoveRepositoryDataSagaMessage> actor,
        ResourceName namespace,
        ResourceName name,
        RepositoryStorageAdapter adapter) {

        return Behaviors.withTimers(timers -> {
            timers.startSingleTimer(Timeout.class, Timeout.apply(), Duration.ofSeconds(TIMEOUT_IN_SECONDS));

            adapter
                .clean()
                .handle((done, ex) -> {
                    if (ex != null) {
                        Error error = Error.apply(namespace, name, ex);
                        actor.getSelf().tell(error);
                    } else {
                        RepositoryDeleted deleted = RepositoryDeleted.apply(namespace, name);
                        actor.getSelf().tell(deleted);
                    }

                    return done;
                });

            return Behaviors
                .receive(RemoveRepositoryDataSagaMessage.class)
                .onMessage(RepositoryDeleted.class, (ctx, deleted) -> {
                    ctx.getLog().debug(
                        String.format(
                            "Successfully cleaned data for repository %s/%s",
                            deleted.namespace, deleted.name));

                    return Behavior.stopped();
                })
                .onMessage(Error.class, (ctx, error) -> {
                    ctx.getLog().warning(
                        error.ex,
                        String.format(
                            "An error occurred while cleaning data for repository %s/%s",
                            error.namespace, error.name));

                    return Behavior.stopped();
                })
                .build();
        });
    }

    private interface RemoveRepositoryDataSagaMessage {

    }

    @AllArgsConstructor(staticName = "apply")
    private static class VersionDeleted implements RemoveRepositoryDataSagaMessage {

        private final ResourceName namespace;

        private final ResourceName name;

        private final RefSpec.VersionRef version;

    }

    @AllArgsConstructor(staticName = "apply")
    private static class RepositoryDeleted implements RemoveRepositoryDataSagaMessage {

        private final ResourceName namespace;

        private final ResourceName name;

    }

    @AllArgsConstructor(staticName = "apply")
    private static class Error implements RemoveRepositoryDataSagaMessage {

        private final ResourceName namespace;

        private final ResourceName name;

        private final Throwable ex;

        private final RefSpec.VersionRef version;

        public static Error apply(ResourceName namespace, ResourceName name, Throwable ex) {
            return Error.apply(namespace, name, ex, null);
        }

    }

    @AllArgsConstructor(staticName = "apply")
    private static class Timeout implements RemoveRepositoryDataSagaMessage {

    }

}
