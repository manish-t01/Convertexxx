package com.convertexxx.converter.layout;

import com.convertexxx.converter.layout.table.TableDetector;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class LayoutAnalyzer {

    public DocumentLayout analyze(PDDocument document) throws IOException {
        DocumentLayout docLayout = DocumentLayout.builder().build();
        
        PdfTextExtractor extractor = new PdfTextExtractor();
        ParagraphDetector paragraphDetector = new ParagraphDetector();
        HeadingDetector headingDetector = new HeadingDetector();

        int totalPages = document.getNumberOfPages();
        for (int p = 1; p <= totalPages; p++) {
            extractor.clear();
            extractor.setStartPage(p);
            extractor.setEndPage(p);
            extractor.getText(document); // This triggers the processTextPosition overrides
            
            List<TextSpan> rawSpans = extractor.extractSpans();
            
            PDPage page = document.getPage(p - 1);
            PDRectangle mediaBox = page.getMediaBox();
            float pageWidth = mediaBox != null ? mediaBox.getWidth() : 600f;
            float pageHeight = mediaBox != null ? mediaBox.getHeight() : 800f;
            
            // 1. Raw Spans -> TextLines
            List<TextLine> lines = paragraphDetector.detectLines(rawSpans);
            
            // 2. TextLines -> Tables & Paragraph Lines (LineGroups)
            TableDetector tableDetector = new TableDetector();
            List<LineGroup> groups = tableDetector.detectTables(lines, p);
            
            // 3. Paragraph Lines -> TextBlocks (mixed with Tables)
            List<PageElement> elements = paragraphDetector.detectBlocks(groups, pageWidth);
            
            headingDetector.detectHeadings(elements);
            
            PageLayout pageLayout = PageLayout.builder()
                    .pageNumber(p)
                    .width(pageWidth)
                    .height(pageHeight)
                    .elements(elements)
                    .build();
            
            docLayout.getPages().add(pageLayout);
        }

        return docLayout;
    }
}
