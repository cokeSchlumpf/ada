package ada.vcs.adapters.cli.commands;

import ada.commons.util.Operators;
import ada.vcs.adapters.cli.consoles.CommandLineConsole;
import ada.vcs.adapters.cli.exceptions.CommandNotInitializedException;
import ada.vcs.domain.dvc.protocol.values.GrantedAuthorization;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import lombok.AllArgsConstructor;
import org.ocpsoft.prettytime.PrettyTime;
import picocli.CommandLine;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@CommandLine.Command(
    name = "grants",
    description = "list granted authorizations of the repository")
@AllArgsConstructor(staticName = "apply")
public final class Repository$Grants extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    @CommandLine.ParentCommand
    private Repository repository;

    public static Repository$Grants apply(CommandLineConsole console) {
        return apply(console, null);
    }

    @Override
    public void run() {
        PrettyTime pt = new PrettyTime();

        CompletionStage<Void> result = getRepository()
            .getRepositoryDetails()
            .thenAccept(details -> console.table(
                Lists.newArrayList("Authorization", "Granted By", "Granted At"),
                Ordering
                    .natural()
                    .reverse()
                    .onResultOf(GrantedAuthorization::getAt)
                    .sortedCopy(details
                        .getAuthorizations()
                        .getAuthorizations())
                    .stream()
                    .map(a -> Lists.newArrayList(
                        a.getAuthorization().toString(),
                        a.getBy().getDisplayName(),
                        pt.format(a.getAt())))
                    .collect(Collectors.toList()),
                true));

        Operators.suppressExceptions(() -> result.toCompletableFuture().get());
    }

    private Repository getRepository() {
        return Optional.ofNullable(repository).orElseThrow(CommandNotInitializedException::apply);
    }

}
