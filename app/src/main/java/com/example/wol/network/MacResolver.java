package com.example.wol.network;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

public class MacResolver {

    private static final String MAC_REGEX = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";

    /**
     * Пытается определить MAC по IP без root.
     * Сначала /proc/net/arp, затем `ip neigh show <ip>`.
     * При любой ошибке возвращает null — вызывающий код показывает ручной ввод.
     */
    public String resolve(String ip) {
        String mac = resolveFromArp(ip);
        if (mac == null) {
            mac = resolveFromIpNeigh(ip);
        }
        return mac;
    }

    private String resolveFromArp(String ip) {
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 4 && parts[0].equals(ip)) {
                    String mac = parts[3];
                    if (mac.matches(MAC_REGEX)) {
                        return mac;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String resolveFromIpNeigh(String ip) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"ip", "neigh", "show", ip});
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.trim().split("\\s+");
                    for (String part : parts) {
                        if (part.matches(MAC_REGEX)) {
                            return part;
                        }
                    }
                }
            }
            process.waitFor();
        } catch (Exception ignored) {
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return null;
    }
}