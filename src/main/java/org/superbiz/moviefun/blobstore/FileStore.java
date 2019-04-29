package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;

public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        File targetFile = getCoverFile(Long.parseLong(blob.name));

        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            IOUtils.copy(blob.inputStream, outputStream);
        }
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        String coverFileName = format("covers/%s", name);
        File thing = new File(coverFileName);

        if (thing.exists()) {
            Blob fileBlob = new Blob(
                    name,
                    new FileInputStream(thing),
                    new Tika().detect(thing)
            );
            return Optional.of(fileBlob);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void deleteAll() {
        // ...
    }

    private File getCoverFile(long albumId) {
        String coverFileName = format("covers/%d", albumId);
        return new File(coverFileName);
    }

}
