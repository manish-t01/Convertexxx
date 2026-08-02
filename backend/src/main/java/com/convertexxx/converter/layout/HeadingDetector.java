package com.convertexxx.converter.layout;

import java.util.List;
import java.util.stream.Collectors;

public class HeadingDetector {
    
    public void detectHeadings(List<PageElement> elements) {
        if (elements == null || elements.isEmpty()) {
            return;
        }

        // Calculate median font size across all blocks to represent "body text"
        float medianFontSize = calculateMedianFontSize(elements);

        for (int i = 0; i < elements.size(); i++) {
            PageElement element = elements.get(i);
            
            if (!(element instanceof TextBlock)) {
                continue;
            }
            
            TextBlock block = (TextBlock) element;
            
            // A heading is typically a single line (or at most two)
            if (block.getLines().size() > 2) {
                continue;
            }

            TextLine firstLine = block.getLines().get(0);
            if (firstLine.getSpans().isEmpty()) continue;
            
            TextSpan firstSpan = firstLine.getSpans().get(0);
            
            boolean isLarger = firstSpan.getFontSize() > (medianFontSize * 1.1f);
            boolean isBold = firstSpan.isBold();
            boolean isCentered = block.getAlignment() == TextBlock.Alignment.CENTER;
            
            // Heuristic: If it's larger than body text OR it's bold/centered and isolated, it's a heading
            if (isLarger || (isBold && isCentered)) {
                block.setHeading(true);
            }
        }
    }

    private float calculateMedianFontSize(List<PageElement> elements) {
        List<Float> fontSizes = elements.stream()
                .filter(e -> e instanceof TextBlock)
                .map(e -> (TextBlock) e)
                .flatMap(b -> b.getLines().stream())
                .flatMap(l -> l.getSpans().stream())
                .map(TextSpan::getFontSize)
                .sorted()
                .collect(Collectors.toList());

        if (fontSizes.isEmpty()) return 12.0f;
        
        int middle = fontSizes.size() / 2;
        if (fontSizes.size() % 2 == 1) {
            return fontSizes.get(middle);
        } else {
            return (fontSizes.get(middle - 1) + fontSizes.get(middle)) / 2.0f;
        }
    }
}
