package com.convertexxx.converter.layout;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class DocumentLayout {
    @Builder.Default
    private List<PageLayout> pages = new ArrayList<>();
}
