package ada.vcs.client.converters;

public interface DataSource {

    ReadResult get(boolean incremental);

}
