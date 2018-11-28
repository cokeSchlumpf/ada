package ada.client.output;

import java.io.PrintStream;

public class PrintStreamOutput implements Output {

    private final PrintStream printStream;

    private PrintStreamOutput(PrintStream printStream) {
        this.printStream = printStream;
    }

    public static PrintStreamOutput apply(PrintStream printStream) {
        return new PrintStreamOutput(printStream);
    }

    @Override
    public void exception(Throwable e) {
        e.printStackTrace(printStream);
    }

    @Override
    public void message(String message, Object... args) {
        String s = String.format(message, args);
        printStream.println(s);
    }

    @Override
    public void message(String message) {
        printStream.println(message);
    }

    @Override
    public void separator() {
        printStream.println();
        printStream.println("----------");
        printStream.println();
    }

    @Override
    public void table(String[] headers, String[][] content) {
        // TODO: implement when required
    }

    @Override
    public PrintStream getStream() {
        return printStream;
    }

}
