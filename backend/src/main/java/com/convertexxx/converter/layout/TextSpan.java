package com.convertexxx.converter.layout;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TextSpan {
    private String text;
    private String fontFamily;
    private float fontSize;
    private boolean isBold;
    private boolean isItalic;
    
    private float x;
    private float y;
    private float width;
    private float height;
}
