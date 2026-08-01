package com.convertexxx.converter;

import com.convertexxx.entity.ConversionJob;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConverterFactory {

    private final List<FileConverter> converters;

    public ConverterFactory(List<FileConverter> converters) {
        this.converters = converters;
    }

    public FileConverter getConverter(ConversionJob job) {
        for (FileConverter converter : converters) {
            if (converter.supports(job)) {
                return converter;
            }
        }
        throw new UnsupportedOperationException("No converter found for " + job.getOriginalFormat() + " to " + job.getTargetFormat());
    }
}
