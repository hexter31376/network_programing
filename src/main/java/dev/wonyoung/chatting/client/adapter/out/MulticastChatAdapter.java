package dev.wonyoung.chatting.client.adapter.out;

import com.google.gson.Gson;
import dev.wonyoung.chatting.client.application.port.out.MulticastPort;
import dev.wonyoung.chatting.share.dto.ProtocolMessage;
import dev.wonyoung.common.container.di.Component;

import java.io.IOException;
import java.net.*;
import java.net.StandardSocketOptions;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Component
public class MulticastChatAdapter implements MulticastPort {

    private static final int BUFFER_SIZE = 4096;

    private final Gson gson = new Gson();
    private volatile MulticastSocket multicastSocket;
    private volatile InetSocketAddress groupSocketAddress;
    private volatile int groupPort;
    // startReceiving은 로그인 전(joinGroup 전)에 호출되므로 핸들러만 저장해둔다.
    // 실제 수신 스레드는 joinGroup() 에서 소켓이 준비된 뒤에 시작한다.
    private volatile Consumer<ProtocolMessage> messageHandler;

    @Override
    public void joinGroup(String address, int port) throws Exception {
        groupPort = port;
        groupSocketAddress = new InetSocketAddress(InetAddress.getByName(address), port);
        multicastSocket = new MulticastSocket(port);
        // 자신이 보낸 패킷도 수신 (입장/퇴장 메시지를 본인 화면에도 표시)
        multicastSocket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
        multicastSocket.joinGroup(groupSocketAddress, null);
        startReceiverThread();
    }

    @Override
    public void send(ProtocolMessage message) throws Exception {
        byte[] data = gson.toJson(message).getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(data, data.length,
            groupSocketAddress.getAddress(), groupPort);
        multicastSocket.send(packet);
    }

    @Override
    public void startReceiving(Consumer<ProtocolMessage> handler) {
        this.messageHandler = handler;
    }

    private void startReceiverThread() {
        Thread receiverThread = new Thread(() -> {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (multicastSocket != null && !multicastSocket.isClosed()) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    multicastSocket.receive(packet);
                    String json = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    if (messageHandler != null) {
                        messageHandler.accept(gson.fromJson(json, ProtocolMessage.class));
                    }
                } catch (IOException e) {
                    if (multicastSocket != null && !multicastSocket.isClosed()) {
                        System.err.println("[클라이언트] 수신 오류: " + e.getMessage());
                    }
                }
            }
        });
        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    @Override
    public void leaveGroup() throws Exception {
        if (multicastSocket != null && !multicastSocket.isClosed()) {
            multicastSocket.leaveGroup(groupSocketAddress, null);
            multicastSocket.close();
        }
    }
}
