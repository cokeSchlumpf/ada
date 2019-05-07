package ada.vcs.server.actors.guardian;

import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.core.repository.fs.FileSystemRepositorySettings;
import ada.vcs.client.core.repository.fs.FileSystemRepositoryStorageAdapter;
import ada.vcs.server.actors.repository.RepositoryManager;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletionStage;

import static ada.vcs.server.actors.guardian.Protocol.*;
import static ada.vcs.server.actors.repository.Protocol.*;

@AllArgsConstructor(staticName = "apply")
public final class ApplicationGuardian extends AbstractBehavior<GuardianMessage> {

    private ActorContext<GuardianMessage> actor;

    private CommandContext context;

    private ActorRef<RepositoryManagerMessage> repository;

    public static Behavior<GuardianMessage> createBehavior(CompletionStage<CommandContext> context) {
        return Behaviors.setup(ctx -> apply(ctx, null, null));
    }

    @Override
    public Receive<GuardianMessage> createReceive() {
        return newReceiveBuilder()
            .onMessage(StartRepository.class, this::onStartRepository)
            .build();
    }

    private Behavior<GuardianMessage> onStartRepository(StartRepository start) {
        if (repository == null) {
            final FileSystemRepositorySettings settings = context
                .factories()
                .repositoryFactory()
                .createSettingsBuilder()
                .build();

            final FileSystemRepositoryStorageAdapter adapter = FileSystemRepositoryStorageAdapter
                .apply(settings, start.getRoot());

            final Behavior<RepositoryManagerMessage> repository = RepositoryManager
                .createBehavior(context, adapter);

            this.repository = actor.spawn(repository, "repository");
        }

        start.getReplyTo().tell(RepositoryStarted.apply(repository));

        return this;
    }

}
