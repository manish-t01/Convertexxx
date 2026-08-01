import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TestPdfbox {
    public static void main(String[] args) {
        System.out.println("Fields:");
        for (Field f : CompressParameters.class.getDeclaredFields()) {
            System.out.println(f.getName() + " - " + f.getType().getName());
        }
        System.out.println("\nMethods:");
        for (Method m : CompressParameters.class.getDeclaredMethods()) {
            System.out.println(m.getName() + " - " + m.getReturnType().getName());
        }
    }
}
