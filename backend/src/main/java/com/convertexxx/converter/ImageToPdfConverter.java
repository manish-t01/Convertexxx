package com.convertexxx.converter;

import com.convertexxx.entity.ConversionJob;
import com.convertexxx.exception.ConversionException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
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
public class ImageToPdfConverter implements FileConverter {

    private static final Logger log = LoggerFactory.getLogger(ImageToPdfConverter.class);

    private static final List<String> SUPPORTED_INPUTS = List.of("jpg", "jpeg", "png", "bmp", "gif");
    private static final List<String> SUPPORTED_OUTPUTS = List.of("pdf");

    private final Path outputDirectory;

    public ImageToPdfConverter(@Value("${app.output.directory:converted/}") String outputDir) {
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

        log.info("Starting image to pdf conversion | jobId={} | originalFile={} | {} -> {}",
                job.getId(), job.getOriginalFileName(), inputFormat, targetFormat);

        Path inputPath = Paths.get(job.getInputFilePath());
        if (!Files.exists(inputPath)) {
            throw new ConversionException("Input file does not exist: " + inputPath);
        }

        if (!SUPPORTED_OUTPUTS.contains(targetFormat)) {
            throw new ConversionException("Unsupported output format: " + targetFormat);
        }

        String outputFileName = UUID.randomUUID() + "." + targetFormat;
        Path outputPath = this.outputDirectory.resolve(outputFileName);

        Files.createDirectories(this.outputDirectory);

        try (PDDocument document = new PDDocument()) {
            // Using a structured approach to allow for easy multi-image expansion in the future
            addImageToDocument(document, inputPath);

            document.save(outputPath.toFile());
        } catch (IOException ex) {
            throw new ConversionException(
                    "Image to PDF conversion failed for job " + job.getId() + ": " + ex.getMessage(), ex);
        }

        // Verify output file was created and is not empty
        if (!Files.exists(outputPath)) {
            throw new ConversionException("Conversion produced no output file for job " + job.getId());
        }

        try {
            if (Files.size(outputPath) == 0) {
                Files.deleteIfExists(outputPath);
                throw new ConversionException("Conversion produced an empty output file (0 bytes) for job " + job.getId());
            }
        } catch (IOException ex) {
            throw new ConversionException("Failed to verify output file size for job " + job.getId(), ex);
        }

        job.setConvertedFileName(outputFileName);
        job.setOutputFilePath(outputPath.toString());

        Duration duration = Duration.between(start, Instant.now());
        log.info("Image to PDF conversion completed | jobId={} | outputFile={} | duration={}ms",
                job.getId(), outputFileName, duration.toMillis());
    }

    /**
     * Adds an image to the PDDocument, scaling it to fit an A4 page and centering it.
     */
    private void addImageToDocument(PDDocument document, Path imagePath) throws IOException {
        PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath.toString(), document);
        
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        // A4 Dimensions
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();

        // Image Dimensions
        float imageWidth = pdImage.getWidth();
        float imageHeight = pdImage.getHeight();

        // Calculate scaling factor to fit A4 preserving aspect ratio
        float scale = Math.min(pageWidth / imageWidth, pageHeight / imageHeight);
        
        // If the image is smaller than A4, we don't scale it up, we keep it original size.
        // Uncomment the next line if you ONLY want to scale down, but to "fit on A4" generally means scaling up or down to fit maximally.
        // scale = Math.min(1.0f, scale);

        float displayWidth = imageWidth * scale;
        float displayHeight = imageHeight * scale;

        // Calculate centered coordinates
        float startX = (pageWidth - displayWidth) / 2;
        float startY = (pageHeight - displayHeight) / 2;

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.drawImage(pdImage, startX, startY, displayWidth, displayHeight);
        }
    }
}
