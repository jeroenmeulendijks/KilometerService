package nl.meul.kilometerservice;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageInfo {
    
    final private File myImage;
    private Metadata myMetaData;
    
    public ImageInfo(File image) {
        myImage = image;
        myMetaData = new Metadata();

        try {
            myMetaData = ImageMetadataReader.readMetadata(image.getAbsoluteFile());
        } catch (ImageProcessingException | IOException ex) {
            Logger.getLogger(ImageInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getMilage(int lastKnownMilage) {
        Ocr ocr = new Ocr(myImage);
        return ocr.getBestMatch(lastKnownMilage);
    }

    public Date getDate() {
        Date result = new Date();
        ExifSubIFDDirectory directory = myMetaData.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (directory != null && !directory.isEmpty())
        {
            result = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        }
        return result;
    }

    public GeoLocation getLocation() {
        GeoLocation geoLocation = new GeoLocation(0, 0);
        GpsDirectory gpsDirectory = myMetaData.getFirstDirectoryOfType(GpsDirectory.class);

        if (gpsDirectory != null && !gpsDirectory.isEmpty())
        {
            GeoLocation loc = gpsDirectory.getGeoLocation();
            if (loc != null && !loc.isZero()) {
                geoLocation = loc;
            }
        }
        
        return geoLocation;
    }
}
