package nl.meul.kilometerservice;

import java.io.File;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

/** The rest interface class.
 */
@Path("v1")
public class RestInterface {

    private static final Logger LOGGER = Logger.getLogger(RestInterface.class.getName());

    @GET
    @Path("/createReport")
    @Produces(MediaType.APPLICATION_JSON)
    public Response status() throws ClassNotFoundException, SQLException {

        JsonBuilderFactory factory = Json.createBuilderFactory(null);
        JsonObjectBuilder result = factory.createObjectBuilder();
        
        Database db = new Database();

        int lastKnownMilage = 0;
        File[] directoryListing = new File("input").listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                ImageInfo info = new ImageInfo(child);
                Date fileDate = info.getDate();

                // Set correct dateFormat
                DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String destinationName = child.getParentFile().getAbsolutePath() + "\\" + df.format(fileDate) + ".jpeg";

                if (!db.recordExists(destinationName)) {
                    lastKnownMilage = info.getMilage(lastKnownMilage);
                    db.addRecord(destinationName, fileDate, info.getLocation(), lastKnownMilage);
                    child.renameTo(new File(destinationName));
                }
            }
        }

        RouteManager manager = new RouteManager();
        manager.determineRoutes();
        HtmlReport.createReport(manager.getRoutes());

        result.add("result", "ok");

        return Response.ok(result.build().toString(), MediaType.APPLICATION_JSON).build();
    }
}
