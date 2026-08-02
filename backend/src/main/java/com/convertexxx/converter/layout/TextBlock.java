package com.convertexxx.converter.layout;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class TextBlock implements PageElement {
    public enum Alignment { LEFT, CENTER, RIGHT, JUSTIFY }

    @Builder.Default
    private List<TextLine> lines = new ArrayList<>();
    
    private boolean isHeading;
    private Alignment alignment;
    
    private float x;
    private float y;
    private float width;
    private float height;
}
