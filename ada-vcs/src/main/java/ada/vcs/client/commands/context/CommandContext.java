package ada.vcs.client.commands.context;

import ada.commons.databind.ObjectMapperFactory;
import ada.vcs.client.core.AdaHome;
import ada.vcs.client.core.project.AdaProject;
import ada.vcs.client.exceptions.NoProjectException;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@AllArgsConstructor(staticName = "apply")
public final class CommandContext {

    private final Supplier<Materializer> materializer;

    private final Supplier<ActorSystem> system;

    private final List<Runnable> shutdownActions;

    private final Factories factories;

    public static CommandContext apply() {
        ArrayList<Runnable> shutdownActions = Lists.newArrayList();

        Supplier<ActorSystem> system = Suppliers.memoize(() -> {
            ActorSystem s = ActorSystem.create();
            shutdownActions.add(s::terminate);
            return s;
        });

        Supplier<Materializer> materializer = Suppliers.memoize(() -> ActorMaterializer.create(system.get()));

        Factories factories = Factories.apply(ObjectMapperFactory.apply().create(true), system);

        return new CommandContext(materializer, system, shutdownActions, factories);
    }

    public Factories factories() {
        return factories;
    }

    public Materializer materializer() {
        return materializer.get();
    }

    public ActorSystem system() {
        return system.get();
    }

    public void withProject(Consumer<AdaProject> block) {
        AdaProject project = factories
            .projectFactory()
            .fromHere()
            .orElseThrow(NoProjectException::apply);

        block.accept(project);
    }

    public void withMaterializer(Function<Materializer, CompletionStage<?>> block) {
        try {
            block
                .apply(materializer())
                .toCompletableFuture()
                .get();
        } catch (InterruptedException | ExecutionException e) {
            ExceptionUtils.wrapAndThrow(e);
        }
    }

    public void shutdown() {
        shutdownActions.forEach(Runnable::run);
    }

}
