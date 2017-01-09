import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Router {
    private NetworkLayer networkLayer;
    private List<RoutePayload> routingList;

    public Router(int port, String routeTablePath) {
        try {
            networkLayer = new NetworkLayer(port);
        } catch (SocketException e) {
            System.err.println("Could not create the networklayer");
        }
        this.routingList = readRoutingTable(routeTablePath);
        System.out.println("Reading from file: " + routeTablePath + " Port: " + port);
    }

    private List<RoutePayload> readRoutingTable(String routeTablePath) {
        List<RoutePayload> routingList = new ArrayList<>();
        String route;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(routeTablePath));
            while ((route = bufferedReader.readLine()) != null) {
                String[] routeArray = route.split(";");
                RoutePayload routePayload = new RoutePayload(routeArray[0], routeArray[1], Integer.parseInt(routeArray[2]));
                routingList.add(routePayload);
            }
        } catch (Exception e) {
            System.err.println("Could not read file");
        }
        return routingList;
    }

    public void sendAndReceive() {
        while (true) {
            try {
                IpPacket ipPacket = networkLayer.getPacket();
                System.out.println("received Message: " + ipPacket);
                boolean reachable = routingList.stream().filter(s -> s.getTargetAdress().toString().contains(ipPacket.getDestinationAddress().toString())).findAny().isPresent();
                if (reachable) {
                    Inet6Address targetAdress = ipPacket.getDestinationAddress();
                    RoutePayload bestRoute = routingList.stream().max(Comparator.comparing(item -> item.getBestMatch(targetAdress))).get();

                    if (ipPacket.getHopLimit() > 1) {
                        sendPackage(ipPacket, bestRoute);
                    } else {
                        sendErrorPackage(ipPacket, ControlPacket.Type.TimeExceeded);
                    }

                } else {
                    sendErrorPackage(ipPacket, ControlPacket.Type.DestinationUnreachable);
                }

            } catch (IOException e) {
                System.err.println("Could not receive message");
            }
        }

    }

    private void sendPackage(IpPacket ipPacket, RoutePayload route) {
        ipPacket.setHopLimit(ipPacket.getHopLimit() - 1);
        ipPacket.setNextHopIp(route.getHopAdress());
        ipPacket.setNextPort(route.getHopPort());
        try {
            networkLayer.sendPacket(ipPacket);
        } catch (IOException e) {
            System.err.println("Could not send package");
        }
    }

    private void sendErrorPackage(IpPacket ipPacket, ControlPacket.Type type) {
        System.out.println("Sending Error return Package of type " + type);
        if (!(ipPacket.getType() == IpPacket.Header.Control || ipPacket.getSourceAddress() == null)) {
            RoutePayload bestRoute = routingList.stream().max(Comparator.comparing(item -> item.getBestMatch(ipPacket.getSourceAddress()))).get();
            ipPacket.setDestinationAddress(ipPacket.getSourceAddress());
            ipPacket.setNextHopIp(bestRoute.getHopAdress());
            ipPacket.setNextPort(bestRoute.getHopPort());
            ipPacket.setHopLimit(255);
            ControlPacket controlPacket = new ControlPacket(type, ipPacket.getBytes());
            ipPacket.setControlPayload(controlPacket.getBytes());
            try {
                networkLayer.sendPacket(ipPacket);
            } catch (IOException e) {
                System.err.println("Could not send control package");
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Wrong number of Parameters!!!");
            return;
        }
        int networkLayerPort = Integer.parseInt(args[0]);
        String routesFilePath = args[1];
        Router router = new Router(networkLayerPort, routesFilePath);
        router.sendAndReceive();
    }
}
