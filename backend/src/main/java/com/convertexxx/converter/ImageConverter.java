package com.convertexxx.converter;

import com.convertexxx.entity.ConversionJob;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ImageConverter implements FileConverter {

    private static final List<String> SUPPORTED_INPUTS = List.of("png", "jpg", "jpeg", "webp", "gif", "bmp");
    private static final List<String> SUPPORTED_OUTPUTS = List.of("png", "jpg", "jpeg", "webp", "pdf");

    @Override
    public boolean supports(String inputFormat, String targetFormat) {
        return SUPPORTED_INPUTS.contains(inputFormat) && SUPPORTED_OUTPUTS.contains(targetFormat);
    }

    @Override
    public void convert(ConversionJob job) throws Exception {
        throw new UnsupportedOperationException("Image conversion to " + job.getTargetFormat() + " is not yet implemented.");
    }
}
