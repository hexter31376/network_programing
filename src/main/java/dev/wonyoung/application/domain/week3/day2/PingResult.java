package dev.wonyoung.application.domain.week3.day2;

public class PingResult {

    private final String host;
    private final String ipAddress;
    private final boolean reachable;
    private final long elapsedMs;
    private final int timeoutMs;

    public PingResult(String host, String ipAddress, boolean reachable, long elapsedMs, int timeoutMs) {
        this.host = host;
        this.ipAddress = ipAddress;
        this.reachable = reachable;
        this.elapsedMs = elapsedMs;
        this.timeoutMs = timeoutMs;
    }

    public String getHost() { return host; }
    public String getIpAddress() { return ipAddress; }
    public boolean isReachable() { return reachable; }
    public long getElapsedMs() { return elapsedMs; }
    public int getTimeoutMs() { return timeoutMs; }
}
