package com.convertexxx.converter;

import com.convertexxx.entity.ConversionJob;
import com.convertexxx.exception.ConversionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class CompressPdfConverter implements FileConverter {

    private static final List<String> SUPPORTED_INPUTS = Arrays.asList("pdf");
    private static final String TARGET_FORMAT = "pdf";

    // Configurable constants
    private static final long JPEG_RECOMPRESS_THRESHOLD = 500_000; // ~0.5 MP
    private static final long DOWNSAMPLE_THRESHOLD = 2_000_000; // ~2 MP
    private static final float JPEG_QUALITY = 0.75f;

    @Value("${conversion.output.dir:uploads}")
    private Path outputDirectory;

    @Override
    public boolean supports(String inputFormat, String targetFormat) {
        return false;
    }

    @Override
    public boolean supports(ConversionJob job) {
        if (!SUPPORTED_INPUTS.contains(job.getOriginalFormat().toLowerCase())) {
            return false;
        }

        String targetFormat = job.getTargetFormat().toLowerCase();
        
        if (targetFormat.equals(TARGET_FORMAT) && 
            (job.getConversionParameters() == null || job.getConversionParameters().trim().isEmpty())) {
            
            if (job.getInputFilePath() == null) {
                return false;
            }
            Path inputPath = Paths.get(job.getInputFilePath());
            return !Files.isDirectory(inputPath);
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

        log.info("Starting Compress PDF conversion | jobId={} | originalFile={}", job.getId(), job.getOriginalFileName());

        try (PDDocument document = Loader.loadPDF(inputPath.toFile())) {
            
            // Step 1: Remove unnecessary metadata
            document.setDocumentInformation(new PDDocumentInformation());
            if (document.getDocumentCatalog() != null) {
                document.getDocumentCatalog().setMetadata(null);
            }

            // Step 2-9: Intelligent Image Compression
            Set<String> processedImages = new HashSet<>();
            int compressedImagesCount = 0;

            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();
                if (resources == null) continue;

                for (COSName xObjectName : resources.getXObjectNames()) {
                    try {
                        PDXObject xObject = resources.getXObject(xObjectName);
                        if (xObject instanceof PDImageXObject) {
                            PDImageXObject image = (PDImageXObject) xObject;
                            
                            // Skip unsupported/transparent images
                            if (image.getMask() != null || image.getSoftMask() != null || image.isStencil()) {
                                continue;
                            }

                            // Avoid duplicate work
                            String cosKey = String.valueOf(image.getCOSObject().hashCode());
                            if (processedImages.contains(cosKey)) {
                                continue;
                            }
                            processedImages.add(cosKey);

                            int width = image.getWidth();
                            int height = image.getHeight();
                            long pixels = (long) width * height;

                            if (pixels > JPEG_RECOMPRESS_THRESHOLD) {
                                BufferedImage bImage = image.getImage();
                                boolean downsampled = false;
                                
                                // Downsample if exceeding threshold
                                if (pixels > DOWNSAMPLE_THRESHOLD) {
                                    double scale = Math.sqrt((double) DOWNSAMPLE_THRESHOLD / pixels);
                                    int newW = (int) (width * scale);
                                    int newH = (int) (height * scale);
                                    
                                    Image scaled = bImage.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                                    BufferedImage newBimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
                                    Graphics2D g2d = newBimg.createGraphics();
                                    g2d.drawImage(scaled, 0, 0, null);
                                    g2d.dispose();
                                    
                                    bImage = newBimg;
                                    downsampled = true;
                                }

                                // Avoid converting grayscale/monochrome to RGB if we didn't downsample
                                if (!downsampled && bImage.getType() != BufferedImage.TYPE_BYTE_GRAY 
                                        && bImage.getType() != BufferedImage.TYPE_BYTE_BINARY) {
                                    // Ensure RGB format for JPEG encoding if not grayscale
                                    if (bImage.getType() != BufferedImage.TYPE_INT_RGB) {
                                        BufferedImage newBimg = new BufferedImage(bImage.getWidth(), bImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                                        Graphics2D g2d = newBimg.createGraphics();
                                        g2d.drawImage(bImage, 0, 0, null);
                                        g2d.dispose();
                                        bImage = newBimg;
                                    }
                                }
                                
                                // Re-encode and compare sizes
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                // ImageIO can write grayscale as JPEG as well
                                boolean wrote = ImageIO.write(bImage, "jpeg", baos); // Wait, we can use JPEGFactory
                                // JPEGFactory allows setting quality
                                PDImageXObject newImage = JPEGFactory.createFromImage(document, bImage, JPEG_QUALITY);
                                
                                int originalSize = image.getCOSObject().getInt(COSName.LENGTH, Integer.MAX_VALUE);
                                int newSize = newImage.getCOSObject().getInt(COSName.LENGTH, 0);

                                if (newSize < originalSize) {
                                    resources.put(xObjectName, newImage);
                                    compressedImagesCount++;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to process image on page in job {}: {}", job.getId(), e.getMessage());
                        // Keep original image and continue
                    }
                }
            }

            log.info("Compressed {} images in job {}", compressedImagesCount, job.getId());

            // Save using PDFBox default compression
            document.save(outputPath.toFile(), CompressParameters.DEFAULT_COMPRESSION);
            
        } catch (IOException e) {
            log.error("Failed to compress PDF | jobId={} | originalFile={}", job.getId(), job.getOriginalFileName(), e);
            throw new ConversionException("Failed to compress PDF: " + e.getMessage(), e);
        }

        if (!Files.exists(outputPath) || Files.size(outputPath) == 0) {
            Files.deleteIfExists(outputPath);
            throw new ConversionException("Conversion produced empty or no output file for job " + job.getId());
        }

        job.setConvertedFileName(outputFileName);
        job.setOutputFilePath(outputPath.toString());

        Duration duration = Duration.between(start, Instant.now());
        log.info("Compress PDF conversion completed | jobId={} | duration={}ms", job.getId(), duration.toMillis());
    }
}
