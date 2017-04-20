package nl.meul.kilometerservice;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HtmlReport {
    
    // Open google maps with directions:
    // http://maps.google.com/?saddr=52.044875,4.4958&daddr=51.89511111111111,5.19315
    // Open google maps with directions and via-poitns +to: can be added multiple times
    // http://maps.google.com/?saddr=52.044875,4.4958&daddr=51.89511111111111,5.19315+to:52.044875,4.4958
    // Open google maps with location:
    // http://maps.google.com/maps?z=12&q=loc:52.044875,4.4958
    // Get JSON answer with information about distance and driving time
    // https://maps.googleapis.com/maps/api/distancematrix/json?origins=52.044875,4.4958&destinations=51.89511111111111,5.19315&mode=driving
    
    public static void createReport(List<Route> routes) {
        StringBuilder sb = new StringBuilder();
        addHtml(sb, "<!DOCTYPE html>");
        addHtml(sb, "<html>");
        addHtml(sb, "  <head>");
        addHtml(sb, "    <title>Kilometer Registratie</title>");
        addHtml(sb, "    <style>");
        addHtml(sb, "      table {");
        addHtml(sb, "         font-family: arial, sans-serif;");
        addHtml(sb, "         border-collapse: collapse;");
        addHtml(sb, "         width: 100%;");
        addHtml(sb, "      }");
        addHtml(sb, "      td, th {");
        addHtml(sb, "         border: 1px solid #dddddd;");
        addHtml(sb, "         text-align: left;");
        addHtml(sb, "         padding: 8px;");
        addHtml(sb, "      }");
        addHtml(sb, "      tr:nth-child(even) {");
        addHtml(sb, "         background-color: #dddddd;");
        addHtml(sb, "      }");
        addHtml(sb, "    </style>");
        addHtml(sb, "  </head>");
        addHtml(sb, "  <body>");
        addHtml(sb, "    <table>");
        addHtml(sb, "      <tr>");
        addHtml(sb, "        <th>Date</th>");
        addHtml(sb, "        <th>Routepoints</th>");
        addHtml(sb, "        <th>Start</th>");
        addHtml(sb, "        <th>End</th>");
        addHtml(sb, "        <th>Driven (km)</th>");
        addHtml(sb, "        <th>Google Maps</th>");
        addHtml(sb, "      </tr>");
        int totalMilage = 0;
        for (Route r : routes) {
            addHtml(sb, "      <tr>");
            addHtml(sb, String.format("        <td>%s</td>", new SimpleDateFormat("dd-MM-yyyy").format(r.getTravelDate())));
            addHtml(sb, String.format("        <td>%s</td>", r.getRoutePoints().size()));
            addHtml(sb, String.format("        <td>%s</td>", r.getStartMilage()));
            addHtml(sb, String.format("        <td>%s</td>", r.getEndMilage()));
            addHtml(sb, String.format("        <td>%s</td>", r.getEndMilage() - r.getStartMilage()));
            addHtml(sb, String.format("        <td><a target=\"blank\" href=\"%s\">Show route</a></td>", getGoogleMapsUri(r)));
            addHtml(sb, "      </tr>");
            totalMilage += r.getEndMilage() - r.getStartMilage();
        }
        // Add totals row
        addHtml(sb, "      <tr>");
        addHtml(sb, String.format("        <td></td>"));
        addHtml(sb, String.format("        <td></td>"));
        addHtml(sb, String.format("        <td></td>"));
        addHtml(sb, String.format("        <td></td>"));
        addHtml(sb, String.format("        <td>%s</td>", totalMilage));
        addHtml(sb, String.format("        <td></td>"));
        addHtml(sb, "      </tr>");
        
        addHtml(sb, "    </table>");
        addHtml(sb, "  </body>");
        addHtml(sb, "</html>");

        try {
            FileWriter fstream = new FileWriter("Report.html");
            try (BufferedWriter out = new BufferedWriter(fstream)) {
                out.write(sb.toString());
            }
        } catch (IOException ex) {
            Logger.getLogger(HtmlReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void addHtml(StringBuilder sb, String value) {
        sb.append(value).append(System.getProperty("line.separator"));
    }

    private static String getGoogleMapsUri(Route r) {
        StringBuilder sb = new StringBuilder();
        List<RoutePoint> points = r.getRoutePoints();
        
        if (!points.isEmpty()) {
            sb.append("http://maps.google.com/?");
        
            ListIterator iter = points.listIterator();

            while (iter.hasNext()) {
                switch (iter.nextIndex()) {
                    case 0:
                        sb.append("saddr=");
                        break;
                    case 1:
                        sb.append("&daddr=");
                        break;
                    default:
                        sb.append("+to:");
                        break;
                }
                sb.append(((RoutePoint)iter.next()).getLocation());
            }
        }
        
        return sb.toString();
    }
}
