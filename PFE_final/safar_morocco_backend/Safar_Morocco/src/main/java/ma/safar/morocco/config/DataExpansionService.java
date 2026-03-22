package ma.safar.morocco.config;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.destination.repository.DestinationRepository;
import ma.safar.morocco.event.repository.EvenementCulturelRepository;
import ma.safar.morocco.media.entity.Media;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class DataExpansionService {
    private static final Logger log = LoggerFactory.getLogger(DataExpansionService.class);
    private static final String UPLOAD_DIR = "uploads";
    private static final String WEBP_EXT = ".webp";
    private static final String PNG_EXT = ".png";
    private static final String JPG_EXT = ".jpg";
    private static final String UPLOADS_PREFIX = "/uploads/";

    private final DestinationRepository destinationRepository;
    private final EvenementCulturelRepository eventRepository;

    @Transactional
    public void repairData() {
        log.info("🔧 Starting Data Repair and Image Synchronization...");
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            syncImagesAndCleanup();
        } catch (Exception e) {
            log.error("❌ Error during data repair: {}", e.getMessage());
        }
    }

    private void syncImagesAndCleanup() {
        java.util.Set<String> referencedFiles = new java.util.HashSet<>();

        // 1. Sync Event Images
        syncEventImages(referencedFiles);

        // 2. Sync Destination Media Images
        syncDestinationMedias(referencedFiles);

        // 3. Cleanup unused files in uploads/
        cleanupUnusedFiles(referencedFiles);
    }

    private String getExtensionFromUrl(String url) {
        if (url.contains(PNG_EXT)) return PNG_EXT;
        if (url.contains(WEBP_EXT)) return WEBP_EXT;
        return JPG_EXT;
    }

    private void syncEventImages(java.util.Set<String> referencedFiles) {
        eventRepository.findAll().forEach(e -> {
            String url = e.getImageUrl();
            if (url == null)
                return;

            if (url.startsWith("http")) {
                // External URL - Download it
                String extension = getExtensionFromUrl(url);
                String filename = "event_" + e.getNom().toLowerCase().replaceAll("[^a-z0-9]", "_") + extension;
                String downloaded = downloadImage(url, filename, true);
                if (downloaded != null) {
                    e.setImageUrl(UPLOADS_PREFIX + downloaded);
                    eventRepository.save(e);
                    referencedFiles.add(downloaded);
                }
            } else if (url.startsWith(UPLOADS_PREFIX)) {
                String filename = url.substring(UPLOADS_PREFIX.length());
                referencedFiles.add(filename);
            }
        });
    }

    private void syncDestinationMedias(java.util.Set<String> referencedFiles) {
        destinationRepository.findAllWithMedias().forEach(d -> {
            boolean changed = false;
            for (Media m : d.getMedias()) {
                String url = m.getUrl();
                if (url == null)
                    continue;

                if (url.startsWith("http")) {
                    String extension = getExtensionFromUrl(url);
                    String filename = "dest_" + d.getNom().toLowerCase().replaceAll("[^a-z0-9]", "_") + "_" + m.getId()
                            + extension;
                    String downloaded = downloadImage(url, filename, true);
                    if (downloaded != null) {
                        m.setUrl(UPLOADS_PREFIX + downloaded);
                        referencedFiles.add(downloaded);
                        changed = true;
                    }
                } else if (url.startsWith(UPLOADS_PREFIX)) {
                    String filename = url.substring(UPLOADS_PREFIX.length());
                    referencedFiles.add(filename);
                }
            }
            if (changed) {
                destinationRepository.save(d);
            }
        });
    }

    private void cleanupUnusedFiles(java.util.Set<String> referencedFiles) {
        log.info("🧹 Starting cleanup of unused images...");
        try (java.util.stream.Stream<Path> stream = Files.list(Paths.get(UPLOAD_DIR))) {
            stream.forEach(path -> {
                if (Files.isDirectory(path))
                    return; // Keep subdirectories like 'invoices' or 'media'

                String filename = path.getFileName().toString();
                // Special case for marker or essential files
                if (filename.equals("marker_workspace.txt"))
                    return;

                if (!referencedFiles.contains(filename)) {
                    try {
                        Files.delete(path);
                        log.info("🗑️ Deleted unused image: {}", filename);
                    } catch (java.io.IOException e) {
                        log.error("❌ Failed to delete {}: {}", filename, e.getMessage());
                    }
                }
            });
        } catch (java.io.IOException e) {
            log.error("❌ Error listing uploads directory: {}", e.getMessage());
        }
    }

    public String downloadImage(String urlStr, String targetFilename, boolean force) {
        try {
            Path targetPath = Paths.get(UPLOAD_DIR, targetFilename);
            if (!force && Files.exists(targetPath)) {
                return targetFilename;
            }

            log.info("📥 Downloading image from: {}", urlStr);
            URL url = java.net.URI.create(urlStr).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (java.io.InputStream in = connection.getInputStream()) {
                    Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    log.info("✅ Successfully saved: {}", targetFilename);
                    return targetFilename;
                }
            } else {
                log.warn("⚠️ Failed to download image. HTTP Response: {}", connection.getResponseCode());
            }
        } catch (Exception e) {
            log.error("❌ Download failed for {}: {}", targetFilename, e.getMessage());
        }
        return null;
    }

    // Unused but kept for compatibility if needed elsewhere
    public String downloadImage(String urlStr, String targetFilename) {
        return downloadImage(urlStr, targetFilename, false);
    }
}
