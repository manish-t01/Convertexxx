package com.convertexxx.converter;

import com.convertexxx.entity.ConversionJob;
import com.convertexxx.exception.ConversionException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class PdfToImageConverter implements FileConverter {

    private static final Logger log = LoggerFactory.getLogger(PdfToImageConverter.class);

    private static final List<String> SUPPORTED_INPUTS = List.of("pdf");
    private static final List<String> SUPPORTED_OUTPUTS = List.of("png", "jpg", "jpeg");
    private static final int DPI = 300;

    private final Path outputDirectory;

    public PdfToImageConverter(@Value("${app.output.directory:converted/}") String outputDir) {
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
        // Normalize jpeg to jpg for ImageIO writer
        if (targetFormat.equals("jpeg")) {
            targetFormat = "jpg";
        }

        log.info("Starting pdf to image conversion | jobId={} | originalFile={} | {} -> {}",
                job.getId(), job.getOriginalFileName(), inputFormat, targetFormat);

        Path inputPath = Paths.get(job.getInputFilePath());
        if (!Files.exists(inputPath)) {
            throw new ConversionException("Input file does not exist: " + inputPath);
        }

        if (!SUPPORTED_OUTPUTS.contains(targetFormat.toLowerCase()) && !targetFormat.equalsIgnoreCase("jpeg")) {
            throw new ConversionException("Unsupported output format: " + targetFormat);
        }

        Files.createDirectories(this.outputDirectory);

        List<Path> generatedImages = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(inputPath.toFile())) {
            if (document.isEncrypted()) {
                throw new ConversionException("Cannot convert encrypted or password-protected PDF.");
            }

            int numPages = document.getNumberOfPages();
            if (numPages == 0) {
                throw new ConversionException("PDF contains no pages.");
            }

            PDFRenderer pdfRenderer = new PDFRenderer(document);
            String baseFileName = UUID.randomUUID().toString();

            for (int page = 0; page < numPages; page++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, DPI, ImageType.RGB);
                String pageFileName = baseFileName + "_page_" + (page + 1) + "." + targetFormat;
                Path pagePath = this.outputDirectory.resolve(pageFileName);
                
                boolean success = ImageIO.write(image, targetFormat, pagePath.toFile());
                if (!success) {
                    throw new ConversionException("No appropriate image writer found for format: " + targetFormat);
                }

                if (!Files.exists(pagePath) || Files.size(pagePath) == 0) {
                    throw new ConversionException("Generated image for page " + (page + 1) + " is empty or missing.");
                }

                generatedImages.add(pagePath);
            }

            String finalOutputFileName;
            Path finalOutputPath;

            if (numPages == 1) {
                // If only 1 page, return the single image directly
                finalOutputPath = generatedImages.get(0);
                finalOutputFileName = finalOutputPath.getFileName().toString();
            } else {
                // If multiple pages, zip them up
                finalOutputFileName = baseFileName + ".zip";
                finalOutputPath = this.outputDirectory.resolve(finalOutputFileName);
                createZipArchive(generatedImages, finalOutputPath.toFile());
                
                // Cleanup individual images after zipping
                for (Path imgPath : generatedImages) {
                    Files.deleteIfExists(imgPath);
                }
            }

            if (!Files.exists(finalOutputPath) || Files.size(finalOutputPath) == 0) {
                throw new ConversionException("Final output file is empty or missing.");
            }

            job.setConvertedFileName(finalOutputFileName);
            job.setOutputFilePath(finalOutputPath.toString());

            Duration duration = Duration.between(start, Instant.now());
            log.info("PDF to Image conversion completed | jobId={} | outputFile={} | pages={} | duration={}ms",
                    job.getId(), finalOutputFileName, numPages, duration.toMillis());

        } catch (IOException ex) {
            // Clean up any partially generated images if failure occurs
            for (Path imgPath : generatedImages) {
                try {
                    Files.deleteIfExists(imgPath);
                } catch (IOException ignored) {}
            }
            throw new ConversionException("PDF to Image conversion failed: " + ex.getMessage(), ex);
        }
    }

    private void createZipArchive(List<Path> files, File zipFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            for (Path file : files) {
                ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                zos.putNextEntry(zipEntry);
                Files.copy(file, zos);
                zos.closeEntry();
            }
        }
    }
}
