package com.convertexxx.converter;

import com.convertexxx.entity.ConversionJob;
import com.convertexxx.exception.ConversionException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
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
@Order(1)
public class SplitPdfConverter implements FileConverter {

    private static final Logger log = LoggerFactory.getLogger(SplitPdfConverter.class);

    private static final List<String> SUPPORTED_INPUTS = List.of("pdf");
    private static final List<String> SUPPORTED_OUTPUTS = List.of("zip", "pdf");

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
    public boolean supports(ConversionJob job) {
        if (!SUPPORTED_INPUTS.contains(job.getOriginalFormat().toLowerCase())) {
            return false;
        }
        
        String targetFormat = job.getTargetFormat().toLowerCase();
        
        if (targetFormat.equals("zip")) {
            return true;
        }
        
        if (targetFormat.equals("pdf") && job.getConversionParameters() != null && !job.getConversionParameters().trim().isEmpty()) {
            return true;
        }
        
        return false;
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

        try (PDDocument document = Loader.loadPDF(inputPath.toFile())) {
            int totalPages = document.getNumberOfPages();
            
            if (targetFormat.equals("zip")) {
                try (FileOutputStream fos = new FileOutputStream(outputPath.toFile());
                     ZipOutputStream zos = new ZipOutputStream(fos)) {
                    for (int i = 0; i < totalPages; i++) {
                        try (PDDocument singlePageDoc = new PDDocument()) {
                            singlePageDoc.importPage(document.getPage(i));
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            singlePageDoc.save(baos);
                            ZipEntry zipEntry = new ZipEntry("Page-" + (i + 1) + ".pdf");
                            zos.putNextEntry(zipEntry);
                            zos.write(baos.toByteArray());
                            zos.closeEntry();
                        }
                    }
                }
            } else if (targetFormat.equals("pdf")) {
                List<Integer> pagesToExtract = parsePageSelection(job.getConversionParameters(), totalPages);
                
                try (PDDocument extractedDoc = new PDDocument()) {
                    for (int pageIndex : pagesToExtract) {
                        extractedDoc.importPage(document.getPage(pageIndex));
                    }
                    extractedDoc.save(outputPath.toFile());
                }
            }
        } catch (IllegalArgumentException ex) {
            throw new ConversionException(ex.getMessage(), ex);
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

    private List<Integer> parsePageSelection(String selection, int totalPages) {
        java.util.List<Integer> selectedPages = new java.util.ArrayList<>();
        if (selection == null || selection.trim().isEmpty()) {
            throw new IllegalArgumentException("Page selection cannot be empty");
        }
        
        String[] parts = selection.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) {
                throw new IllegalArgumentException("Malformed page selection: contains empty parts");
            }
            
            if (part.contains("-")) {
                String[] rangeParts = part.split("-");
                if (rangeParts.length != 2) {
                    throw new IllegalArgumentException("Malformed page range: " + part);
                }
                
                try {
                    int startPage = Integer.parseInt(rangeParts[0].trim());
                    int endPage = Integer.parseInt(rangeParts[1].trim());
                    
                    if (startPage <= 0 || endPage <= 0) {
                        throw new IllegalArgumentException("Page numbers must be greater than 0");
                    }
                    if (startPage > endPage) {
                        throw new IllegalArgumentException("Invalid range (start page is greater than end page): " + part);
                    }
                    if (endPage > totalPages) {
                        throw new IllegalArgumentException("Page number " + endPage + " exceeds total pages (" + totalPages + ")");
                    }
                    
                    for (int i = startPage; i <= endPage; i++) {
                        if (selectedPages.contains(i - 1)) {
                            throw new IllegalArgumentException("Duplicate page specified: " + i);
                        }
                        selectedPages.add(i - 1); // 0-indexed internally
                    }
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Malformed page number in range: " + part);
                }
            } else {
                try {
                    int page = Integer.parseInt(part);
                    if (page <= 0) {
                        throw new IllegalArgumentException("Page numbers must be greater than 0");
                    }
                    if (page > totalPages) {
                        throw new IllegalArgumentException("Page number " + page + " exceeds total pages (" + totalPages + ")");
                    }
                    if (selectedPages.contains(page - 1)) {
                        throw new IllegalArgumentException("Duplicate page specified: " + page);
                    }
                    selectedPages.add(page - 1); // 0-indexed internally
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Malformed page number: " + part);
                }
            }
        }
        
        return selectedPages;
    }
}
