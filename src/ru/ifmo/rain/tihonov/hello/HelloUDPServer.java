package ru.ifmo.rain.tihonov.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class HelloUDPServer {
    public static void main(String[] args) throws IOException { //todo remove
        DatagramSocket ds = new DatagramSocket(1488);

        byte[] result = new byte[64];
        DatagramPacket dp = new DatagramPacket(result, result.length);
        ds.receive(dp);

        System.out.println("Server get: " + Arrays.toString(result));

        for (int i = 0; i < result.length; i++) {
            if (result[i] != 0) {
                result[i]++;
            } else {
                break;
            }
        }

        InetAddress clientIP = dp.getAddress();
        int clientPort = dp.getPort();

        DatagramPacket response = new DatagramPacket(result, result.length, clientIP, clientPort);
        ds.send(response);
    }
}
