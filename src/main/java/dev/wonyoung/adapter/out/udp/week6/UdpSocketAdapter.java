package dev.wonyoung.adapter.out.udp.week6;

import dev.wonyoung.domain.model.ChatMessage;
import dev.wonyoung.domain.port.out.UdpSocketPort;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UdpSocketAdapter implements UdpSocketPort {

    private final DatagramSocket socket;
    private volatile InetAddress remoteAddress;
    private volatile int remotePort;

    /** 서버 모드: 로컬 포트만 지정, 상대 주소는 수신 패킷에서 동적으로 파악 */
    public UdpSocketAdapter(int localPort) throws SocketException {
        this.socket = new DatagramSocket(localPort);
    }

    /** 클라이언트 모드: 로컬 포트 + 고정 원격 엔드포인트 지정 */
    public UdpSocketAdapter(int localPort, InetAddress remoteAddress, int remotePort) throws SocketException {
        this.socket = new DatagramSocket(localPort);
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
    }

    @Override
    public void send(String message) throws IOException {
        if (remoteAddress == null) {
            throw new IOException("원격 엔드포인트 미설정 - 먼저 패킷을 수신해야 합니다");
        }
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, remoteAddress, remotePort);
        socket.send(packet);
    }

    @Override
    public ChatMessage receive() throws IOException {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        remoteAddress = packet.getAddress();
        remotePort = packet.getPort();
        int length = packet.getLength();
        String content = new String(packet.getData(), 0, length);
        return new ChatMessage(remoteAddress, remotePort, length, content);
    }

    @Override
    public boolean isClosed() {
        return socket.isClosed();
    }

    @Override
    public void close() {
        socket.close();
    }
}
