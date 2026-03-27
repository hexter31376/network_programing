package dev.wonyoung.application.port.in.week3.day2;

import dev.wonyoung.application.domain.week3.day2.NetworkInfo;
import dev.wonyoung.application.domain.week3.day2.PingResult;

import java.util.List;

public interface NetworkUseCase {
    NetworkInfo lookupIp(String host);
    List<NetworkInfo> lookupAllIps(String host);
    PingResult ping(String host, int timeoutMs);
}
