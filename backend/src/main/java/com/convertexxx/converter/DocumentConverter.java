package com.convertexxx.converter;

import com.convertexxx.entity.ConversionJob;
import com.convertexxx.exception.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class DocumentConverter implements FileConverter {

    private static final Logger log = LoggerFactory.getLogger(DocumentConverter.class);

    private static final List<String> SUPPORTED_INPUTS = List.of(
            "doc", "docx", "odt", "rtf",
            "ppt", "pptx", "odp",
            "xls", "xlsx", "ods"
    );

    private static final List<String> SUPPORTED_OUTPUTS = List.of("pdf");

    private static final long TIMEOUT_MINUTES = 5;

    private final String libreOfficeExecutable;
    private final Path outputDirectory;

    public DocumentConverter(
            @Value("${app.libreoffice.executable:soffice}") String libreOfficeExecutable,
            @Value("${app.output.directory:converted/}") String outputDir
    ) {
        this.libreOfficeExecutable = libreOfficeExecutable;
        this.outputDirectory = Paths.get(outputDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.outputDirectory);
        } catch (IOException ex) {
            throw new ConversionException("Could not create the output directory for converted documents.", ex);
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

        log.info("Starting document conversion | jobId={} | originalFile={} | {} -> {}",
                job.getId(), job.getOriginalFileName(), inputFormat, targetFormat);

        // Validate input file exists
        Path inputPath = Paths.get(job.getInputFilePath());
        if (!Files.exists(inputPath)) {
            throw new ConversionException("Input file does not exist: " + inputPath);
        }

        // Validate output format
        if (!SUPPORTED_OUTPUTS.contains(targetFormat)) {
            throw new ConversionException("Unsupported document output format: " + targetFormat);
        }

        // Ensure output directory exists (in case it was deleted at runtime)
        Files.createDirectories(this.outputDirectory);

        // LibreOffice names output after the input file's base name.
        // Copy the input to a UUID-named temp file to ensure thread-safe output naming.
        String uniqueBaseName = UUID.randomUUID().toString();
        Path tempInputFile = this.outputDirectory.resolve(uniqueBaseName + "." + inputFormat);
        Files.copy(inputPath, tempInputFile);

        try {
            // Build the LibreOffice command
            List<String> command = List.of(
                    libreOfficeExecutable,
                    "--headless",
                    "--convert-to", targetFormat,
                    "--outdir", this.outputDirectory.toString(),
                    tempInputFile.toString()
            );

            log.info("Executing LibreOffice | jobId={} | command={}", job.getId(), String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            // Capture stdout and stderr concurrently to prevent buffer deadlocks
            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();

            Thread stdoutReader = new Thread(() -> readStream(process.getInputStream(), stdout), "lo-stdout-" + job.getId());
            Thread stderrReader = new Thread(() -> readStream(process.getErrorStream(), stderr), "lo-stderr-" + job.getId());
            stdoutReader.start();
            stderrReader.start();

            boolean completed = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);

            // Allow reader threads a moment to finish draining
            stdoutReader.join(2000);
            stderrReader.join(2000);

            if (!completed) {
                process.destroyForcibly();
                throw new ConversionException(
                        "Document conversion timed out after " + TIMEOUT_MINUTES + " minutes for job " + job.getId()
                                + ". stderr: " + stderr.toString().trim());
            }

            int exitCode = process.exitValue();
            log.info("LibreOffice exited | jobId={} | exitCode={} | stdout={} | stderr={}",
                    job.getId(), exitCode, stdout.toString().trim(), stderr.toString().trim());

            if (exitCode != 0) {
                String errorDetail = stderr.toString().trim();
                if (errorDetail.isEmpty()) {
                    errorDetail = stdout.toString().trim();
                }
                throw new ConversionException(
                        "LibreOffice exited with code " + exitCode + " for job " + job.getId()
                                + ". Output: " + errorDetail);
            }

            // LibreOffice produces: <outputDir>/<uniqueBaseName>.pdf
            Path libreOfficeOutput = this.outputDirectory.resolve(uniqueBaseName + "." + targetFormat);

            if (!Files.exists(libreOfficeOutput)) {
                throw new ConversionException(
                        "Conversion produced no output file for job " + job.getId()
                                + ". Expected: " + libreOfficeOutput);
            }

            if (Files.size(libreOfficeOutput) == 0) {
                Files.deleteIfExists(libreOfficeOutput);
                throw new ConversionException(
                        "Conversion produced an empty output file (0 bytes) for job " + job.getId());
            }

            // Rename to a final unique name to keep the output directory clean
            String outputFileName = UUID.randomUUID() + "." + targetFormat;
            Path finalOutputPath = this.outputDirectory.resolve(outputFileName);
            Files.move(libreOfficeOutput, finalOutputPath);

            // Update job with output details (in-memory only — ConversionService handles persistence)
            job.setConvertedFileName(outputFileName);
            job.setOutputFilePath(finalOutputPath.toString());

            Duration duration = Duration.between(start, Instant.now());
            log.info("Document conversion completed | jobId={} | outputFile={} | duration={}ms",
                    job.getId(), outputFileName, duration.toMillis());

        } finally {
            // Clean up the temporary copy of the input file
            Files.deleteIfExists(tempInputFile);
        }
    }

    /**
     * Reads all lines from the given input stream into the provided StringBuilder.
     * Designed to be run on a dedicated thread to prevent process buffer deadlocks.
     */
    private void readStream(java.io.InputStream inputStream, StringBuilder output) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        } catch (IOException ex) {
            log.warn("Error reading process stream", ex);
        }
    }
}
