package ada.vcs.client.core.remotes;

public final class HttpRemoteFactory implements RemoteFactory<HttpRemote> {

    @Override
    public Class<HttpRemote> getType() {
        return HttpRemote.class;
    }

    @Override
    public HttpRemote create(HttpRemote properties) {
        return properties;
    }

}
