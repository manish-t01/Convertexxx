package com.convertexxx.converter;

import com.convertexxx.entity.ConversionJob;

public interface FileConverter {
    
    /**
     * Determines if this converter supports the given input and target formats.
     */
    boolean supports(String inputFormat, String targetFormat);

    /**
     * Executes the conversion process.
     */
    void convert(ConversionJob job) throws Exception;
}
