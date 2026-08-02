package com.convertexxx.converter.render;

import com.convertexxx.converter.layout.TextBlock;
import com.convertexxx.converter.layout.TextLine;
import com.convertexxx.converter.layout.TextSpan;
import org.apache.poi.xwpf.usermodel.IBody;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParagraphRenderer {
    
    private final StyleMapper styleMapper;

    @Autowired
    public ParagraphRenderer(StyleMapper styleMapper) {
        this.styleMapper = styleMapper;
    }

    public void renderTextBlock(IBody body, TextBlock block) {
        XWPFParagraph paragraph = null;
        
        if (body instanceof XWPFDocument) {
            paragraph = ((XWPFDocument) body).createParagraph();
        } else if (body instanceof XWPFTableCell) {
            paragraph = ((XWPFTableCell) body).addParagraph();
        } else {
            throw new IllegalArgumentException("Unsupported IBody type");
        }
        
        renderTextBlockContent(paragraph, block);
    }

    private void renderTextBlockContent(XWPFParagraph paragraph, TextBlock block) {
        styleMapper.applyAlignment(paragraph, block.getAlignment());
        
        for (int l = 0; l < block.getLines().size(); l++) {
            TextLine line = block.getLines().get(l);
            
            for (TextSpan span : line.getSpans()) {
                XWPFRun run = paragraph.createRun();
                run.setText(span.getText());
                
                int fontSize = Math.round(span.getFontSize());
                if (fontSize > 0) {
                    run.setFontSize(fontSize);
                }
                
                if (span.getFontFamily() != null && !span.getFontFamily().equals("Unknown")) {
                    run.setFontFamily(styleMapper.sanitizeFontFamily(span.getFontFamily()));
                }
                
                if (span.isBold()) run.setBold(true);
                if (span.isItalic()) run.setItalic(true);
            }
            
            if (l < block.getLines().size() - 1) {
                XWPFRun breakRun = paragraph.createRun();
                breakRun.addBreak();
            }
        }
    }
}
