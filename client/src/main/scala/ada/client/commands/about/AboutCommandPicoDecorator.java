package ada.client.commands.about;

import picocli.CommandLine;

@CommandLine.Command(
        name = "about",
        mixinStandardHelpOptions = true,
        description = "Show information about Ada")
public class AboutCommandPicoDecorator implements AboutCommand, Runnable {

    private final AboutCommand cmd;

    private AboutCommandPicoDecorator(AboutCommand cmd) {
        this.cmd = cmd;
    }

    public static AboutCommandPicoDecorator apply(AboutCommand cmd) {
        return new AboutCommandPicoDecorator(cmd);
    }

    @Override
    public void run() {
        cmd.run();
    }

}
