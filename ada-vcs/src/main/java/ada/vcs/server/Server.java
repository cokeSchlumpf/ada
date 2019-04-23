package ada.vcs.server;

import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import akka.stream.IOResult;
import akka.stream.javadsl.Compression;
import akka.stream.javadsl.FileIO;

import java.io.File;
import java.util.concurrent.CompletionStage;

public final class Server extends HttpApp {

    @Override
    protected Route routes() {
        return path("hello", () ->
            concat(
                get(() ->
                    complete(HttpEntities.create(ContentTypes.TEXT_HTML_UTF8, "<h1>Say hello to akka-http</h1>"))),
                put(() ->
                    extractMaterializer(materializer ->
                        extractDataBytes(source -> {
                            CompletionStage<IOResult> result = source
                                //.via(Compression.gunzip(1024))
                                .runWith(
                                    FileIO.toFile(new File("/Users/michael/Downloads/akka-http-tests/out-2.gz")),
                                    materializer);

                            return onSuccess(result, r -> complete(r.toString()));
                        })
                    )
                )
            )
        );
    }

}
