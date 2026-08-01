package com.convertexxx.converter;

import com.convertexxx.entity.ConversionJob;
import com.convertexxx.exception.ConversionException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class SplitPdfConverter implements FileConverter {

    private static final Logger log = LoggerFactory.getLogger(SplitPdfConverter.class);

    private static final List<String> SUPPORTED_INPUTS = List.of("pdf");
    private static final List<String> SUPPORTED_OUTPUTS = List.of("zip");

    private final Path outputDirectory;

    public SplitPdfConverter(@Value("${app.output.directory:converted/}") String outputDir) {
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

        log.info("Starting Split PDF conversion | jobId={} | originalFile={} | {} -> {}",
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

        try (PDDocument document = Loader.loadPDF(inputPath.toFile());
             FileOutputStream fos = new FileOutputStream(outputPath.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            int totalPages = document.getNumberOfPages();
            
            for (int i = 0; i < totalPages; i++) {
                try (PDDocument singlePageDoc = new PDDocument()) {
                    singlePageDoc.addPage(document.getPage(i));
                    
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    singlePageDoc.save(baos);
                    
                    ZipEntry zipEntry = new ZipEntry("Page-" + (i + 1) + ".pdf");
                    zos.putNextEntry(zipEntry);
                    zos.write(baos.toByteArray());
                    zos.closeEntry();
                }
            }

        } catch (IOException ex) {
            throw new ConversionException(
                    "Split PDF conversion failed for job " + job.getId() + ": " + ex.getMessage(), ex);
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
        log.info("Split PDF conversion completed | jobId={} | outputFile={} | duration={}ms",
                job.getId(), outputFileName, duration.toMillis());
    }
}
