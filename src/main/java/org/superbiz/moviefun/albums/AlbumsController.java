package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;
import sun.misc.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private BlobStore blobStore;

    public AlbumsController(
            AlbumsBean albumsBean,
            BlobStore blobStore
    ) {
        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(
            @PathVariable long albumId,
            @RequestParam("file") MultipartFile uploadedFile
    ) throws IOException {
        Blob uploadBlob = new Blob(
                Long.toString(albumId),
                new ByteArrayInputStream(uploadedFile.getBytes()),
                uploadedFile.getContentType()
        );
        blobStore.put(uploadBlob);

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(
            @PathVariable long albumId
    ) throws IOException, URISyntaxException {

        Optional<Blob> blobOptional = blobStore.get(Long.toString(albumId));
        byte[] imageBytes;
        String contentType;

        if (blobOptional.isPresent()) {
            imageBytes = IOUtils.readFully(blobOptional.get().inputStream, -1, true);
            contentType = blobOptional.get().contentType;
        } else {
            URI resourceURI = this.getClass().getClassLoader().getResource("default-cover.jpg").toURI();
            Path imagePath = Paths.get(resourceURI);
            imageBytes = readAllBytes(imagePath);
            contentType = new Tika().detect(imagePath);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(contentType));
        headers.setContentLength(imageBytes.length);

        return new HttpEntity<>(imageBytes, headers);
    }
}
