package com.convertexxx.converter.layout;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class TextLine {
    @Builder.Default
    private List<TextSpan> spans = new ArrayList<>();
    
    private float x;
    private float y;
    private float width;
    private float height;

    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (TextSpan span : spans) {
            sb.append(span.getText());
        }
        return sb.toString();
    }
}
