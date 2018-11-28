package ada.cli.testutil;

import ada.cli.configuration.picocli.AdaCommandLineRunner;
import ada.cli.configuration.picocli.ApplicationFactory;
import ada.client.output.Output;
import org.springframework.boot.CommandLineRunner;

public class AdaTestCommandLineRunner extends AdaCommandLineRunner implements CommandLineRunner {

    public AdaTestCommandLineRunner(ApplicationFactory factory, Output out) {
        super(factory, out);
    }

    @Override
    public void run(String... args) {
        if (args == null || args.length == 0) {
            // ignore empty calls
            return;
        }
        try {
            executeCommand(args);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
