package ada.vcs.adapters.cli.commands;

import ada.commons.util.ResourceName;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.adapters.cli.consoles.CommandLineConsole;
import ada.vcs.adapters.cli.core.endpoints.Endpoint;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CommandLine.Command(
    name = "endpoints",
    description = "list or add Ada server endpoints",
    subcommands = {
        Endpoints$Add.class
    })
@AllArgsConstructor(staticName = "apply")
public final class Endpoints extends StandardOptions implements Runnable {

    private CommandLineConsole console;

    private CommandContext context;

    @Override
    public void run() {
        context.withAdaHome(home -> {
            List<Endpoint> endpoints = home.getConfiguration().getEndpoints();
            Optional<ResourceName> current = home.getConfiguration().getEndpoint().map(Endpoint::getAlias);


            if (endpoints.size() > 0) {
                console.table(
                    Lists.newArrayList("Alias", "URL"),
                    endpoints
                        .stream()
                        .sorted()
                        .map(endpoint -> Lists.newArrayList(
                            current
                                .filter(e -> endpoint.getAlias().equals(e))
                                .map(i -> "* ")
                                .orElse("  ") + endpoint.getAlias().getValue(),
                            endpoint.getUrl().toString()))
                        .collect(Collectors.toList()),
                    false);
            } else {
                console.message("No endpoints configured.");
            }
        });
    }

}
