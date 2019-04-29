package ru.ifmo.rain.tihonov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class HelloUDPClient implements HelloClient {
//    имя или ip-адрес компьютера, на котором запущен сервер;
//    номер порта, на который отсылать запросы;
//    префикс запросов (строка);
//    число параллельных потоков запросов;
//    число запросов в каждом потоке.


    private static byte[] getMessage(String prefix, int threadNumber, int requestNumber) {
        String result = prefix + threadNumber + "_" + requestNumber;
        return result.getBytes(StandardCharsets.UTF_8);
    }

//    public static void main(String[] args) throws IOException { //todo remove
//        String name = args[0];
//        int port = Integer.parseInt(args[1]);
//        String prefix = args[2];
//        int threads = Integer.parseInt(args[3]);
//        int requests = Integer.parseInt(args[4]);
//
//        InetAddress ip = InetAddress.getByName(name);
//
//        List<Thread> threadList = new ArrayList<>();
//        for (int i = 0; i < threads; i++) {
//            final int threadNumber = i;
//            threadList.add(new Thread(() -> {
//                try {
//                    for (int index = 0; index < requests; index++) {
//                        byte[] message = getMessage(prefix, threadNumber, index);
//
//                        DatagramSocket socket = new DatagramSocket();
//                        DatagramPacket packet = new DatagramPacket(message, message.length, ip, port);
//
//                        socket.send(packet);
//
//                        byte[] response = new byte[1024];
//                        packet = new DatagramPacket(response, response.length);
//                        socket.receive(packet);
//
//                        System.out.println(new String(response, 0, packet.getLength()));
//                    }
//                } catch (IOException e) {
//
//                }
//            }));
//            threadList.get(i).start();
//    }


//        InetAddress ip = InetAddress.getLocalHost();
//        byte[] buf = {50, 48, 47};
//
//        DatagramSocket ds = new DatagramSocket();
//        DatagramPacket dp = new DatagramPacket(buf, buf.length, ip, 1488);
//
//        ds.send(dp);
//
//        byte[] buffer = new byte[64];
//        DatagramPacket response = new DatagramPacket(buffer, buffer.length);
//        ds.receive(response);
//
//        System.out.println(Arrays.toString(buffer));
//}

    @Override
    public void run(String name, int port, String prefix, int threads, int requests) {
        InetAddress ip;

//        if (threads < 1 || port < 0 || requests < 1) {
//            throw new IllegalArgumentException();
//        }

        try {
            ip = InetAddress.getByName(name);
        } catch (IOException e) {
            System.out.println("9!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }


        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            final int threadNumber = i;
            threadList.add(new Thread(() -> {
                try (DatagramSocket socket = new DatagramSocket()) {
                    for (int index = 0; index < requests; index++) {
                        byte[] message = getMessage(prefix, threadNumber, index);

                        DatagramPacket packet = new DatagramPacket(message, message.length, ip, port);

                        while (true) {
                            try {
                                socket.setSoTimeout(100);
                                socket.send(packet);

                                byte[] buffer = new byte[2048];
                                DatagramPacket response = new DatagramPacket(buffer, buffer.length);

                                if (index <= 1) System.out.print('%');

                                socket.receive(response);

                                if (index <= 1) System.out.print('$');

                                String result = new String(response.getData(), response.getOffset(), response.getLength());

                                if (index <= 1) System.out.println(result);
                                if (result.contains("_")) {
                                    break;
                                }
                            } catch (Exception e) {
                                System.out.println("Err" + e.getMessage());
                                if (e.getMessage().startsWith("Re")) continue;
                            }
                        }
                    }

//                    if (threadNumber == 0) {
//                        System.out.println("???????????/");
//                    }

                } catch (Exception e) {
                    System.out.println("ERROR");
//                    throw new IllegalArgumentException();
                }
            }));
            threadList.get(i).start();
        }

        for (int i = 0; i < threads; i++) {
            try {
                threadList.get(i).join();
            } catch (InterruptedException e) {
            }
        }
    }
}