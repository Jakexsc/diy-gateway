package com.xsc;

import com.xsc.nio.OkInboundServer;

/**
 * @author Jakexsc
 * 2022/4/17
 */
public class NettyApplication {
    public static final String GATEWAY_NAME = "NioGateWay";
    public static final String GATEWAY_VERSION = "1.0.0";

    public static void main(String[] args) {
        String proxyServer = System.getProperty("proxyServer", "http://localhost:8081");
        String proxyPort = System.getProperty("proxyPort", "8888");

        int port = Integer.parseInt(proxyPort);
        System.out.println(GATEWAY_NAME + " " + GATEWAY_VERSION + " " + "starting...");
        OkInboundServer okInboundServer = new OkInboundServer(port, proxyServer);
        System.out.println(GATEWAY_NAME + " " + GATEWAY_VERSION + " started at http://localhost:" + port + " for server:" + proxyServer);
        try {
            okInboundServer.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
