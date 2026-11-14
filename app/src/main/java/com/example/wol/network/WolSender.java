package com.example.wol.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;

public class WolSender {

    private static final int WOL_PORT = 9;

    public void send(String macAddress, String targetIpAddress) throws IOException {
        byte[] mac = parseMac(macAddress);
        byte[] packet = new byte[6 + 16 * mac.length];
        Arrays.fill(packet, 0, 6, (byte) 0xFF);
        for (int i = 0; i < 16; i++) {
            System.arraycopy(mac, 0, packet, 6 + i * mac.length, mac.length);
        }

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);

            InetAddress broadcast = getDirectedBroadcast(targetIpAddress);
            socket.send(new DatagramPacket(packet, packet.length, broadcast, WOL_PORT));

            if (!"255.255.255.255".equals(broadcast.getHostAddress())) {
                socket.send(new DatagramPacket(
                        packet,
                        packet.length,
                        InetAddress.getByName("255.255.255.255"),
                        WOL_PORT
                ));
            }
        }
    }

    /**
     * NOTE: пытаемся вычислить directed broadcast подсети, в которой находится
     * target IP. Если не удалось — fallback на 255.255.255.255.
     */
    private InetAddress getDirectedBroadcast(String targetIpAddress) {
        try {
            InetAddress target = InetAddress.getByName(targetIpAddress);
            if (target instanceof Inet4Address) {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface ni = interfaces.nextElement();
                    if (!ni.isUp() || ni.isLoopback()) continue;

                    for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                        InetAddress local = ia.getAddress();
                        if (local instanceof Inet4Address) {
                            int prefix = ia.getNetworkPrefixLength();
                            if (sameSubnet(local, target, prefix)) {
                                InetAddress b = ia.getBroadcast();
                                if (b != null) return b;
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        try {
            return InetAddress.getByName("255.255.255.255");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean sameSubnet(InetAddress a, InetAddress b, int prefix) {
        byte[] aBytes = a.getAddress();
        byte[] bBytes = b.getAddress();
        if (aBytes.length != 4 || bBytes.length != 4) return false;
        if (prefix <= 0) return true;

        int mask = 0xFFFFFFFF << (32 - prefix);
        int aInt = ((aBytes[0] & 0xFF) << 24)
                | ((aBytes[1] & 0xFF) << 16)
                | ((aBytes[2] & 0xFF) << 8)
                | (aBytes[3] & 0xFF);
        int bInt = ((bBytes[0] & 0xFF) << 24)
                | ((bBytes[1] & 0xFF) << 16)
                | ((bBytes[2] & 0xFF) << 8)
                | (bBytes[3] & 0xFF);
        return (aInt & mask) == (bInt & mask);
    }

    private byte[] parseMac(String mac) {
        String[] parts = mac.trim().split("[:-]");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid MAC: " + mac);
        }
        byte[] out = new byte[6];
        for (int i = 0; i < 6; i++) {
            out[i] = (byte) Integer.parseInt(parts[i], 16);
        }
        return out;
    }
}