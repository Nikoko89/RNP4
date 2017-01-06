import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class Routes {
    private String target;
    private String nextHop;
    private int port;

    public Routes(String target, String nextHop, int port) {
        this.target = target;
        this.nextHop = nextHop;
        this.port = port;
    }

    public static ArrayList<Routes> readRoutes(File routeCSV) {
        ArrayList<Routes> routingTable = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(routeCSV));
            String input = "";
            while ((input = bufferedReader.readLine()) != null) {
                String[] inputArray = input.split(";");
                routingTable.add(new Routes(inputArray[0], inputArray[1], Integer.parseInt(inputArray[2])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return routingTable;
    }

    public String getTarget() {
        return target;
    }

    public String getNextHop() {
        return nextHop;
    }

    public int getPort() {
        return port;
    }
}
