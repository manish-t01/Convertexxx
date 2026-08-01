package com.convertexxx.converter;

import com.convertexxx.entity.ConversionJob;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ArchiveConverter implements FileConverter {

    private static final List<String> SUPPORTED_INPUTS = List.of("zip", "rar", "7z", "tar", "gz");
    private static final List<String> SUPPORTED_OUTPUTS = List.of("zip", "tar", "gz");

    @Override
    public boolean supports(String inputFormat, String targetFormat) {
        return SUPPORTED_INPUTS.contains(inputFormat) && SUPPORTED_OUTPUTS.contains(targetFormat);
    }

    @Override
    public void convert(ConversionJob job) throws Exception {
        throw new UnsupportedOperationException("Archive conversion to " + job.getTargetFormat() + " is not yet implemented.");
    }
}
