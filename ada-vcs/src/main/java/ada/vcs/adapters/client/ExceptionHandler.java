package ada.vcs.adapters.client;

import ada.vcs.adapters.cli.exceptions.ExitWithErrorException;
import ada.vcs.adapters.server.directives.ServerError;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.stream.Materializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public class ExceptionHandler {

    private final ObjectMapper om;

    private final Materializer materializer;

    public CompletionStage<HttpResponse> handle(HttpResponse response) {
        if (!response.status().equals(StatusCodes.OK)) {
            return Jackson
                .unmarshaller(om, ServerError.class)
                .unmarshal(response.entity().withoutSizeLimit(), materializer)
                .thenApply(error -> {
                    throw ExitWithErrorException.apply(error.getMessage());
                })
                .thenApply(i -> response);
        } else {
            return CompletableFuture.completedFuture(response);
        }
    }

}
