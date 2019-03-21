package ada.vcs.client.converters;

import akka.stream.javadsl.AsPublisher;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class CSVSource implements DataSource {

    @Override
    public ReadResult get(boolean incremental) {
        Source
            .single("Hallo")
            .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), null);

        return null;
    }

}
