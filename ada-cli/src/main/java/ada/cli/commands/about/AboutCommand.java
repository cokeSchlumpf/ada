package ada.cli.commands.about;

import ada.cli.commands.StandardOptions;
import ada.cli.consoles.CommandLineConsole;
import picocli.CommandLine;

@CommandLine.Command(
    name = "about",
    description = "shows information about Ada")
public class AboutCommand extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private AboutCommand(CommandLineConsole console) {
        this.console = console;
    }

    public static AboutCommand apply(CommandLineConsole console) {
        return new AboutCommand(console);
    }

    @Override
    public void run() {
        String out = "                 _       \n" +
            "        /\\      | |      \n" +
            "       /  \\   __| | __ _ \n" +
            "      / /\\ \\ / _` |/ _` |\n" +
            "     / ____ \\ (_| | (_| |\n" +
            "====/_/====\\_\\__,_|\\__,_|====\n" +
            "=============================\n" +
            ":: IBM Ada CLI :: %s ::";

        String version = "0.0.42"; // TODO: Inject during build.

        console.message(out, version);
    }

}
