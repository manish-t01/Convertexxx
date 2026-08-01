package com.convertexxx.converter;

import com.convertexxx.entity.ConversionJob;
import com.convertexxx.exception.ConversionException;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
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
public class PdfMergeConverter implements FileConverter {

    private static final Logger log = LoggerFactory.getLogger(PdfMergeConverter.class);

    private static final List<String> SUPPORTED_INPUTS = List.of("pdf");
    private static final List<String> SUPPORTED_OUTPUTS = List.of("pdf");

    private final Path outputDirectory;

    public PdfMergeConverter(@Value("${app.output.directory:converted/}") String outputDir) {
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

        log.info("Starting PDF merge conversion | jobId={} | originalFile={} | {} -> {}",
                job.getId(), job.getOriginalFileName(), inputFormat, targetFormat);

        Path inputPath = Paths.get(job.getInputFilePath());
        if (!Files.exists(inputPath)) {
            throw new ConversionException("Input file or directory does not exist: " + inputPath);
        }

        if (!SUPPORTED_OUTPUTS.contains(targetFormat)) {
            throw new ConversionException("Unsupported output format: " + targetFormat);
        }

        String outputFileName = UUID.randomUUID() + "." + targetFormat;
        Path outputPath = this.outputDirectory.resolve(outputFileName);

        Files.createDirectories(this.outputDirectory);

        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        pdfMerger.setDestinationFileName(outputPath.toString());

        try {
            if (Files.isDirectory(inputPath)) {
                try (var stream = Files.list(inputPath)) {
                    List<Path> files = stream.sorted().toList();
                    for (Path file : files) {
                        pdfMerger.addSource(file.toFile());
                    }
                }
            } else {
                pdfMerger.addSource(inputPath.toFile());
            }

            pdfMerger.mergeDocuments(null);
        } catch (IOException ex) {
            throw new ConversionException(
                    "PDF Merge failed for job " + job.getId() + ": " + ex.getMessage(), ex);
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
        log.info("PDF Merge conversion completed | jobId={} | outputFile={} | duration={}ms",
                job.getId(), outputFileName, duration.toMillis());
    }
}
