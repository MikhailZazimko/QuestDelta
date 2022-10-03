package ua.com.javarush.quest.khmelov.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ImageService {

    public static final String ROOT = "/";
    public static final String IMAGES_FOLDER = "images";
    public static final String PART_NAME = "image";
    public static final String FILENAME_PREFIX = "image-";
    public static final String NO_IMAGE_PNG = "no-image.png";
    public static final List<String> EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".bmp", ".gif", ".webp");
    private final Path imagesFolder = Path.of(Objects.requireNonNull(
                    ImageService.class.getResource(ROOT)
            ).getPath())
            .getParent()
            .resolve(IMAGES_FOLDER);


    @SneakyThrows
    public ImageService() {
        Files.createDirectories(imagesFolder);
    }


    @SneakyThrows
    public Optional<Path> getImagePath(String filename) {
        return EXTENSIONS.stream()
                .map(ext -> imagesFolder.resolve(filename + ext))
                .filter(Files::exists)
                .findAny()
                .or(() -> Optional.of(imagesFolder.resolve(NO_IMAGE_PNG)));
    }

    public void uploadImage(HttpServletRequest req) throws IOException, ServletException {
        Part data = req.getPart(PART_NAME);
        //todo add a file extension
        if (data.getInputStream().available() > 0) {
            String id = req.getParameter("id");
            String filename = data.getSubmittedFileName();
            String ext = filename.substring(filename.lastIndexOf("."));
            deleteOldFiles(FILENAME_PREFIX + id);
            filename = FILENAME_PREFIX + id + ext;
            uploadImageInternal(filename, data.getInputStream());
        }
    }

    private void deleteOldFiles(String filename) {
        EXTENSIONS.stream()
                .map(ext -> imagesFolder.resolve(filename + ext))
                .filter(Files::exists)
                .forEach(p-> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @SneakyThrows
    private void uploadImageInternal(String name, InputStream data) {
        try (data) {
            if (data.available() > 0) {
                Files.copy(data, imagesFolder.resolve(name), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

}