package com.convertexxx.converter.render;

import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WidthMapper {

    private static final float WORD_PRINTABLE_WIDTH_TWIPS = 9360f; // 8.5" with 1" margins = 6.5" = 9360 twips

    /**
     * Map PDF geometric widths to Word table widths.
     * Scales relative to the PDF page width and standard Word printable width.
     */
    public int calculateTwips(float pdfWidthInPoints, float pdfPageWidth) {
        if (pdfPageWidth <= 0) return 0;
        float ratio = pdfWidthInPoints / pdfPageWidth;
        return Math.round(ratio * WORD_PRINTABLE_WIDTH_TWIPS);
    }
    
    public List<Integer> calculateColumnWidths(List<Float> pdfColumnWidths, float pdfPageWidth) {
        return pdfColumnWidths.stream()
                .map(w -> calculateTwips(w, pdfPageWidth))
                .collect(Collectors.toList());
    }
    
    public void applyCellWidth(XWPFTableCell wordCell, int twips) {
        wordCell.setWidth(String.valueOf(twips));
    }
}
