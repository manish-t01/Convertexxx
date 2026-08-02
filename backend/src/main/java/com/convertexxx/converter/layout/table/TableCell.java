package com.convertexxx.converter.layout.table;

import com.convertexxx.converter.layout.TextBlock;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TableCell {
    // A cell may contain multiple blocks (e.g. paragraphs)
    private List<TextBlock> blocks;
}
