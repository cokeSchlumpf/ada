package ada.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

public class AdaServer {

    private final String[] args;

    private ConfigurableApplicationContext ctx;

    private AdaServer(String[] args) {
        this.args = args;
    }

    public static AdaServer apply(String... args) {
        return new AdaServer(args);
    }

    public static void main(String ...args) {
        AdaServer.apply(args).run();
    }

    public void run() {
        ctx = SpringApplication.run(Application.class, args);
    }

    public void start() {
        run();

        while (!ctx.isActive()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        System.out.println("hello world, I have just started up");
    }

    public void stop() {
        if (ctx != null) {
            ctx.stop();
        }
    }

}
