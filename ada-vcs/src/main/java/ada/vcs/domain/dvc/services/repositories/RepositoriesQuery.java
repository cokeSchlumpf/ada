package ada.vcs.domain.dvc.services.repositories;

import ada.commons.util.ErrorMessage;
import ada.commons.util.FQResourceName;
import ada.commons.util.Operators;
import ada.vcs.domain.dvc.entities.Repository;
import ada.vcs.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.domain.dvc.protocol.queries.RepositoriesResponse;
import ada.vcs.domain.dvc.protocol.queries.RepositorySummaryRequest;
import ada.vcs.domain.dvc.protocol.queries.RepositorySummaryResponse;
import ada.vcs.domain.dvc.services.Timeout;
import ada.vcs.domain.dvc.protocol.values.RepositorySummary;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor(staticName = "apply")
public final class RepositoriesQuery extends AbstractBehavior<Object> {

    private final ActorContext<Object> actor;

    private final long durationInSeconds;

    public static Behavior<Object> createBehavior(long durationInSeconds) {
        return Behaviors.setup(ctx -> RepositoriesQuery.apply(ctx, durationInSeconds));
    }

    public static Behavior<Object> createBehavior() {
        return createBehavior(3);
    }

    @Override
    public Receive<Object> createReceive() {
        return newReceiveBuilder()
            .onMessage(StartRepositoriesQuery$New.class, this::collectSummaries)
            .onAnyMessage(msg -> {
                actor.getLog().warning("Received unexpected message of type '{}'", msg.getClass());
                return Behaviors.same();
            })
            .build();
    }

    private Behavior<Object> collectSummaries(StartRepositoriesQuery$New start) {
        final Set<FQResourceName> stillWaiting = Sets.newHashSet(start.getRepositories());
        final Set<RepositorySummary> summaries = Sets.newHashSet();
        final AtomicInteger errors = new AtomicInteger(0); // atomic because we need final vars for lambdas

        ClusterSharding sharding = ClusterSharding.get(actor.getSystem());

        return Behaviors.withTimers(timers -> {
            if (stillWaiting.isEmpty()) {
                start.getReplyTo().tell(RepositoriesResponse.apply());
                return Behaviors.stopped();
            } else {
                for (FQResourceName repo : stillWaiting) {
                    EntityRef<RepositoryMessage> repository = sharding.entityRefFor(
                        Repository.ENTITY_KEY,
                        Repository.createEntityId(repo.getNamespace(), repo.getName()));

                    final ActorRef<RepositorySummaryResponse> summaryAdapter = actor.messageAdapter(
                        RepositorySummaryResponse.class,
                        keep -> keep);

                    final ActorRef<ErrorMessage> errorAdapter = actor.messageAdapter(
                        ErrorMessage.class,
                        keep -> keep);

                    final RepositorySummaryRequest msg = RepositorySummaryRequest.apply(
                        Operators.hash(),
                        start.getExecutor(),
                        repo.getNamespace(),
                        repo.getName(), summaryAdapter, errorAdapter);

                    repository.tell(msg);
                }

                timers.startSingleTimer(Timeout.class, Timeout.apply(), Duration.ofSeconds(durationInSeconds));

                return Behaviors
                    .receive(Object.class)
                    .onMessage(RepositorySummaryResponse.class, (actor, summary) -> {
                        summary.getSummary().ifPresent(summaries::add);
                        stillWaiting.remove(FQResourceName.apply(summary.getNamespace(), summary.getRepository()));

                        if (stillWaiting.size() - errors.get() <= 0) {
                            return respondWithSummaries(start, summaries);
                        } else {
                            return Behaviors.same();
                        }
                    })
                    .onMessage(Timeout.class, (actor, repos) -> {
                        actor.getLog().warning("Did not receive summaries from all repositories.");
                        return respondWithSummaries(start, summaries);
                    })
                    .onMessage(ErrorMessage.class, (actor, repoName) -> {
                        errors.incrementAndGet();

                        if (stillWaiting.size() - errors.get() <= 0) {
                            return respondWithSummaries(start, summaries);
                        } else {
                            return Behaviors.same();
                        }
                    })
                    .onAnyMessage((actor, msg) -> {
                        actor.getLog().warning("Received unexpected message of type {}", msg.getClass());
                        return Behaviors.same();
                    })
                    .build();
            }
        });
    }

    private Behavior<Object> respondWithSummaries(StartRepositoriesQuery$New start, Set<RepositorySummary> summaries) {
        List<RepositorySummary> result = Ordering
            .natural()
            .reverse()
            .onResultOf(RepositorySummary::getLastUpdate)
            .sortedCopy(summaries);

        start.getReplyTo().tell(RepositoriesResponse.apply(result));
        return Behaviors.stopped();
    }

}
