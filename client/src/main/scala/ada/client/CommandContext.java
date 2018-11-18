package ada.client;

import ada.client.output.ClientOutput;

public interface CommandContext {

    ClientOutput getOutput();

    void terminate();

}
