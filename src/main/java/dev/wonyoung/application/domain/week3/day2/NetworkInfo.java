package dev.wonyoung.application.domain.week3.day2;

public class NetworkInfo {

    private final String input;
    private final String hostName;
    private final String canonicalHostName;
    private final String ipAddress;

    public NetworkInfo(String input, String hostName, String canonicalHostName, String ipAddress) {
        this.input = input;
        this.hostName = hostName;
        this.canonicalHostName = canonicalHostName;
        this.ipAddress = ipAddress;
    }

    public String getInput() { return input; }
    public String getHostName() { return hostName; }
    public String getCanonicalHostName() { return canonicalHostName; }
    public String getIpAddress() { return ipAddress; }
}
