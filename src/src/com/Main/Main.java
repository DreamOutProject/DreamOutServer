package com.Main;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws UnknownHostException {
        int port=54321;
        new GameServer(port);
    }
}
