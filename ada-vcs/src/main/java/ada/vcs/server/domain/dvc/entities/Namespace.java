package ada.vcs.server.domain.dvc.entities;

import ada.commons.util.ErrorMessage;
import ada.commons.util.ResourceName;
import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.server.domain.dvc.protocol.api.NamespaceEvent;
import ada.vcs.server.domain.dvc.protocol.api.NamespaceMessage;
import ada.vcs.server.domain.dvc.protocol.api.RepositoryMessage;
import ada.vcs.server.domain.dvc.protocol.commands.CreateRepository;
import ada.vcs.server.domain.dvc.protocol.commands.InitializeRepository;
import ada.vcs.server.domain.dvc.protocol.errors.RepositoryNotFoundError;
import ada.vcs.server.domain.dvc.protocol.events.RepositoryCreated;
import ada.vcs.server.domain.dvc.protocol.events.RepositoryRemoved;
import ada.vcs.server.domain.dvc.protocol.queries.RepositoriesInNamespaceRequest;
import ada.vcs.server.domain.dvc.protocol.queries.RepositoriesInNamespaceResponse;
import ada.vcs.shared.repository.api.RepositoryStorageAdapter;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Date;
import java.util.HashMap;
import java.util.function.BiFunction;

public final class Namespace extends EventSourcedBehavior<NamespaceMessage, NamespaceEvent, Namespace.State> {

    private final ActorContext<NamespaceMessage> actor;

    private final CommandContext context;

    private final RepositoryStorageAdapter repositoryStorageAdapter;

    private final ResourceName name;

    public Namespace(
        PersistenceId persistenceId, ActorContext<NamespaceMessage> actor, CommandContext context,
        RepositoryStorageAdapter repositoryStorageAdapter, ResourceName name) {

        super(persistenceId);
        this.actor = actor;
        this.context = context;
        this.repositoryStorageAdapter = repositoryStorageAdapter;
        this.name = name;
    }

    public static Behavior<NamespaceMessage> createBehavior(
        CommandContext context, RepositoryStorageAdapter repositoryStorageAdapter, ResourceName name) {
        final PersistenceId id = PersistenceId.apply(String.format("dvc/%s", name.getValue()));
        return Behaviors.setup(actor -> new Namespace(id, actor, context, repositoryStorageAdapter, name));
    }

    @Override
    public State emptyState() {
        return State.apply(Maps.newHashMap());
    }

    @Override
    public CommandHandler<NamespaceMessage, NamespaceEvent, State> commandHandler() {
        return newCommandHandlerBuilder()
            .forAnyState()
            .onCommand(CreateRepository.class, whenResponsible(this::onCreateRepository))
            .onCommand(RepositoriesInNamespaceRequest.class, whenResponsible(this::onRepositoriesRequest))
            .onCommand(RepositoryTerminated.class, whenResponsible(this::onRepositoryTerminated))
            .onCommand(RepositoryMessage.class, whenResponsible(this::forward))
            .build();
    }

    @Override
    public EventHandler<State, NamespaceEvent> eventHandler() {
        return newEventHandlerBuilder()
            .forAnyState()
            .onEvent(RepositoryCreated.class, this::onRepositoryCreated)
            .onEvent(RepositoryRemoved.class, this::onRepositoryRemoved)
            .build();
    }

    private Effect<NamespaceEvent, State> onCreateRepository(State state, CreateRepository create) {
        ActorRef<RepositoryMessage> repo = state.nameToActorRef.get(create.getRepository());

        if (repo == null) {
            RepositoryCreated created = RepositoryCreated.apply(
                create.getId(), create.getNamespace(), create.getRepository(),
                create.getExecutor().getUserId(), new Date());

            return Effect()
                .persist(created)
                .thenRun(newState -> newState
                    .nameToActorRef
                    .get(create.getRepository())
                    .tell(InitializeRepository.apply(create.getId(), create.getExecutor(), name,
                        create.getRepository(), actor.getSystem().deadLetters(), created.getCreated())))
                .thenRun(() -> create.getReplyTo().tell(created));
        } else {
            RepositoryCreated created = RepositoryCreated.apply(
                create.getId(), create.getNamespace(), create.getRepository(),
                create.getExecutor().getUserId(), new Date());

            create.getReplyTo().tell(created);

            return Effect().none();
        }
    }

    private State onRepositoryCreated(State state, RepositoryCreated created) {
        actor.getLog().info(
            "Creating repository '{}/{}'",
            created.getNamespace().getValue(), created.getRepository().getValue());

        Behavior<RepositoryMessage> repoBehavior = Repository.createBehavior(
            context, repositoryStorageAdapter, created.getNamespace(), created.getRepository());

        ActorRef<RepositoryMessage> repo = actor.spawn(repoBehavior, created.getRepository().getValue());
        state.nameToActorRef.put(created.getRepository(), repo);

        RepositoryTerminated terminated = RepositoryTerminated.apply(
            created.getId(), name, created.getRepository(), actor.getSystem().deadLetters());

        actor.watchWith(repo, terminated);

        return state;
    }

    private Effect<NamespaceEvent, State> forward(State state, RepositoryMessage msg) {
        ActorRef<RepositoryMessage> repo = state.nameToActorRef.get(msg.getRepository());

        if (repo != null) {
            repo.tell(msg);
        } else {
            actor.getLog().warning(
                "Ignoring message for not existing repository '{}/{}'",
                msg.getNamespace().getValue(), msg.getRepository().getValue());

            RepositoryNotFoundError error = RepositoryNotFoundError.apply(
                msg.getId(), msg.getNamespace(), msg.getRepository());

            msg.getErrorTo().tell(error);
        }

        return Effect().none();
    }

    private Effect<NamespaceEvent, State> onRepositoriesRequest(State state, RepositoriesInNamespaceRequest request) {
        RepositoriesInNamespaceResponse response = RepositoriesInNamespaceResponse.apply(name, Maps.newHashMap(state.nameToActorRef));
        request.getReplyTo().tell(response);
        return Effect().none();
    }

    private State onRepositoryRemoved(State state, RepositoryRemoved removed) {
        actor.getLog().info(
            "Repository '{}/{}' has been removed", removed.getNamespace(), removed.getRepository());

        state.nameToActorRef.remove(removed.getRepository());

        return state;
    }

    private Effect<NamespaceEvent, State> onRepositoryTerminated(State state, RepositoryTerminated terminated) {
        if (state.nameToActorRef.containsKey(terminated.getRepository())) {
            return Effect()
                .persist(RepositoryRemoved.apply(terminated.namespace, terminated.repository));
        } else {
            return Effect().none();
        }
    }

    private <T extends NamespaceMessage> BiFunction<State, T, Effect<NamespaceEvent, State>> whenResponsible(
        BiFunction<State, T, Effect<NamespaceEvent, State>> then) {

        return (state, message) -> {
            if (message.getNamespace().equals(name)) {
                return then.apply(state, message);
            } else {
                actor.getLog().warning(
                    "Ignoring message for namespace '{}'",
                    message.getNamespace().getValue());

                return Effect().none();
            }
        };
    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    private static class RepositoryTerminated implements NamespaceMessage {

        private final String id;

        public final ResourceName namespace;

        public final ResourceName repository;

        public final ActorRef<ErrorMessage> errorTo;

    }

    @AllArgsConstructor(staticName = "apply")
    public static class State {

        private HashMap<ResourceName, ActorRef<RepositoryMessage>> nameToActorRef;

    }

}
