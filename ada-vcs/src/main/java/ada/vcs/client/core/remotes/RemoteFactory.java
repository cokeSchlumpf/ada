package ada.vcs.client.core.remotes;

public interface RemoteFactory<T extends RemoteProperties> {

    Class<T> getType();

    Remote create(T properties);

}
