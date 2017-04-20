package nl.meul.kilometerservice;

import com.drew.lang.GeoLocation;
import java.util.Date;

public class RoutePoint {
    
    final private Date myTravelDate;
    final private int myMilage;
    final private GeoLocation myLocation;
    
    public RoutePoint(Date travelDate, int milage, GeoLocation loc) {
        myTravelDate = travelDate;
        myMilage = milage;
        myLocation = loc;
    }
    
    public Date getTravelDate() {
        return myTravelDate;
    }
    
    public int getMilage() {
        return myMilage;
    }
    
    public GeoLocation getLocation() {
        return myLocation;
    }
}