package com.convertexxx.converter;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConverterFactory {

    private final List<FileConverter> converters;

    public ConverterFactory(List<FileConverter> converters) {
        this.converters = converters;
    }

    public FileConverter getConverter(String inputFormat, String targetFormat) {
        for (FileConverter converter : converters) {
            if (converter.supports(inputFormat, targetFormat)) {
                return converter;
            }
        }
        throw new UnsupportedOperationException("No converter found for " + inputFormat + " to " + targetFormat);
    }
}
