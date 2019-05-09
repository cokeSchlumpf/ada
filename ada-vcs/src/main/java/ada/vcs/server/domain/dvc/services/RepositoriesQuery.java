package ada.vcs.server.domain.dvc.services;

import ada.commons.util.ResourceName;
import ada.vcs.server.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.server.domain.dvc.protocol.queries.RepositorySummaryRequest;
import ada.vcs.server.domain.dvc.protocol.queries.RepositoriesInNamespaceRequest;
import ada.vcs.server.domain.dvc.protocol.queries.RepositoriesInNamespaceResponse;
import ada.vcs.server.domain.dvc.values.RepositorySummary;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor(staticName = "apply")
public final class RepositoriesQuery extends AbstractBehavior<Object> {

    private final ActorContext<Object> actor;

    private final long durationInSeconds;

    @Override
    public Receive<Object> createReceive() {
        return newReceiveBuilder()
            .onMessage(StartRepositoriesQuery.class, this::waitForRepositories)
            .build();
    }

    private Behavior<Object> waitForRepositories(StartRepositoriesQuery start) {
        final Set<ResourceName> stillWaiting = Sets.newHashSet();
        final Map<RepositoryName, ActorRef<RepositoryMessage>> collected = Maps.newHashMap();

        return Behaviors.withTimers(timers -> {
            if (start.getNamespaces().isEmpty()) {
                start.getReplyTo().tell(Lists.newArrayList());
                return Behaviors.stopped();
            } else {
                start.getNamespaces().forEach((name, namespace) -> {
                    ActorRef<RepositoriesInNamespaceResponse> adapter = actor.messageAdapter(
                        RepositoriesInNamespaceResponse.class,
                        keep -> keep);

                    RepositoriesInNamespaceRequest msg = RepositoriesInNamespaceRequest.apply(start.getExecutor(), name, adapter);

                    stillWaiting.add(name);
                    namespace.tell(msg);
                });

                timers.startSingleTimer(QueryTimeout.class, QueryTimeout.apply(), Duration.ofSeconds(durationInSeconds));

                return Behaviors
                    .receive(Object.class)
                    .onMessage(RepositoriesInNamespaceResponse.class, (actor, repos) -> {
                        repos
                            .getRepositories()
                            .forEach((repoName, repoActor) -> {
                                collected.put(RepositoryName.apply(repos.getNamespace(), repoName), repoActor);
                                stillWaiting.remove(repoName);
                            });

                        stillWaiting.removeAll(repos.getRepositories().keySet());

                        if (stillWaiting.isEmpty()) {
                            return waitForSummaries(start, collected);
                        } else {
                            return Behaviors.same();
                        }
                    })
                    .onMessage(QueryTimeout.class, (actor, repos) -> {
                        actor.getLog().warning("Did not receive repositories from all namespaces.");

                        if (collected.isEmpty()) {
                            start.getReplyTo().tell(Lists.newArrayList());
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

        return Behaviors.withTimers(timers -> {
            if (collected.isEmpty()) {
                start.getReplyTo().tell(Lists.newArrayList());
                return Behaviors.stopped();
            } else {
                collected.forEach((repoName, repository) -> {
                    ActorRef<RepositorySummary> adapter = actor.messageAdapter(
                        RepositorySummary.class,
                        keep -> keep);

                    RepositorySummaryRequest msg = RepositorySummaryRequest.apply(
                        start.getExecutor(), repoName.getNamespace(), repoName.getRepository(), adapter);

                    stillWaiting.add(repoName);
                    repository.tell(msg);
                });

                timers.startSingleTimer(QueryTimeout.class, QueryTimeout.apply(), Duration.ofSeconds(durationInSeconds));

                return Behaviors
                    .receive(Object.class)
                    .onMessage(RepositorySummary.class, (actor, summary) -> {
                        summaries.add(summary);
                        stillWaiting.remove(RepositoryName.apply(summary.getNamespace(), summary.getRepository()));

                        if (stillWaiting.isEmpty()) {
                            return respondWithSummaries(start, summaries);
                        } else {
                            return Behaviors.same();
                        }
                    })
                    .onMessage(QueryTimeout.class, (actor, repos) -> {
                        actor.getLog().warning("Did not receive summaries from all repositories.");
                        return respondWithSummaries(start, summaries);
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

        start.getReplyTo().tell(result);
        return Behaviors.stopped();
    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class RepositoryName {

        private final ResourceName namespace;

        private final ResourceName repository;

    }

}
