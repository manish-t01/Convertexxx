import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import java.lang.reflect.Method;

public class TestPdfboxCompress {
    public static void main(String[] args) throws Exception {
        System.out.println("Methods available in PDImageXObject:");
        for (Method m : PDImageXObject.class.getMethods()) {
            if (m.getName().toLowerCase().contains("mask") || m.getName().toLowerCase().contains("stencil")) {
                System.out.println(m.getName());
            }
        }
    }
}
