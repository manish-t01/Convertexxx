import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestPdfSplit {
    public static void main(String[] args) throws Exception {
        // Create a dummy PDF with 10 pages
        File dummyFile = new File("dummy.pdf");
        try (PDDocument doc = new PDDocument()) {
            for (int i = 0; i < 10; i++) {
                doc.addPage(new PDPage());
            }
            doc.save(dummyFile);
        }
        
        // Simulate SplitPdfConverter logic
        try (PDDocument document = Loader.loadPDF(dummyFile)) {
            List<Integer> pagesToExtract = List.of(0, 1, 2); // 1-3
            File outputFile = new File("extracted.pdf");
            try (PDDocument extractedDoc = new PDDocument()) {
                for (int pageIndex : pagesToExtract) {
                    // This is where the issue might be
                    // In PDFBox 3.x, you cannot just addPage() from another document if it might share resources incorrectly?
                    // Let's test
                    extractedDoc.addPage(document.getPage(pageIndex));
                }
                extractedDoc.save(outputFile);
                System.out.println("Extracted PDF size: " + outputFile.length());
            }
        }
    }
}
