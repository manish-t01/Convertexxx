package com.convertexxx.converter;

import com.convertexxx.entity.ConversionJob;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentConverter implements FileConverter {

    private static final List<String> SUPPORTED_INPUTS = List.of("pdf", "docx", "doc", "pptx", "ppt", "xlsx", "xls", "txt", "csv");
    private static final List<String> SUPPORTED_OUTPUTS = List.of("pdf", "docx", "txt", "png", "jpg");

    @Override
    public boolean supports(String inputFormat, String targetFormat) {
        return SUPPORTED_INPUTS.contains(inputFormat) && SUPPORTED_OUTPUTS.contains(targetFormat);
    }

    @Override
    public void convert(ConversionJob job) throws Exception {
        throw new UnsupportedOperationException("Document conversion to " + job.getTargetFormat() + " is not yet implemented.");
    }
}
