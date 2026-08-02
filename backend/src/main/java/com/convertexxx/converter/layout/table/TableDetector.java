package com.convertexxx.converter.layout.table;

import com.convertexxx.converter.layout.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TableDetector {

    public List<LineGroup> detectTables(List<TextLine> lines, int pageNumber) {
        List<LineGroup> groups = new ArrayList<>();
        
        List<TextLine> tableBuffer = new ArrayList<>();
        List<TextLine> textBuffer = new ArrayList<>();
        List<Interval> currentTableColumns = null;

        for (TextLine line : lines) {
            List<Interval> lineColumns = getColumns(line);
            
            // A line could be part of a table if it has >= 2 columns
            boolean isTableLine = lineColumns.size() >= 2;
            
            if (isTableLine) {
                if (tableBuffer.isEmpty()) {
                    tableBuffer.add(line);
                    currentTableColumns = lineColumns;
                } else {
                    List<Interval> combinedCols = getCombinedColumns(tableBuffer, line);
                    
                    // Allow lenient matching, as long as they maintain >= 2 columns
                    if (combinedCols.size() >= 2) {
                        tableBuffer.add(line);
                        currentTableColumns = combinedCols;
                    } else {
                        // Flush existing table, start new one
                        flushTableBuffer(groups, tableBuffer, currentTableColumns, textBuffer, pageNumber);
                        
                        tableBuffer.add(line);
                        currentTableColumns = lineColumns;
                    }
                }
            } else {
                // Not a table line
                if (!tableBuffer.isEmpty()) {
                    flushTableBuffer(groups, tableBuffer, currentTableColumns, textBuffer, pageNumber);
                }
                textBuffer.add(line);
            }
        }
        
        if (!tableBuffer.isEmpty()) {
            flushTableBuffer(groups, tableBuffer, currentTableColumns, textBuffer, pageNumber);
        }
        if (!textBuffer.isEmpty()) {
            groups.add(LineGroup.builder().isTable(false).lines(new ArrayList<>(textBuffer)).build());
        }

        return groups;
    }
    
    private void flushTableBuffer(List<LineGroup> groups, List<TextLine> tableBuffer, 
                                  List<Interval> columns, List<TextLine> textBuffer, int pageNumber) {
        if (tableBuffer.size() >= 2) {
            // Valid table. Flush text buffer first.
            if (!textBuffer.isEmpty()) {
                groups.add(LineGroup.builder().isTable(false).lines(new ArrayList<>(textBuffer)).build());
                textBuffer.clear();
            }
            TableModel model = buildTableModel(tableBuffer, columns, pageNumber);
            groups.add(LineGroup.builder().isTable(true).table(model).build());
        } else {
            // False positive. Add to text buffer.
            textBuffer.addAll(tableBuffer);
        }
        tableBuffer.clear();
    }

    private List<Interval> getColumns(TextLine line) {
        return computeIntervals(line.getSpans());
    }

    private List<Interval> getCombinedColumns(List<TextLine> buffer, TextLine newLine) {
        List<TextSpan> spans = new ArrayList<>();
        for (TextLine b : buffer) {
            spans.addAll(b.getSpans());
        }
        spans.addAll(newLine.getSpans());
        return computeIntervals(spans);
    }

    private List<Interval> computeIntervals(List<TextSpan> spans) {
        if (spans.isEmpty()) return Collections.emptyList();

        // Expand each span by 6.0f on each side.
        // This merges words separated by normal spaces (typically 3-5 pts).
        // Only larger gaps (like tabs or column spacing > 12 pts) will result in multiple intervals.
        List<Interval> intervals = spans.stream()
                .map(s -> new Interval(s.getX() - 6.0f, s.getX() + s.getWidth() + 6.0f))
                .sorted(Comparator.comparingDouble(i -> i.start))
                .collect(Collectors.toList());

        List<Interval> merged = new ArrayList<>();
        Interval current = intervals.get(0);

        for (int i = 1; i < intervals.size(); i++) {
            Interval next = intervals.get(i);
            if (next.start <= current.end) {
                current.end = Math.max(current.end, next.end);
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);

        return merged;
    }

    private TableModel buildTableModel(List<TextLine> lines, List<Interval> columns, int pageNumber) {
        List<TableRow> tableRows = new ArrayList<>();
        
        for (TextLine line : lines) {
            TableRow row = TableRow.builder().cells(new ArrayList<>()).build();
            
            for (int i = 0; i < columns.size(); i++) {
                row.getCells().add(TableCell.builder().blocks(new ArrayList<>()).build());
            }
            
            for (TextSpan span : line.getSpans()) {
                int colIndex = findColumnIndex(span, columns);
                if (colIndex != -1) {
                    TableCell cell = row.getCells().get(colIndex);
                    if (cell.getBlocks().isEmpty()) {
                        cell.getBlocks().add(TextBlock.builder().lines(new ArrayList<>()).build());
                    }
                    TextBlock cellBlock = cell.getBlocks().get(0);
                    if (cellBlock.getLines().isEmpty()) {
                        cellBlock.getLines().add(TextLine.builder().spans(new ArrayList<>()).build());
                    }
                    cellBlock.getLines().get(0).getSpans().add(span);
                }
            }
            
            // Only add row if it has content
            boolean hasContent = row.getCells().stream().anyMatch(c -> !c.getBlocks().isEmpty());
            if (hasContent) {
                tableRows.add(row);
            }
        }
        
        List<Float> colWidths = columns.stream()
                .map(i -> (float)(i.end - i.start))
                .collect(Collectors.toList());
                
        float confidence = 0.85f;
        
        TableModel model = TableModel.builder()
                .rows(tableRows)
                .columnWidths(colWidths)
                .confidenceScore(confidence)
                .pageNumber(pageNumber)
                .build();
                
        log.debug("TableDetector [Page {}]: Detected TableModel with {} rows, {} columns, confidence={}", 
                  pageNumber, tableRows.size(), columns.size(), confidence);
                  
        return model;
    }

    private int findColumnIndex(TextSpan span, List<Interval> columns) {
        float mid = span.getX() + (span.getWidth() / 2.0f);
        for (int i = 0; i < columns.size(); i++) {
            Interval col = columns.get(i);
            if (mid >= col.start && mid <= col.end) {
                return i;
            }
        }
        
        // Fallback: find closest column
        int bestIdx = 0;
        float minDiff = Float.MAX_VALUE;
        for (int i = 0; i < columns.size(); i++) {
            Interval col = columns.get(i);
            float colMid = (float) (col.start + col.end) / 2.0f;
            float diff = Math.abs(mid - colMid);
            if (diff < minDiff) {
                minDiff = diff;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    private static class Interval {
        double start;
        double end;
        Interval(double start, double end) {
            this.start = start;
            this.end = end;
        }
    }
}
