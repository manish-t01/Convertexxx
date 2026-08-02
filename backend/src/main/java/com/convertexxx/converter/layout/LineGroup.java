package com.convertexxx.converter.layout;

import com.convertexxx.converter.layout.table.TableModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LineGroup {
    private boolean isTable;
    private TableModel table;
    private List<TextLine> lines;
}
