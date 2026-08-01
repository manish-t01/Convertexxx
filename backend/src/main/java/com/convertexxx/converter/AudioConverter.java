package com.convertexxx.converter;

import com.convertexxx.entity.ConversionJob;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AudioConverter implements FileConverter {

    private static final List<String> SUPPORTED_INPUTS = List.of("mp3", "wav", "ogg", "flac");
    private static final List<String> SUPPORTED_OUTPUTS = List.of("mp3", "wav", "ogg", "flac");

    @Override
    public boolean supports(String inputFormat, String targetFormat) {
        return SUPPORTED_INPUTS.contains(inputFormat) && SUPPORTED_OUTPUTS.contains(targetFormat);
    }

    @Override
    public void convert(ConversionJob job) throws Exception {
        throw new UnsupportedOperationException("Audio conversion to " + job.getTargetFormat() + " is not yet implemented.");
    }
}
