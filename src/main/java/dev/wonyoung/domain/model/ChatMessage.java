package dev.wonyoung.domain.model;

import java.net.InetAddress;

public record ChatMessage(InetAddress address, int port, int dataLength, String content) {
}
