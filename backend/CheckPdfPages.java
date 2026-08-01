import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import java.io.File;
public class CheckPdfPages {
    public static void main(String[] args) throws Exception {
        try (PDDocument doc = Loader.loadPDF(new File(args[0]))) {
            System.out.println("Pages: " + doc.getNumberOfPages());
        }
    }
}
