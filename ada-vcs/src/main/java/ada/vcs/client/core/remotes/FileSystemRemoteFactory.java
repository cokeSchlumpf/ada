package ada.vcs.client.core.remotes;

public final class FileSystemRemoteFactory implements RemoteFactory<FileSystemRemote> {

    @Override
    public Class<FileSystemRemote> getType() {
        return FileSystemRemote.class;
    }

    @Override
    public FileSystemRemote create(FileSystemRemote properties) {
        return properties;
    }

}
