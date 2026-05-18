package dev.wonyoung.chatting.share.dto;

import dev.wonyoung.chatting.share.domain.Protocol;

public class ProtocolMessage {

    public Protocol type;
    public String sender;
    public String message;
    public String reason;
    public String multicastAddress;
    public int multicastPort;

    public static ProtocolMessage login(String sender) {
        ProtocolMessage m = new ProtocolMessage();
        m.type = Protocol.LOGIN;
        m.sender = sender;
        return m;
    }

    public static ProtocolMessage loginSuccess(String address, int port) {
        ProtocolMessage m = new ProtocolMessage();
        m.type = Protocol.LOGIN_SUCCESS;
        m.multicastAddress = address;
        m.multicastPort = port;
        return m;
    }

    public static ProtocolMessage loginFail(String reason) {
        ProtocolMessage m = new ProtocolMessage();
        m.type = Protocol.LOGIN_FAIL;
        m.reason = reason;
        return m;
    }

    public static ProtocolMessage logout(String sender) {
        ProtocolMessage m = new ProtocolMessage();
        m.type = Protocol.LOGOUT;
        m.sender = sender;
        return m;
    }

    public static ProtocolMessage logoutSuccess() {
        ProtocolMessage m = new ProtocolMessage();
        m.type = Protocol.LOGOUT_SUCCESS;
        return m;
    }

    public static ProtocolMessage chat(String sender, String message) {
        ProtocolMessage m = new ProtocolMessage();
        m.type = Protocol.CHAT;
        m.sender = sender;
        m.message = message;
        return m;
    }

    public static ProtocolMessage join(String sender) {
        ProtocolMessage m = new ProtocolMessage();
        m.type = Protocol.JOIN;
        m.sender = sender;
        return m;
    }

    public static ProtocolMessage leave(String sender) {
        ProtocolMessage m = new ProtocolMessage();
        m.type = Protocol.LEAVE;
        m.sender = sender;
        return m;
    }
}
