package com.convertexxx.converter.render;

import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;

@Component
public class OOXMLTableConfigurator {

    public void configureTable(XWPFTable wordTable, List<Integer> colWidthsTwips) {
        CTTbl ctTbl = wordTable.getCTTbl();
        CTTblPr tblPr = ctTbl.getTblPr();
        if (tblPr == null) tblPr = ctTbl.addNewTblPr();
        
        // 1. Fixed Layout
        CTTblLayoutType layoutType = tblPr.isSetTblLayout() ? tblPr.getTblLayout() : tblPr.addNewTblLayout();
        layoutType.setType(STTblLayoutType.FIXED);
        
        // 2. Set total preferred width
        int totalWidth = colWidthsTwips.stream().mapToInt(Integer::intValue).sum();
        CTTblWidth tblW = tblPr.isSetTblW() ? tblPr.getTblW() : tblPr.addNewTblW();
        tblW.setType(STTblWidth.DXA);
        tblW.setW(BigInteger.valueOf(totalWidth));
        
        CTTblGrid tblGrid = ctTbl.getTblGrid();
        if (tblGrid == null) tblGrid = ctTbl.addNewTblGrid();
        int existingCols = tblGrid.sizeOfGridColArray();
        
        for (int i = 0; i < colWidthsTwips.size(); i++) {
            CTTblGridCol gridCol = (i < existingCols) ? tblGrid.getGridColArray(i) : tblGrid.addNewGridCol();
            gridCol.setW(BigInteger.valueOf(colWidthsTwips.get(i)));
        }
        
        // 4. Default Cell Margins (to match typical PDF tight spacing)
        CTTblCellMar cellMar = tblPr.isSetTblCellMar() ? tblPr.getTblCellMar() : tblPr.addNewTblCellMar();
        
        CTTblWidth top = cellMar.isSetTop() ? cellMar.getTop() : cellMar.addNewTop();
        top.setW(BigInteger.valueOf(50)); // ~0.03 inch
        top.setType(STTblWidth.DXA);
        
        CTTblWidth bottom = cellMar.isSetBottom() ? cellMar.getBottom() : cellMar.addNewBottom();
        bottom.setW(BigInteger.valueOf(50));
        bottom.setType(STTblWidth.DXA);
        
        CTTblWidth left = cellMar.isSetLeft() ? cellMar.getLeft() : cellMar.addNewLeft();
        left.setW(BigInteger.valueOf(100)); // ~0.07 inch
        left.setType(STTblWidth.DXA);
        
        CTTblWidth right = cellMar.isSetRight() ? cellMar.getRight() : cellMar.addNewRight();
        right.setW(BigInteger.valueOf(100));
        right.setType(STTblWidth.DXA);
    }
}
