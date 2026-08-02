package com.convertexxx.converter.layout;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfTextExtractor extends PDFTextStripper {
    
    private List<TextSpan> currentSpans = new ArrayList<>();

    public PdfTextExtractor() throws IOException {
        super();
        this.setSortByPosition(true);
    }

    public List<TextSpan> extractSpans() {
        return currentSpans;
    }

    public void clear() {
        currentSpans.clear();
    }

    @Override
    protected void processTextPosition(TextPosition text) {
        String unicode = text.getUnicode();
        
        // Include spaces because they matter for layout
        if (unicode == null) {
            return;
        }

        PDFont font = text.getFont();
        String fontFamily = font != null ? font.getName() : "Unknown";
        String familyLower = fontFamily.toLowerCase();
        boolean isBold = familyLower.contains("bold");
        boolean isItalic = familyLower.contains("italic") || familyLower.contains("oblique");

        TextSpan span = TextSpan.builder()
                .text(unicode)
                .fontFamily(fontFamily)
                .fontSize(text.getFontSizeInPt())
                .isBold(isBold)
                .isItalic(isItalic)
                .x(text.getXDirAdj())
                .y(text.getYDirAdj())
                .width(text.getWidthDirAdj())
                .height(text.getHeightDir())
                .build();
        
        currentSpans.add(span);
        super.processTextPosition(text);
    }
}
