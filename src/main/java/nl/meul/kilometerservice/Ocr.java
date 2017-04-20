package nl.meul.kilometerservice;

import com.sun.jna.Pointer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.ITessAPI;
import static net.sourceforge.tess4j.ITessAPI.TRUE;
import net.sourceforge.tess4j.TessAPI1;
import net.sourceforge.tess4j.util.ImageIOHelper;

/** Class for reading text from an image
 */
public class Ocr {
    final private String DATAPATH = ".//tessdata";
    final private String LANGUAGE = "eng";
    
    static final double MINIMUM_DESKEW_THRESHOLD = 0.05d;
    
    private BufferedImage myImage;
    final private String myFileName;

    /** Constructor
     *
     * @param image The image to read text from
     */
    public Ocr(File image) {
        myFileName = image.getName();

        try {
            BufferedImage source = ImageIO.read(image);
            
            // Convert image to correct ColorModel
            BufferedImage sourceConv = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g = sourceConv.getGraphics();
            g.drawImage(source, 0, 0, null);
            g.dispose();
   
            sourceConv = Threshold(sourceConv, 150, 255);
            ImageIO.write(sourceConv, "JPG", new File(myFileName));
            myImage = ImageIO.read(new File(myFileName));
            //myImage = Threshold(sourceConv, 150, 255);
        } catch (IOException ex) {
            Logger.getLogger(Ocr.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //ImageDeskew id = new ImageDeskew(myImage);
        //double imageSkewAngle = id.getSkewAngle(); // determine skew angle
        //if ((imageSkewAngle > MINIMUM_DESKEW_THRESHOLD || imageSkewAngle < -(MINIMUM_DESKEW_THRESHOLD))) {
        //    myImage = ImageHelper.rotateImage(myImage, -imageSkewAngle); // deskew image
        //}
    }
    
    public static BufferedImage Threshold(BufferedImage img, int lowThresholdValue, int highThresholdValue) {
        int height = img.getHeight();
        int width = img.getWidth();
        BufferedImage finalThresholdImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB) ;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int color = img.getRGB(x, y);

                int red = (color & 0x00ff0000) >> 16;
                int green = (color & 0x0000ff00) >> 8;
                int blue = (color & 0x000000ff);

                if(red >= lowThresholdValue && red <= highThresholdValue &&
                        green >= lowThresholdValue && green <= highThresholdValue &&
                        blue >= lowThresholdValue && blue <= highThresholdValue) {
                    finalThresholdImage.setRGB(x, y, 0);
                }
                else {
                    finalThresholdImage.setRGB(x, y, 0xffffff);
                }
            }
        }
        return finalThresholdImage;
    }
    
    public int getBestMatch(int minimalValue) { 
        float confidence = 0.0f;
        int bestMatch = 0;
        ITessAPI.TessBaseAPI handle = TessAPI1.TessBaseAPICreate();

        ByteBuffer buf = ImageIOHelper.convertImageData(myImage);
        int bpp = myImage.getColorModel().getPixelSize();
        int bytespp = bpp / 8;
        int bytespl = (int) Math.ceil(myImage.getWidth() * bpp / 8.0);

        TessAPI1.TessBaseAPISetPageSegMode(handle, ITessAPI.TessPageSegMode.PSM_SPARSE_TEXT);
        TessAPI1.TessBaseAPIInit3(handle, DATAPATH, LANGUAGE);
        TessAPI1.TessBaseAPISetImage(handle, buf, myImage.getWidth(), myImage.getHeight(), bytespp, bytespl);
        TessAPI1.TessBaseAPISetVariable(handle, "tessedit_char_whitelist", "0123456789");

        ITessAPI.ETEXT_DESC monitor = new ITessAPI.ETEXT_DESC();
        TessAPI1.TessBaseAPIRecognize(handle, monitor);
        ITessAPI.TessResultIterator ri = TessAPI1.TessBaseAPIGetIterator(handle);
        ITessAPI.TessPageIterator pi = TessAPI1.TessResultIteratorGetPageIterator(ri);
        TessAPI1.TessPageIteratorBegin(pi);
        int level = TessAPI1.TessPageIteratorLevel.RIL_WORD;

        do {
            Pointer ptr = TessAPI1.TessResultIteratorGetUTF8Text(ri, level);
            if (ptr != null) {
                String word = ptr.getString(0);
                TessAPI1.TessDeleteText(ptr);
                float foundConfidence = TessAPI1.TessResultIteratorConfidence(ri, level);

                if (foundConfidence > 50.0 && word.matches("[-+]?\\d*\\.?\\d+")) {
                    int matchedValue = Integer.parseInt(word);
                    //System.out.println(String.format("Number:%d Confidence:%f", matchedValue, foundConfidence));
                    //drawBoundingBox(pi, level);

                    if (matchedValue > minimalValue && confidence < foundConfidence) {
                        bestMatch = Integer.parseInt(word);
                        confidence = foundConfidence;
                    }
                }
            }
        } while (TessAPI1.TessPageIteratorNext(pi, level) == TRUE);
        
        // Cleanup to free memory crashes???
        //TessAPI1.TessPageIteratorDelete(pi);
        //TessAPI1.TessResultIteratorDelete(ri);
        //TessAPI1.TessBaseAPIDelete(handle);
        //TessAPI1.TessBaseAPIEnd(handle);
        System.out.println(String.format("Best match:%s Confidence:%f", bestMatch, confidence));
        return bestMatch;
    }

    private void drawBoundingBox(ITessAPI.TessPageIterator pi, int level) {
        IntBuffer leftB = IntBuffer.allocate(1);
        IntBuffer topB = IntBuffer.allocate(1);
        IntBuffer rightB = IntBuffer.allocate(1);
        IntBuffer bottomB = IntBuffer.allocate(1);
        TessAPI1.TessPageIteratorBoundingBox(pi, level, leftB, topB, rightB, bottomB);
        int left = leftB.get();
        int top = topB.get();
        int right = rightB.get();
        int bottom = bottomB.get();

        Graphics2D graphics = myImage.createGraphics();
        graphics.setColor(Color.red);
        graphics.setStroke(new BasicStroke(5));
        graphics.drawRect(left, top, right - left, bottom - top);
        try {
            ImageIO.write(myImage, "JPG", new File(myFileName));
        } catch (IOException ex) {
            Logger.getLogger(ImageInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        graphics.dispose();
    }
}
