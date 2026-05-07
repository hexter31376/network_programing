package dev.wonyoung.application.port.out.week3.day2;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public interface NetworkPort {
    InetAddress resolve(String host) throws UnknownHostException;
    InetAddress[] resolveAll(String host) throws UnknownHostException;
    boolean isReachable(InetAddress addr, int timeoutMs) throws IOException;
}
