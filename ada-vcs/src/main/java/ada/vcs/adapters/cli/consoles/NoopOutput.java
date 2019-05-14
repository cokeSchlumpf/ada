package ada.vcs.adapters.cli.consoles;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
public final class NoopOutput implements Output {

    @Override
    public void message(String message, Object... args) {

    }

    @Override
    public void print(String message) {

    }

    @Override
    public void println(String message) {

    }

}
