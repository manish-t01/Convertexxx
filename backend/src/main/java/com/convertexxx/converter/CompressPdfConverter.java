package com.convertexxx.converter;

import com.convertexxx.entity.ConversionJob;
import com.convertexxx.exception.ConversionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class CompressPdfConverter implements FileConverter {

    private static final List<String> SUPPORTED_INPUTS = Arrays.asList("pdf");
    private static final String TARGET_FORMAT = "pdf";

    @Value("${conversion.output.dir:uploads}")
    private Path outputDirectory;

    @Override
    public boolean supports(String inputFormat, String targetFormat) {
        // Compress PDF uses full job properties in the default supports(job) method.
        // It cannot be fully determined just by format strings.
        return false;
    }

    @Override
    public boolean supports(ConversionJob job) {
        if (!SUPPORTED_INPUTS.contains(job.getOriginalFormat().toLowerCase())) {
            return false;
        }
        
        String targetFormat = job.getTargetFormat().toLowerCase();
        
        // Compress PDF has target=pdf and empty conversion parameters
        if (targetFormat.equals(TARGET_FORMAT) && 
            (job.getConversionParameters() == null || job.getConversionParameters().trim().isEmpty())) {
            return true;
        }
        
        return false;
    }

    @Override
    public void convert(ConversionJob job) throws Exception {
        Instant start = Instant.now();

        String outputFileName = UUID.randomUUID() + "." + TARGET_FORMAT;
        Path outputPath = this.outputDirectory.resolve(outputFileName);

        Files.createDirectories(this.outputDirectory);

        Path inputPath = Paths.get(job.getInputFilePath());
        if (!Files.exists(inputPath)) {
            throw new ConversionException("Input file does not exist: " + inputPath);
        }

        log.info("Starting Compress PDF conversion | jobId={} | originalFile={} | {} -> {}", 
                job.getId(), job.getOriginalFileName(), job.getOriginalFormat(), TARGET_FORMAT);

        try (PDDocument document = Loader.loadPDF(inputPath.toFile())) {
            
            // Remove unnecessary metadata
            document.setDocumentInformation(new PDDocumentInformation());
            if (document.getDocumentCatalog() != null) {
                document.getDocumentCatalog().setMetadata(null);
            }

            // Save using PDFBox default compression which optimizes streams
            document.save(outputPath.toFile(), CompressParameters.DEFAULT_COMPRESSION);
            
        } catch (IOException e) {
            log.error("Failed to compress PDF | jobId={} | originalFile={}", job.getId(), job.getOriginalFileName(), e);
            throw new ConversionException("Failed to compress PDF: " + e.getMessage(), e);
        }

        // Verify output file was created and is not empty
        if (!Files.exists(outputPath)) {
            throw new ConversionException("Conversion produced no output file for job " + job.getId());
        }

        if (Files.size(outputPath) == 0) {
            Files.deleteIfExists(outputPath);
            throw new ConversionException("Conversion produced an empty output file (0 bytes) for job " + job.getId());
        }

        job.setConvertedFileName(outputFileName);
        job.setOutputFilePath(outputPath.toString());

        Duration duration = Duration.between(start, Instant.now());
        log.info("Compress PDF conversion completed | jobId={} | outputFile={} | duration={}ms", 
                job.getId(), outputFileName, duration.toMillis());
    }
}
