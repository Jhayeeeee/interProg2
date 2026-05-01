import java.awt.*;
import java.awt.image.*;

public class ImageUtils {
    public static Image makeTransparent(Image im) {
        ImageFilter filter = new RGBImageFilter() {
            public final int filterRGB(int x, int y, int rgb) {
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                // If the pixel is very close to white, make it transparent
                if (r > 245 && g > 245 && b > 245) {
                    return 0x00FFFFFF & rgb;
                }
                return rgb;
            }
        };
        ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }
}
