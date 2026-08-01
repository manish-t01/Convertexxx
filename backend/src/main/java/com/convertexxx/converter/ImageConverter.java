package com.convertexxx.converter;

import com.convertexxx.entity.ConversionJob;
import com.convertexxx.exception.ConversionException;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class ImageConverter implements FileConverter {

    private static final Logger log = LoggerFactory.getLogger(ImageConverter.class);

    private static final List<String> SUPPORTED_INPUTS = List.of("jpg", "jpeg", "png", "bmp", "gif");
    private static final List<String> SUPPORTED_OUTPUTS = List.of("jpg", "jpeg", "png", "bmp", "gif");

    private final Path outputDirectory;

    public ImageConverter(@Value("${app.output.directory:converted/}") String outputDir) {
        this.outputDirectory = Paths.get(outputDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.outputDirectory);
        } catch (IOException ex) {
            throw new ConversionException("Could not create the output directory for converted files.", ex);
        }
    }

    @Override
    public boolean supports(String inputFormat, String targetFormat) {
        return SUPPORTED_INPUTS.contains(inputFormat.toLowerCase())
                && SUPPORTED_OUTPUTS.contains(targetFormat.toLowerCase());
    }

    @Override
    public void convert(ConversionJob job) throws Exception {
        Instant start = Instant.now();

        String inputFormat = job.getOriginalFormat().toLowerCase();
        String targetFormat = job.getTargetFormat().toLowerCase();

        log.info("Starting image conversion | jobId={} | originalFile={} | {} -> {}",
                job.getId(), job.getOriginalFileName(), inputFormat, targetFormat);

        // Validate input file exists
        Path inputPath = Paths.get(job.getInputFilePath());
        if (!Files.exists(inputPath)) {
            throw new ConversionException("Input file does not exist: " + inputPath);
        }

        // Validate output format
        if (!SUPPORTED_OUTPUTS.contains(targetFormat)) {
            throw new ConversionException("Unsupported output format: " + targetFormat);
        }

        // Generate output filename and path
        String outputFileName = UUID.randomUUID() + "." + targetFormat;
        Path outputPath = this.outputDirectory.resolve(outputFileName);

        // Ensure output directory exists (in case it was deleted at runtime)
        Files.createDirectories(this.outputDirectory);

        try {
            // Determine the correct output format name for Thumbnailator
            String thumbnailatorFormat = resolveThumbnailatorFormat(targetFormat);

            Thumbnails.of(inputPath.toFile())
                    .scale(1.0)                        // preserve original dimensions
                    .outputQuality(1.0)                // preserve maximum quality
                    .outputFormat(thumbnailatorFormat)
                    .toFile(outputPath.toFile());

        } catch (IOException ex) {
            throw new ConversionException(
                    "Image conversion failed for job " + job.getId() + ": " + ex.getMessage(), ex);
        }

        // Verify output file was created
        if (!Files.exists(outputPath)) {
            throw new ConversionException("Conversion produced no output file for job " + job.getId());
        }

        // Update job with output details
        job.setConvertedFileName(outputFileName);
        job.setOutputFilePath(outputPath.toString());

        Duration duration = Duration.between(start, Instant.now());
        log.info("Image conversion completed | jobId={} | outputFile={} | duration={}ms",
                job.getId(), outputFileName, duration.toMillis());
    }

    /**
     * Maps the user-facing format name to the format string expected by Thumbnailator/ImageIO.
     */
    private String resolveThumbnailatorFormat(String format) {
        return switch (format.toLowerCase()) {
            case "jpg", "jpeg" -> "JPEG";
            case "png" -> "PNG";
            case "bmp" -> "BMP";
            case "gif" -> "GIF";
            default -> throw new ConversionException("Unsupported image output format: " + format);
        };
    }
}
