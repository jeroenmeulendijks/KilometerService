package nl.meul.kilometerservice;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/** Main class responsible for starting and stopping the http server.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static HttpServer myServer = null;

    /** Start the http server.
     *
     * @param uri The base uri the http serer should listen to.
     */
    public static void startServer(URI uri) {

        if (myServer == null) {
            LOGGER.log(Level.INFO, "Starting http server for {0}", uri.toString());

            String[] packages = { "nl.meul.kilometerservice" };

            ResourceConfig rc = new ResourceConfig().packages(packages);

            myServer = GrizzlyHttpServerFactory.createHttpServer(uri, rc);
        }
        else {
            LOGGER.log(Level.WARNING, "http server already started");
        }
    }

    /** Stop the http server.
     */
    public static void stopServer() {

        if (myServer != null) {
            LOGGER.log(Level.INFO, "Stopping http server");

            myServer.shutdown();
            myServer = null;
        }
        else {
            LOGGER.log(Level.WARNING, "http server not running");
        }
    }

    /** Run the micro-service.
     */
    public void run() {

        startServer(UriBuilder
                .fromUri("http://{host}/kilometerservice")
                .resolveTemplate("host", "localhost")
                .port(9000).build());

        try {
            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Server interrupted.", ex);
        }

        stopServer();
    }

    /** The application entry point.
     *
     * @param args The command line parameters.
     */
    public static void main(String[] args) {
        
        Main main = new Main();

        main.run();
    }
}
