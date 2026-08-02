package com.convertexxx.converter.layout.table;

import com.convertexxx.converter.layout.PageElement;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TableModel implements PageElement {
    private List<TableRow> rows;
    private List<Float> columnWidths; // Approximate proportional widths
    private float confidenceScore; // 0.0 to 1.0 indicating confidence in detection
    private int pageNumber;
}
