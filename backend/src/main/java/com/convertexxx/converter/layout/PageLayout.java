package com.convertexxx.converter.layout;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class PageLayout {
    private int pageNumber;
    private float width;
    private float height;
    
    @Builder.Default
    private List<PageElement> elements = new ArrayList<>();
}
