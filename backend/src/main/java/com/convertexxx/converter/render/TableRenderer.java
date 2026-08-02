package com.convertexxx.converter.render;

import com.convertexxx.converter.layout.TextBlock;
import com.convertexxx.converter.layout.table.TableCell;
import com.convertexxx.converter.layout.table.TableModel;
import com.convertexxx.converter.layout.table.TableRow;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TableRenderer {

    private final ParagraphRenderer paragraphRenderer;
    private final WidthMapper widthMapper;
    private final OOXMLTableConfigurator tableConfigurator;

    @Autowired
    public TableRenderer(ParagraphRenderer paragraphRenderer, WidthMapper widthMapper, OOXMLTableConfigurator tableConfigurator) {
        this.paragraphRenderer = paragraphRenderer;
        this.widthMapper = widthMapper;
        this.tableConfigurator = tableConfigurator;
    }

    public void renderTableModel(XWPFDocument document, TableModel tableModel, float pdfPageWidth) {
        if (tableModel.getRows().isEmpty()) {
            log.warn("TableRenderer [Page {}]: TableModel has 0 rows, skipping.", tableModel.getPageNumber());
            return;
        }
        
        int numRows = tableModel.getRows().size();
        int numCols = tableModel.getRows().get(0).getCells().size();
        
        // Diagnostic fallback
        if (numRows < 2 && numCols < 2) {
            log.warn("TableRenderer [Page {}]: TableModel is 1x1, falling back to paragraph rendering.", tableModel.getPageNumber());
            for (TableRow row : tableModel.getRows()) {
                for (TableCell cell : row.getCells()) {
                    for (TextBlock block : cell.getBlocks()) {
                        paragraphRenderer.renderTextBlock(document, block);
                    }
                }
            }
            return;
        }
        
        log.debug("TableRenderer [Page {}]: Rendering native XWPFTable with {} rows, {} columns.", 
                  tableModel.getPageNumber(), numRows, numCols);
        
        // Map PDF width to Twips
        List<Integer> colWidthsTwips = widthMapper.calculateColumnWidths(tableModel.getColumnWidths(), pdfPageWidth);
        
        XWPFTable wordTable = document.createTable(numRows, numCols);
        wordTable.removeBorders(); 
        
        // Apply strict OOXML compliance configurations
        tableConfigurator.configureTable(wordTable, colWidthsTwips);
        
        for (int r = 0; r < numRows; r++) {
            TableRow rowModel = tableModel.getRows().get(r);
            XWPFTableRow wordRow = wordTable.getRow(r);
            if (wordRow == null) wordRow = wordTable.createRow();
            
            for (int c = 0; c < rowModel.getCells().size() && c < numCols; c++) {
                TableCell cellModel = rowModel.getCells().get(c);
                XWPFTableCell wordCell = wordRow.getCell(c);
                if (wordCell == null) wordCell = wordRow.createCell();
                
                if (c < colWidthsTwips.size()) {
                    widthMapper.applyCellWidth(wordCell, colWidthsTwips.get(c));
                }
                
                // Clear default paragraph POI creates
                if (!wordCell.getParagraphs().isEmpty()) {
                    wordCell.removeParagraph(0);
                }
                
                // Render text blocks into the cell
                for (TextBlock block : cellModel.getBlocks()) {
                    paragraphRenderer.renderTextBlock(wordCell, block);
                }
                
                // Ensure every TableCell contains at least one paragraph
                if (wordCell.getParagraphs().isEmpty()) {
                    wordCell.addParagraph();
                }
            }
        }
    }
}
