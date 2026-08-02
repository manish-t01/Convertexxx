package com.convertexxx.converter.layout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ParagraphDetector {

    public List<TextLine> detectLines(List<TextSpan> rawSpans) {
        if (rawSpans == null || rawSpans.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<TextSpan> mergedSpans = mergeIntoWords(rawSpans);
        return groupIntoLines(mergedSpans);
    }
    
    public List<PageElement> detectBlocks(List<LineGroup> groups, float pageWidth) {
        if (groups == null || groups.isEmpty()) {
            return new ArrayList<>();
        }

        List<PageElement> elements = new ArrayList<>();
        
        for (LineGroup group : groups) {
            if (group.isTable()) {
                elements.add(group.getTable());
            } else {
                List<TextLine> lines = group.getLines();
                if (lines == null || lines.isEmpty()) continue;
                
                float avgLineSpacing = calculateAverageLineSpacing(lines);
                List<TextBlock> blocks = groupIntoBlocks(lines, avgLineSpacing, pageWidth);
                elements.addAll(blocks);
            }
        }
        
        return elements;
    }

    private List<TextSpan> mergeIntoWords(List<TextSpan> rawSpans) {
        List<TextSpan> words = new ArrayList<>();
        if (rawSpans.isEmpty()) return words;

        // Sort primarily by Y, then by X
        rawSpans.sort(Comparator.comparing(TextSpan::getY).thenComparing(TextSpan::getX));

        TextSpan currentWord = null;

        for (TextSpan span : rawSpans) {
            if (currentWord == null) {
                currentWord = cloneSpan(span);
                continue;
            }

            boolean sameStyle = span.getFontFamily().equals(currentWord.getFontFamily()) &&
                                Math.abs(span.getFontSize() - currentWord.getFontSize()) < 0.5f &&
                                span.isBold() == currentWord.isBold() &&
                                span.isItalic() == currentWord.isItalic();

            boolean sameLine = Math.abs(span.getY() - currentWord.getY()) < Math.max(span.getFontSize(), currentWord.getFontSize()) * 0.3f;
            
            float distanceX = span.getX() - (currentWord.getX() + currentWord.getWidth());
            boolean closeEnough = distanceX < (currentWord.getFontSize() * 0.4f); // Dynamic spacing threshold based on font size

            if (sameStyle && sameLine && closeEnough) {
                currentWord.setText(currentWord.getText() + span.getText());
                currentWord.setWidth((span.getX() + span.getWidth()) - currentWord.getX());
                currentWord.setHeight(Math.max(currentWord.getHeight(), span.getHeight()));
            } else {
                words.add(currentWord);
                currentWord = cloneSpan(span);
            }
        }
        
        if (currentWord != null) {
            words.add(currentWord);
        }

        return words;
    }

    private List<TextLine> groupIntoLines(List<TextSpan> words) {
        List<TextLine> lines = new ArrayList<>();
        if (words.isEmpty()) return lines;

        TextLine currentLine = null;

        for (TextSpan word : words) {
            if (currentLine == null) {
                currentLine = createLine(word);
                continue;
            }

            boolean sameLine = Math.abs(word.getY() - currentLine.getY()) < word.getFontSize() * 0.5f;

            if (sameLine) {
                currentLine.getSpans().add(word);
                float minX = Math.min(currentLine.getX(), word.getX());
                float maxX = Math.max(currentLine.getX() + currentLine.getWidth(), word.getX() + word.getWidth());
                currentLine.setX(minX);
                currentLine.setWidth(maxX - minX);
                currentLine.setHeight(Math.max(currentLine.getHeight(), word.getHeight()));
            } else {
                lines.add(currentLine);
                currentLine = createLine(word);
            }
        }
        
        if (currentLine != null) {
            lines.add(currentLine);
        }

        return lines;
    }

    private float calculateAverageLineSpacing(List<TextLine> lines) {
        if (lines.size() < 2) return 12.0f; // Default
        
        float totalSpacing = 0;
        int count = 0;

        for (int i = 1; i < lines.size(); i++) {
            TextLine prev = lines.get(i - 1);
            TextLine curr = lines.get(i);
            
            float gap = curr.getY() - (prev.getY() + prev.getHeight());
            if (gap > 0 && gap < curr.getHeight() * 3) { // ignore massive jumps (like next section)
                totalSpacing += gap;
                count++;
            }
        }
        
        return count > 0 ? totalSpacing / count : 12.0f;
    }

    private List<TextBlock> groupIntoBlocks(List<TextLine> lines, float avgLineSpacing, float pageWidth) {
        List<TextBlock> blocks = new ArrayList<>();
        if (lines.isEmpty()) return blocks;

        TextBlock currentBlock = null;

        for (TextLine line : lines) {
            if (currentBlock == null) {
                currentBlock = createBlock(line);
                continue;
            }

            TextLine lastLine = currentBlock.getLines().get(currentBlock.getLines().size() - 1);
            float gap = line.getY() - (lastLine.getY() + lastLine.getHeight());
            
            // Dynamic threshold: If gap is larger than ~1.5x average line spacing, it's a new paragraph
            boolean sameBlock = gap < (Math.max(avgLineSpacing, line.getHeight()) * 1.5f);

            if (sameBlock) {
                currentBlock.getLines().add(line);
                float minX = Math.min(currentBlock.getX(), line.getX());
                float maxX = Math.max(currentBlock.getX() + currentBlock.getWidth(), line.getX() + line.getWidth());
                currentBlock.setX(minX);
                currentBlock.setWidth(maxX - minX);
                currentBlock.setHeight((line.getY() + line.getHeight()) - currentBlock.getY());
            } else {
                blocks.add(currentBlock);
                currentBlock = createBlock(line);
            }
        }
        
        if (currentBlock != null) {
            blocks.add(currentBlock);
        }

        // Compute alignment for each block based on page width
        for (TextBlock block : blocks) {
            block.setAlignment(detectAlignment(block, pageWidth));
        }

        return blocks;
    }

    private TextBlock.Alignment detectAlignment(TextBlock block, float pageWidth) {
        float midX = block.getX() + (block.getWidth() / 2.0f);
        float pageMidX = pageWidth / 2.0f;
        
        if (Math.abs(midX - pageMidX) < (pageWidth * 0.1f)) {
            return TextBlock.Alignment.CENTER;
        }
        
        float rightEdge = block.getX() + block.getWidth();
        if (pageWidth - rightEdge < block.getX() && (pageWidth - rightEdge) < (pageWidth * 0.15f)) {
            return TextBlock.Alignment.RIGHT;
        }
        
        return TextBlock.Alignment.LEFT; // Default left
    }

    private TextSpan cloneSpan(TextSpan s) {
        return TextSpan.builder()
                .text(s.getText())
                .fontFamily(s.getFontFamily())
                .fontSize(s.getFontSize())
                .isBold(s.isBold())
                .isItalic(s.isItalic())
                .x(s.getX())
                .y(s.getY())
                .width(s.getWidth())
                .height(s.getHeight())
                .build();
    }

    private TextLine createLine(TextSpan word) {
        List<TextSpan> spans = new ArrayList<>();
        spans.add(word);
        return TextLine.builder()
                .spans(spans)
                .x(word.getX())
                .y(word.getY())
                .width(word.getWidth())
                .height(word.getHeight())
                .build();
    }

    private TextBlock createBlock(TextLine line) {
        List<TextLine> lines = new ArrayList<>();
        lines.add(line);
        return TextBlock.builder()
                .lines(lines)
                .x(line.getX())
                .y(line.getY())
                .width(line.getWidth())
                .height(line.getHeight())
                .build();
    }
}
