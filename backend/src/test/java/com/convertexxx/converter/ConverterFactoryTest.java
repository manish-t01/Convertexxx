package com.convertexxx.converter;

import com.convertexxx.entity.ConversionJob;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ConverterFactoryTest {

    private ConverterFactory converterFactory;
    private PdfMergeConverter mergeConverter;
    private CompressPdfConverter compressConverter;
    private Path tempFile;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        mergeConverter = new PdfMergeConverter("target/test-output-merge");
        compressConverter = new CompressPdfConverter();
        // Since CompressPdfConverter relies on property injection, we don't strictly need it to be fully initialized for supports() check, but it works fine.

        converterFactory = new ConverterFactory(Arrays.asList(compressConverter, mergeConverter));

        tempFile = Files.createTempFile("test-compress", ".pdf");
        tempDir = Files.createTempDirectory("test-merge-dir");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
        Files.deleteIfExists(tempDir);
    }

    @Test
    void testMergePdfSelectsMergeConverter() {
        ConversionJob job = new ConversionJob();
        job.setOriginalFormat("pdf");
        job.setTargetFormat("pdf");
        job.setInputFilePath(tempDir.toString());
        job.setConversionParameters(null);

        assertTrue(mergeConverter.supports(job), "Merge converter should support a directory input");
        assertFalse(compressConverter.supports(job), "Compress converter should NOT support a directory input");

        FileConverter selected = converterFactory.getConverter(job);
        assertEquals(PdfMergeConverter.class, selected.getClass(), "ConverterFactory should select PdfMergeConverter for directory inputs");
    }

    @Test
    void testCompressPdfSelectsCompressConverter() {
        ConversionJob job = new ConversionJob();
        job.setOriginalFormat("pdf");
        job.setTargetFormat("pdf");
        job.setInputFilePath(tempFile.toString());
        job.setConversionParameters(null);

        assertFalse(mergeConverter.supports(job), "Merge converter should NOT support a single file input");
        assertTrue(compressConverter.supports(job), "Compress converter should support a single file input");

        FileConverter selected = converterFactory.getConverter(job);
        assertEquals(CompressPdfConverter.class, selected.getClass(), "ConverterFactory should select CompressPdfConverter for single file inputs");
    }

    @Test
    void testBothConvertersNeverMatchSameJob() {
        ConversionJob fileJob = new ConversionJob();
        fileJob.setOriginalFormat("pdf");
        fileJob.setTargetFormat("pdf");
        fileJob.setInputFilePath(tempFile.toString());
        
        boolean compressSupportsFile = compressConverter.supports(fileJob);
        boolean mergeSupportsFile = mergeConverter.supports(fileJob);
        
        assertTrue(compressSupportsFile);
        assertFalse(mergeSupportsFile);

        ConversionJob dirJob = new ConversionJob();
        dirJob.setOriginalFormat("pdf");
        dirJob.setTargetFormat("pdf");
        dirJob.setInputFilePath(tempDir.toString());
        
        boolean compressSupportsDir = compressConverter.supports(dirJob);
        boolean mergeSupportsDir = mergeConverter.supports(dirJob);
        
        assertFalse(compressSupportsDir);
        assertTrue(mergeSupportsDir);
    }
}
