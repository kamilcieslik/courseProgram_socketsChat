import java.io.Serializable;

public class BannedIp implements Serializable {

    private static final long serialVersionUID = 1L;
    private String ip;
    private String reason;

    BannedIp(String ip, String reason) {
        this.ip = ip;
        this.reason = reason;
    }

    String getIp() {
        return ip;
    }

    String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return ("IP: " + ip + ", powód: " + reason);
    }
}
