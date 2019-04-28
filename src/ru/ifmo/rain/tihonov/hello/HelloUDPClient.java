package ru.ifmo.rain.tihonov.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class HelloUDPClient {
//    имя или ip-адрес компьютера, на котором запущен сервер;
//    номер порта, на который отсылать запросы;
//    префикс запросов (строка);
//    число параллельных потоков запросов;
//    число запросов в каждом потоке.

    public static void main(String[] args) throws IOException { //todo remove
        InetAddress ip = InetAddress.getLocalHost();
        byte[] buf = {50, 48, 47};

        DatagramSocket ds = new DatagramSocket();
        DatagramPacket dp = new DatagramPacket(buf, buf.length, ip, 1488);

        System.out.println(1);
        ds.send(dp);
        System.out.println(2);

        byte[] buffer = new byte[64];
        DatagramPacket response = new DatagramPacket(buffer, buffer.length);
        ds.receive(response);

        System.out.println(Arrays.toString(buffer));
    }
}
