import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;

public class TestPdfboxMethods {
    public static void main(String[] args) throws Exception {
        System.out.println("Methods in PDImageXObject:");
        for (java.lang.reflect.Method m : PDImageXObject.class.getMethods()) {
            if (m.getName().toLowerCase().contains("byte")) {
                System.out.println(m.getName());
            }
        }
    }
}
