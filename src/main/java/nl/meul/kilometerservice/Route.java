package nl.meul.kilometerservice;

import java.util.ArrayList;
import java.util.Date;
import java.util.IntSummaryStatistics;
import java.util.List;

public class Route {
    private Date myTravelDate;
    final private List<RoutePoint> myRoutePoints;
    
    public Route() {
        myRoutePoints = new ArrayList<>();
        myTravelDate = new Date();
    }

    public boolean isPartOfRoute(RoutePoint q) {
        if (!myRoutePoints.isEmpty()) {
            RoutePoint p = myRoutePoints.get(0);

            return (p.getTravelDate().getYear() == q.getTravelDate().getYear() &&
                p.getTravelDate().getMonth()== q.getTravelDate().getMonth() &&
                p.getTravelDate().getDate()== q.getTravelDate().getDate());
        }
        else {
            return false;
        }
    }

    public void add(RoutePoint routePoint) {
        myTravelDate = routePoint.getTravelDate();
        myRoutePoints.add(routePoint);
    }
    
    public List<RoutePoint> getRoutePoints() {
        return myRoutePoints;
    }
    
    public Date getTravelDate() {
        return myTravelDate;
    }

    public int getStartMilage() {
        IntSummaryStatistics summaryStatistics = myRoutePoints.stream()
                .mapToInt(RoutePoint::getMilage)
                .summaryStatistics();

        return summaryStatistics.getMin();
    }
    
    public int getEndMilage() {
        IntSummaryStatistics summaryStatistics = myRoutePoints.stream()
                .mapToInt(RoutePoint::getMilage)
                .summaryStatistics();

        return summaryStatistics.getMax();
    }
}