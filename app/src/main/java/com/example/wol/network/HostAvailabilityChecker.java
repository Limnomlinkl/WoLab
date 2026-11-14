package com.example.wol.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class HostAvailabilityChecker {

    public boolean isAvailable(String ipAddress, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ipAddress, port), timeoutMs);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}