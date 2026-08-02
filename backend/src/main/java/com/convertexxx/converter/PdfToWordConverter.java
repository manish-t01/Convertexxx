package com.convertexxx.converter;

import com.convertexxx.entity.ConversionJob;
import com.convertexxx.exception.ConversionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import com.convertexxx.converter.layout.DocumentLayout;
import com.convertexxx.converter.layout.LayoutAnalyzer;
import com.convertexxx.converter.render.WordRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
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
public class PdfToWordConverter implements FileConverter {

    private static final List<String> SUPPORTED_INPUTS = Arrays.asList("pdf");
    private static final String TARGET_FORMAT = "docx";

    @Value("${conversion.output.dir:uploads}")
    private Path outputDirectory;

    private final LayoutAnalyzer layoutAnalyzer;
    private final WordRenderer wordRenderer;

    @Autowired
    public PdfToWordConverter(LayoutAnalyzer layoutAnalyzer, WordRenderer wordRenderer) {
        this.layoutAnalyzer = layoutAnalyzer;
        this.wordRenderer = wordRenderer;
    }

    @Override
    public boolean supports(String inputFormat, String targetFormat) {
        if (inputFormat == null || targetFormat == null) return false;
        return SUPPORTED_INPUTS.contains(inputFormat.toLowerCase()) && 
               targetFormat.toLowerCase().equals(TARGET_FORMAT);
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

        log.info("Starting PDF to Word conversion | jobId={} | originalFile={}", job.getId(), job.getOriginalFileName());

        try (PDDocument document = Loader.loadPDF(inputPath.toFile());
             FileOutputStream out = new FileOutputStream(outputPath.toFile())) {

            // 1. Analyze PDF into a renderer-independent DocumentLayout
            DocumentLayout documentLayout = layoutAnalyzer.analyze(document);

            // 2. Render DocumentLayout into Word DOCX via WordRenderer
            wordRenderer.render(documentLayout, out);

        } catch (IOException e) {
            log.error("Failed to convert PDF to Word | jobId={} | originalFile={}", job.getId(), job.getOriginalFileName(), e);
            throw new ConversionException("Failed to convert PDF to Word: " + e.getMessage(), e);
        }

        // Verify output file was created and is not empty
        if (!Files.exists(outputPath) || Files.size(outputPath) == 0) {
            Files.deleteIfExists(outputPath);
            throw new ConversionException("Conversion produced empty or no output file for job " + job.getId());
        }

        String originalFileName = job.getOriginalFileName();
        String baseName = originalFileName;
        if (originalFileName != null && originalFileName.contains(".")) {
            baseName = originalFileName.substring(0, originalFileName.lastIndexOf("."));
        }
        
        job.setConvertedFileName(baseName + "." + TARGET_FORMAT);
        job.setOutputFilePath(outputPath.toString());

        Duration duration = Duration.between(start, Instant.now());
        log.info("PDF to Word conversion completed | jobId={} | outputFile={} | duration={}ms", 
                 job.getId(), outputFileName, duration.toMillis());
    }
}
