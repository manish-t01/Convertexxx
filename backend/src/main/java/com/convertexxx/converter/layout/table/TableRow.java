package com.convertexxx.converter.layout.table;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TableRow {
    private List<TableCell> cells;
}
