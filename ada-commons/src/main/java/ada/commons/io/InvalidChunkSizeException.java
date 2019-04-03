package ada.commons.io;

import ada.commons.util.FileSize;

public final class InvalidChunkSizeException extends IllegalArgumentException {

    private static final String TOO_SMALL_MESSAGE = "The chunk size %s %s is invalid, needs to be higher than %s %s.";
    private static final String TOO_LARGE_MESSAGE = "The chunk size %s %s is invalid, needs to be less or equal the file size %s %s.";

    private FileSize size;

    private InvalidChunkSizeException(FileSize chunkSize, String message) {
        super(message);
        this.size = chunkSize;
    }

    public static InvalidChunkSizeException tooSmall(FileSize chunkSize, FileSize minSize) {
        String message = String.format(
            TOO_SMALL_MESSAGE,
            chunkSize.getSize(), chunkSize.getUnit().getName(),
            minSize.getSize(), minSize.getUnit().getName());

        return new InvalidChunkSizeException(chunkSize, message);
    }

    public static InvalidChunkSizeException tooLarge(FileSize chunkSize, FileSize fileSize) {
        String message = String.format(
            TOO_LARGE_MESSAGE,
            chunkSize.getSize(), chunkSize.getUnit().getName(),
            fileSize.getSize(), fileSize.getUnit().getName());

        return new InvalidChunkSizeException(chunkSize, message);
    }

    public FileSize getSize() {
        return size;
    }
}
