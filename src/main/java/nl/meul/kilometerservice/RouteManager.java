package nl.meul.kilometerservice;

import java.util.ArrayList;
import java.util.List;

public class RouteManager {

    final private List<Route> myRoutes;
    
    public RouteManager() {
        myRoutes = new ArrayList<>();
    }
    
    public void determineRoutes() {
        Route curRoute = null;       
        Database db = new Database();
        
        for (RoutePoint rp : db.getRoutePoints()) {
            if (curRoute == null || !curRoute.isPartOfRoute(rp)) {
                curRoute = new Route();
                curRoute.add(rp);
                myRoutes.add(curRoute);
            }
            else {
                curRoute.add(rp);
            }
        }
    }

    public List<Route> getRoutes() {
        return myRoutes;
    }
}
