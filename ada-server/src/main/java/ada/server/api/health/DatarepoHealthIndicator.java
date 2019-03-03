package ada.server.api.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DatarepoHealthIndicator implements ReactiveHealthIndicator {

    @Override
    public Mono<Health> health() {
        return checkDownstreamServiceHealth()
            .onErrorResume(
                ex -> Mono.just(new Health.Builder(Status.DOWN).build()));
    }

    private Mono<Health> checkDownstreamServiceHealth() {
        // TODO: Check whether Datarepo is up and running. This is now a Fake.

        return Mono.just(
            new Health.Builder()
                .up()
                .withDetail("Repository Count", 100)
                .withDetail("Storage Available", "1.4G")
                .build());
    }

}
