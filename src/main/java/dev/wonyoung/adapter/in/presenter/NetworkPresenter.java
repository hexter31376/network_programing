package dev.wonyoung.adapter.in.presenter;

import dev.wonyoung.adapter.in.view.NetworkViewImpl;
import dev.wonyoung.application.domain.week3.day2.NetworkInfo;
import dev.wonyoung.application.domain.week3.day2.PingResult;
import dev.wonyoung.application.exception.AppException;
import dev.wonyoung.application.port.in.week3.day2.NetworkUseCase;
import dev.wonyoung.infrastructure.container.di.Component;
import dev.wonyoung.infrastructure.container.di.Inject;
import dev.wonyoung.view.NetworkView;

import javax.swing.*;
import java.util.List;

@Component
public class NetworkPresenter {

    private static final String ERR_EMPTY_HOST         = "[ERROR] Please enter a hostname or IP.";
    private static final String ERR_PREFIX             = "[ERROR] ";

    private static final String HEADER_LOOKUP_IP       = "=== IP Lookup Result ===\n";
    private static final String LABEL_INPUT            = "Input            : ";
    private static final String LABEL_HOSTNAME         = "Hostname         : ";
    private static final String LABEL_CANONICAL        = "CanonicalHostName: ";
    private static final String LABEL_IP               = "IP Address       : ";

    private static final String HEADER_LOOKUP_ALL      = "=== All IPs Lookup Result ===\n";
    private static final String LABEL_INPUT_SHORT      = "Input : ";
    private static final String LABEL_TOTAL_PREFIX     = "Total : ";
    private static final String LABEL_TOTAL_SUFFIX     = "\n\n";
    private static final String FORMAT_ALL_IP_ROW      = "[%d] Hostname: %-30s IP: %s%n";

    private static final String MSG_PING_IN_PROGRESS   = "Pinging... (";
    private static final String MSG_PING_CLOSE_PAREN   = ")";
    private static final String HEADER_PING            = "=== Ping Test Result ===\n";
    private static final String LABEL_PING_HOST        = "Target  : ";
    private static final String LABEL_PING_IP          = "IP      : ";
    private static final String LABEL_PING_RESULT      = "Result  : ";
    private static final String MSG_REACHABLE          = "Reachable\n";
    private static final String LABEL_ELAPSED          = "Elapsed : ";
    private static final String UNIT_MS                = "ms\n";
    private static final String MSG_UNREACHABLE_PREFIX = "Unreachable (timeout ";
    private static final String MSG_UNREACHABLE_SUFFIX = "ms)\n";
    private static final String MSG_FIREWALL_NOTE      = "* Firewall or ICMP blocking may be the cause.\n";

    private final NetworkUseCase networkUseCase;
    private NetworkView view;

    @Inject
    public NetworkPresenter(NetworkUseCase networkUseCase) {
        this.networkUseCase = networkUseCase;
    }

    public void start() {
        SwingUtilities.invokeLater(() -> {
            view = new NetworkViewImpl(
                    this::onLookupIp,
                    this::onLookupAllIps,
                    this::onPingTest
            );
            view.show();
        });
    }

    private void onLookupIp() {
        String host = view.getHost();
        if (host.isEmpty()) {
            view.showResult(ERR_EMPTY_HOST);
            return;
        }
        try {
            NetworkInfo info = networkUseCase.lookupIp(host);
            view.showResult(
                    HEADER_LOOKUP_IP +
                    LABEL_INPUT + info.getInput() + "\n" +
                    LABEL_HOSTNAME + info.getHostName() + "\n" +
                    LABEL_CANONICAL + info.getCanonicalHostName() + "\n" +
                    LABEL_IP + info.getIpAddress() + "\n"
            );
        } catch (AppException e) {
            view.showResult(ERR_PREFIX + e.getMessage());
        }
    }

    private void onLookupAllIps() {
        String host = view.getHost();
        if (host.isEmpty()) {
            view.showResult(ERR_EMPTY_HOST);
            return;
        }
        try {
            List<NetworkInfo> infos = networkUseCase.lookupAllIps(host);
            StringBuilder sb = new StringBuilder();
            sb.append(HEADER_LOOKUP_ALL);
            sb.append(LABEL_INPUT_SHORT).append(host).append("\n");
            sb.append(LABEL_TOTAL_PREFIX).append(infos.size()).append(LABEL_TOTAL_SUFFIX);
            for (int i = 0; i < infos.size(); i++) {
                sb.append(String.format(FORMAT_ALL_IP_ROW,
                        i + 1, infos.get(i).getHostName(), infos.get(i).getIpAddress()));
            }
            view.showResult(sb.toString());
        } catch (AppException e) {
            view.showResult(ERR_PREFIX + e.getMessage());
        }
    }

    private void onPingTest() {
        String host = view.getHost();
        if (host.isEmpty()) {
            view.showResult(ERR_EMPTY_HOST);
            return;
        }
        view.showResult(MSG_PING_IN_PROGRESS + host + MSG_PING_CLOSE_PAREN);

        new Thread(() -> {
            try {
                PingResult ping = networkUseCase.ping(host, 3000);
                StringBuilder sb = new StringBuilder();
                sb.append(HEADER_PING);
                sb.append(LABEL_PING_HOST).append(ping.getHost()).append("\n");
                sb.append(LABEL_PING_IP).append(ping.getIpAddress()).append("\n");
                if (ping.isReachable()) {
                    sb.append(LABEL_PING_RESULT).append(MSG_REACHABLE);
                    sb.append(LABEL_ELAPSED).append(ping.getElapsedMs()).append(UNIT_MS);
                } else {
                    sb.append(LABEL_PING_RESULT)
                      .append(MSG_UNREACHABLE_PREFIX).append(ping.getTimeoutMs()).append(MSG_UNREACHABLE_SUFFIX);
                    sb.append(MSG_FIREWALL_NOTE);
                }
                view.showResult(sb.toString());
            } catch (AppException e) {
                view.showResult(ERR_PREFIX + e.getMessage());
            }
        }).start();
    }
}
