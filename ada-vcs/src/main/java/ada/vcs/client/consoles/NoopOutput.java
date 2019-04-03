package ada.vcs.client.consoles;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
public class NoopOutput implements Output {

    @Override
    public void message(String message, Object... args) {

    }

}