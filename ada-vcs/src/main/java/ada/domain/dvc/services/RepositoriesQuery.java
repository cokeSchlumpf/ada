package ada.domain.dvc.services;

import ada.commons.util.ErrorMessage;
import ada.commons.util.FQResourceName;
import ada.commons.util.Operators;
import ada.commons.util.ResourcePath;
import ada.domain.dvc.entities.Repository;
import ada.domain.dvc.protocol.api.RepositoryMessage;
import ada.domain.dvc.protocol.queries.RepositoriesRequest;
import ada.domain.dvc.protocol.queries.RepositoriesResponse;
import ada.domain.dvc.protocol.queries.RepositorySummaryRequest;
import ada.domain.dvc.protocol.queries.RepositorySummaryResponse;
import ada.domain.dvc.values.repository.RepositorySummary;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.ShardingEnvelope;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class RepositoriesQuery {

    private static final long TIMEOUT_IN_SECONDS = 10;

    private RepositoriesQuery() {

    }

    public static Behavior<RepositoryQueryMessage> createBehavior(
        ActorRef<ShardingEnvelope<RepositoryMessage>> repositories,
        RepositoriesRequest request,
        Set<ResourcePath> resources) {
        return Behaviors.setup(actor -> collecting(actor, repositories, request, resources));
    }

    private static Behavior<RepositoryQueryMessage> collecting(
        ActorContext<RepositoryQueryMessage> actor,
        ActorRef<ShardingEnvelope<RepositoryMessage>> repositories,
        RepositoriesRequest request,
        Set<ResourcePath> resources) {

        final Set<FQResourceName> waitingFor = getRepositoriesFromResources(resources);
        final AtomicInteger errors = new AtomicInteger(0);
        final Set<RepositorySummary> summaries = Sets.newHashSet();

        final Supplier<Behavior<RepositoryQueryMessage>> reply = () -> {
            request
                .getReplyTo()
                .tell(RepositoriesResponse.apply(ImmutableList.copyOf(summaries)));

            return Behavior.stopped();
        };

        final ActorRef<RepositorySummaryResponse> responseAdapter = actor.messageAdapter(
            RepositorySummaryResponse.class,
            RepositorySummaryResponseEnv::apply);

        final ActorRef<ErrorMessage> errorAdapter = actor.messageAdapter(
            ErrorMessage.class,
            ErrorMessageEnv::apply);

        waitingFor.forEach(repository -> {
            String entityId = Repository.createEntityId(repository.getNamespace(), repository.getName());
            RepositorySummaryRequest msg = RepositorySummaryRequest.apply(
                Operators.hash(),
                request.getExecutor(),
                repository.getNamespace(),
                repository.getName(),
                responseAdapter,
                errorAdapter);

            repositories.tell(ShardingEnvelope.apply(entityId, msg));
        });



        Supplier<Behavior<RepositoryQueryMessage>> nextBehavior = () -> {
            if (waitingFor.size() - errors.intValue() <= 0) {
                return reply.get();
            } else {
                return Behavior.same();
            }
        };

        if (waitingFor.isEmpty()) {
            return reply.get();
        } else {
            return Behaviors.withTimers(timers -> {
                timers.startSingleTimer(Timeout.class, Timeout.apply(), Duration.ofSeconds(TIMEOUT_IN_SECONDS));

                return Behaviors
                    .receive(RepositoryQueryMessage.class)
                    .onMessage(
                        RepositorySummaryResponseEnv.class,
                        (ctx, summary) -> {
                            summary
                                .getValue()
                                .getSummary()
                                .ifPresent(summaries::add);

                            FQResourceName fqn = FQResourceName.apply(
                                summary.value.getNamespace(),
                                summary.value.getRepository());

                            waitingFor.remove(fqn);

                            return nextBehavior.get();
                        }
                    )
                    .onMessage(
                        ErrorMessageEnv.class,
                        (ctx, error) -> {
                            ctx.getLog().warning("Received error message from repository for summary request: {}", error);
                            errors.incrementAndGet();
                            return nextBehavior.get();
                        }
                    )
                    .onMessage(
                        Timeout.class,
                        (ctx, timeout) -> {
                            ctx.getLog().warning("Did not receive response from all repositories collecting summaries.");
                            return reply.get();
                        }
                    )
                    .build();
            });
        }
    }

    private static Set<FQResourceName> getRepositoriesFromResources(Set<ResourcePath> resources) {
        return resources
            .stream()
            .map(path -> FQResourceName.tryApply(path.toString()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
    }

    public interface RepositoryQueryMessage {

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class RepositorySummaryResponseEnv implements RepositoryQueryMessage {

        private final RepositorySummaryResponse value;

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class ErrorMessageEnv implements RepositoryQueryMessage {

        private final ErrorMessage error;

    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class Timeout implements RepositoryQueryMessage {

    }

}
