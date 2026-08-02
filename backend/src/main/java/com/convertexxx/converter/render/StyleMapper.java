package com.convertexxx.converter.render;

import com.convertexxx.converter.layout.TextBlock;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

@Component
public class StyleMapper {

    public void applyAlignment(XWPFParagraph paragraph, TextBlock.Alignment alignment) {
        if (alignment == null) return;
        switch (alignment) {
            case CENTER:
                paragraph.setAlignment(ParagraphAlignment.CENTER);
                break;
            case RIGHT:
                paragraph.setAlignment(ParagraphAlignment.RIGHT);
                break;
            case JUSTIFY:
                paragraph.setAlignment(ParagraphAlignment.BOTH);
                break;
            case LEFT:
            default:
                paragraph.setAlignment(ParagraphAlignment.LEFT);
                break;
        }
    }
    
    public String sanitizeFontFamily(String pdfFontName) {
        if (pdfFontName.contains("+")) {
            pdfFontName = pdfFontName.substring(pdfFontName.indexOf('+') + 1);
        }
        pdfFontName = pdfFontName.replaceAll("-?(Bold|Italic|Oblique|MT|Roman)", "");
        return pdfFontName;
    }
}
