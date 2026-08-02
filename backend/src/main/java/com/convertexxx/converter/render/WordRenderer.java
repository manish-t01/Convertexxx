package com.convertexxx.converter.render;

import com.convertexxx.converter.layout.DocumentLayout;
import com.convertexxx.converter.layout.PageElement;
import com.convertexxx.converter.layout.PageLayout;
import com.convertexxx.converter.layout.TextBlock;
import com.convertexxx.converter.layout.table.TableModel;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Slf4j
@Component
public class WordRenderer {

    private final ParagraphRenderer paragraphRenderer;
    private final TableRenderer tableRenderer;

    @Autowired
    public WordRenderer(ParagraphRenderer paragraphRenderer, TableRenderer tableRenderer) {
        this.paragraphRenderer = paragraphRenderer;
        this.tableRenderer = tableRenderer;
    }

    public void render(DocumentLayout layout, OutputStream outputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            
            List<PageLayout> pages = layout.getPages();
            
            for (int p = 0; p < pages.size(); p++) {
                PageLayout page = pages.get(p);
                
                for (PageElement element : page.getElements()) {
                    if (element instanceof TextBlock) {
                        paragraphRenderer.renderTextBlock(document, (TextBlock) element);
                    } else if (element instanceof TableModel) {
                        tableRenderer.renderTableModel(document, (TableModel) element, page.getWidth());
                    }
                }
                
                if (p < pages.size() - 1) {
                    XWPFParagraph breakPara = document.createParagraph();
                    XWPFRun breakRun = breakPara.createRun();
                    breakRun.addBreak(BreakType.PAGE);
                }
            }
            
            document.write(outputStream);
        }
    }
}
