package ada.adapters.cli;

import ada.adapters.cli.util.AbstractAdaTest;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.StreamConverters;
import akka.util.ByteString;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AkkaStreamsISTest extends AbstractAdaTest {

    @Test
    public void test() throws InterruptedException, IOException {

        InputStream inputStream = Source
            .range(1, 1000)
            .map(i -> {
                System.out.println(i);
                return ByteString.fromString(String.valueOf(i));
            })
            .buffer(100, OverflowStrategy.backpressure())
            .runWith(StreamConverters.asInputStream(), getContext().materializer());

        Thread.sleep(5000);
        System.out.println("----");


        BufferedInputStream bis = new BufferedInputStream(inputStream);
        System.out.println(IOUtils.toString(bis, StandardCharsets.UTF_8));

    }

}
