package dev.wonyoung.adapter.out.network.week3.day2;

import dev.wonyoung.application.port.out.week3.day2.NetworkPort;
import dev.wonyoung.infrastructure.container.di.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class InetAddressAdapter implements NetworkPort {

    @Override
    public InetAddress resolve(String host) throws UnknownHostException {
        return InetAddress.getByName(host);
    }

    @Override
    public InetAddress[] resolveAll(String host) throws UnknownHostException {
        return InetAddress.getAllByName(host);
    }

    @Override
    public boolean isReachable(InetAddress addr, int timeoutMs) throws IOException {
        return addr.isReachable(timeoutMs);
    }
}
