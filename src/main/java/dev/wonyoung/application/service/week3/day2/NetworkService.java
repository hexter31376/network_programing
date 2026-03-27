package dev.wonyoung.application.service.week3.day2;

import dev.wonyoung.application.domain.week3.day2.NetworkInfo;
import dev.wonyoung.application.domain.week3.day2.PingResult;
import dev.wonyoung.application.exception.AppException;
import dev.wonyoung.application.port.in.week3.day2.NetworkUseCase;
import dev.wonyoung.application.port.out.week3.day2.NetworkPort;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Component
public class NetworkService implements NetworkUseCase {

    private final NetworkPort networkPort;

    @Inject
    public NetworkService(NetworkPort networkPort) {
        this.networkPort = networkPort;
    }

    @Override
    public NetworkInfo lookupIp(String host) {
        try {
            InetAddress addr = networkPort.resolve(host);
            return new NetworkInfo(host, addr.getHostName(), addr.getCanonicalHostName(), addr.getHostAddress());
        } catch (UnknownHostException e) {
            throw new AppException("Unknown host: " + host, e);
        }
    }

    @Override
    public List<NetworkInfo> lookupAllIps(String host) {
        try {
            InetAddress[] addrs = networkPort.resolveAll(host);
            List<NetworkInfo> result = new ArrayList<>();
            for (InetAddress addr : addrs) {
                result.add(new NetworkInfo(host, addr.getHostName(), addr.getCanonicalHostName(), addr.getHostAddress()));
            }
            return result;
        } catch (UnknownHostException e) {
            throw new AppException("Unknown host: " + host, e);
        }
    }

    @Override
    public PingResult ping(String host, int timeoutMs) {
        try {
            InetAddress addr = networkPort.resolve(host);
            long start = System.currentTimeMillis();
            boolean reachable = networkPort.isReachable(addr, timeoutMs);
            long elapsed = System.currentTimeMillis() - start;
            return new PingResult(host, addr.getHostAddress(), reachable, elapsed, timeoutMs);
        } catch (UnknownHostException e) {
            throw new AppException("Unknown host: " + host, e);
        } catch (IOException e) {
            throw new AppException("Network error: " + e.getMessage(), e);
        }
    }
}
