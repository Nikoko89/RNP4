import java.net.Inet6Address;
import java.net.UnknownHostException;

public class RoutePayload {
    private Inet6Address targetAdress;
    private String targetNetwork;
    private Inet6Address hopAdress;
    private int hopPort;

    public RoutePayload(String targetAdress, String hopAdress, int hopPort) {
        this.targetAdress = changeToIPv6(targetAdress.split("/")[0]);
        this.targetNetwork = targetAdress;
        this.hopAdress = changeToIPv6(hopAdress);
        this.hopPort = hopPort;
    }


    public Inet6Address getTargetAdress() {
        return targetAdress;
    }

    public String getTargetNetwork() {
        return targetNetwork;
    }

    public Inet6Address getHopAdress() {
        return hopAdress;
    }

    public int getHopPort() {
        return hopPort;
    }


    private Inet6Address changeToIPv6(String adress) {
        Inet6Address inet6Address = null;

        try {
            inet6Address = (Inet6Address) Inet6Address.getByName(adress);
        } catch (UnknownHostException e) {
            System.err.println("Could not convert the given string to an IPv6 adress");
        }
        return inet6Address;
    }

    private String changeToBinary(Inet6Address adress) {
        String[] ipv6 = adress.getHostAddress().split(":");
        String binaryString = "";
        for (String string : ipv6) {
            binaryString += Integer.toBinaryString(Integer.parseInt(string));
        }
        return binaryString;
    }

    public int getBestMatch(Inet6Address adress) {
        String target = changeToBinary(targetAdress);
        String compare = changeToBinary(adress);

        int counter = 0;

        for (int i = 0; i < target.length() && i < compare.length(); i++) {
            if (target.indexOf(i) == compare.indexOf(i)) {
                counter++;
            } else {
                break;
            }
        }
        return counter;
    }
}