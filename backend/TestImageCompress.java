import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.cos.COSName;
import java.io.File;

public class TestImageCompress {
    public static void main(String[] args) throws Exception {
        try (PDDocument doc = Loader.loadPDF(new File(args[0]))) {
            for (PDPage page : doc.getPages()) {
                PDResources res = page.getResources();
                if (res != null) {
                    for (COSName name : res.getXObjectNames()) {
                        PDXObject xobj = res.getXObject(name);
                        if (xobj instanceof PDImageXObject) {
                            PDImageXObject img = (PDImageXObject) xobj;
                            System.out.println("Image: " + name.getName() + " - " + img.getSuffix());
                        }
                    }
                }
            }
        }
    }
}
