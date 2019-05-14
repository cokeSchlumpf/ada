package ada.vcs.server.domain.dvc.services;

import ada.commons.util.ErrorMessage;
import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.vcs.server.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.server.domain.dvc.protocol.queries.*;
import ada.vcs.server.domain.dvc.values.RepositorySummary;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Duration;
import java.util.List;
import java.util.Map;
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
            .onMessage(StartRepositoriesQuery.class, this::waitForRepositories)
            .onAnyMessage(msg -> {
                actor.getLog().warning("Received unexpected message of type '{}'", msg.getClass());
                return Behaviors.same();
            })
            .build();
    }

    private Behavior<Object> waitForRepositories(StartRepositoriesQuery start) {
        final Set<ResourceName> stillWaiting = Sets.newHashSet();
        final Map<RepositoryName, ActorRef<RepositoryMessage>> collected = Maps.newHashMap();
        final AtomicInteger errors = new AtomicInteger(0);

        return Behaviors.withTimers(timers -> {
            if (start.getNamespaces().isEmpty()) {
                start.getReplyTo().tell(RepositoriesResponse.apply());
                return Behaviors.stopped();
            } else {
                start.getNamespaces().forEach((name, namespace) -> {
                    ActorRef<RepositoriesInNamespaceResponse> adapter = actor.messageAdapter(
                        RepositoriesInNamespaceResponse.class,
                        keep -> keep);

                    final ActorRef<ErrorMessage> errorAdapter = actor.messageAdapter(
                        ErrorMessage.class,
                        keep -> keep);

                    RepositoriesInNamespaceRequest msg = RepositoriesInNamespaceRequest.apply(
                        Operators.hash(), start.getExecutor(), name, adapter, errorAdapter);

                    stillWaiting.add(name);
                    namespace.tell(msg);
                });

                timers.startSingleTimer(QueryTimeout.class, QueryTimeout.apply(), Duration.ofSeconds(durationInSeconds));

                return Behaviors
                    .receive(Object.class)
                    .onMessage(RepositoriesInNamespaceResponse.class, (actor, repos) -> {
                        repos
                            .getRepositories()
                            .forEach((repoName, repoActor) -> collected
                                .put(RepositoryName.apply(repos.getNamespace(), repoName), repoActor));

                        stillWaiting.remove(repos.getNamespace());

                        if (stillWaiting.size() - errors.get() <= 0) {
                            return waitForSummaries(start, collected);
                        } else {
                            return Behaviors.same();
                        }
                    })
                    .onMessage(ErrorMessage.class, (actor, errorMessage) -> {
                        errors.incrementAndGet();

                        actor.getLog().warning("Received error from namespace: {}", errorMessage.getMessage());

                        if (stillWaiting.size() - errors.get() <= 0) {
                            return waitForSummaries(start, collected);
                        } else {
                            return Behaviors.same();
                        }
                    })
                    .onMessage(QueryTimeout.class, (actor, timeout) -> {
                        actor.getLog().warning("Did not receive repositories from all namespaces.");

                        if (collected.isEmpty()) {
                            start.getReplyTo().tell(RepositoriesResponse.apply());
                            return Behaviors.stopped();
                        } else {
                            return waitForSummaries(start, collected);
                        }
                    })
                    .onAnyMessage((actor, msg) -> {
                        actor.getLog().warning("Received unexpected message of type '{}'", msg.getClass());
                        return Behaviors.same();
                    })
                    .build();
            }
        });
    }

    private Behavior<Object> waitForSummaries(StartRepositoriesQuery start, Map<RepositoryName, ActorRef<RepositoryMessage>> collected) {
        final Set<RepositoryName> stillWaiting = Sets.newHashSet();
        final Set<RepositorySummary> summaries = Sets.newHashSet();
        final AtomicInteger errors = new AtomicInteger(0);

        return Behaviors.withTimers(timers -> {
            if (collected.isEmpty()) {
                start.getReplyTo().tell(RepositoriesResponse.apply());
                return Behaviors.stopped();
            } else {
                for (RepositoryName repoName : collected.keySet()) {
                    final ActorRef<RepositoryMessage> repository = collected.get(repoName);
                    final ActorRef<RepositorySummaryResponse> summaryAdapter = actor.messageAdapter(
                        RepositorySummaryResponse.class,
                        keep -> keep);

                    final ActorRef<ErrorMessage> errorAdapter = actor.messageAdapter(
                        ErrorMessage.class,
                        keep -> keep);

                    final RepositorySummaryRequest msg = RepositorySummaryRequest.apply(
                        Operators.hash(),
                        start.getExecutor(),
                        repoName.getNamespace(),
                        repoName.getRepository(), summaryAdapter, errorAdapter);

                    stillWaiting.add(repoName);
                    repository.tell(msg);
                }

                timers.startSingleTimer(QueryTimeout.class, QueryTimeout.apply(), Duration.ofSeconds(durationInSeconds));

                return Behaviors
                    .receive(Object.class)
                    .onMessage(RepositorySummaryResponse.class, (actor, summary) -> {
                        summary.getSummary().ifPresent(summaries::add);
                        stillWaiting.remove(RepositoryName.apply(summary.getNamespace(), summary.getRepository()));

                        if (stillWaiting.size() - errors.get() <= 0) {
                            return respondWithSummaries(start, summaries);
                        } else {
                            return Behaviors.same();
                        }
                    })
                    .onMessage(QueryTimeout.class, (actor, repos) -> {
                        actor.getLog().warning("Did not receive summaries from all repositories.");
                        return respondWithSummaries(start, summaries);
                    })
                    .onMessage(RepositoryName.class, (actor, repoName) -> {
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

    private Behavior<Object> respondWithSummaries(StartRepositoriesQuery start, Set<RepositorySummary> summaries) {
        List<RepositorySummary> result = Ordering
            .natural()
            .reverse()
            .onResultOf(RepositorySummary::getLastUpdate)
            .sortedCopy(summaries);

        start.getReplyTo().tell(RepositoriesResponse.apply(result));
        return Behaviors.stopped();
    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class RepositoryName {

        private final ResourceName namespace;

        private final ResourceName repository;

    }

}
