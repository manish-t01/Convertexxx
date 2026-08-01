import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class TestImageRecompression {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) return;
        File input = new File(args[0]);
        File output = new File(args[1]);

        try (PDDocument doc = Loader.loadPDF(input)) {
            Set<String> processedImages = new HashSet<>();

            for (PDPage page : doc.getPages()) {
                PDResources res = page.getResources();
                if (res != null) {
                    for (COSName name : res.getXObjectNames()) {
                        PDXObject xobj = res.getXObject(name);
                        if (xobj instanceof PDImageXObject) {
                            PDImageXObject img = (PDImageXObject) xobj;
                            
                            // Skip if transparency is present
                            if (img.getMask() != null || img.getSoftMask() != null || img.isStencil()) {
                                System.out.println("Skipping image with transparency");
                                continue;
                            }

                            // Avoid duplicate processing
                            String objectKey = img.getCOSObject().hashCode() + "";
                            if (processedImages.contains(objectKey)) {
                                continue;
                            }
                            processedImages.add(objectKey);

                            int width = img.getWidth();
                            int height = img.getHeight();
                            long pixels = (long) width * height;

                            // Only recompress if image is large (e.g. > 100,000 pixels)
                            if (pixels > 100000) {
                                BufferedImage bimg = img.getImage();
                                
                                // Downsample if huge (> 2 megapixels)
                                if (pixels > 2000000) {
                                    double scale = Math.sqrt(2000000.0 / pixels);
                                    int newW = (int) (width * scale);
                                    int newH = (int) (height * scale);
                                    Image scaled = bimg.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                                    BufferedImage newBimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
                                    Graphics2D g2d = newBimg.createGraphics();
                                    g2d.drawImage(scaled, 0, 0, null);
                                    g2d.dispose();
                                    bimg = newBimg;
                                } else if (bimg.getType() != BufferedImage.TYPE_INT_RGB) {
                                    BufferedImage newBimg = new BufferedImage(bimg.getWidth(), bimg.getHeight(), BufferedImage.TYPE_INT_RGB);
                                    Graphics2D g2d = newBimg.createGraphics();
                                    g2d.drawImage(bimg, 0, 0, null);
                                    g2d.dispose();
                                    bimg = newBimg;
                                }
                                
                                PDImageXObject newImg = JPEGFactory.createFromImage(doc, bimg, 0.65f);
                                res.put(name, newImg);
                                System.out.println("Recompressed image " + name.getName());
                            }
                        }
                    }
                }
            }
            doc.save(output, CompressParameters.DEFAULT_COMPRESSION);
        }
    }
}
