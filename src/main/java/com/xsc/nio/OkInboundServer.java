package com.xsc.nio;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.logging.Level;

/**
 * @author Jakexsc
 * 2022/4/17
 */
public class OkInboundServer {
    private Integer port;
    private String proxyServer;

    public OkInboundServer() {

    }

    public OkInboundServer(Integer port, String proxyServer) {
        this.port = port;
        this.proxyServer = proxyServer;
    }

    public void run() throws InterruptedException {
        NioEventLoopGroup bossEventLoopGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workEventLoopGroup = new NioEventLoopGroup(16);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, false)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                    .option(ChannelOption.SO_SNDBUF, 32 * 1024)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

            b.group(bossEventLoopGroup, workEventLoopGroup).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO)).childHandler(new OkInboundInitializer(this.proxyServer));

            Channel channel = b.bind(this.port).sync().channel();
            System.out.println("开启Netty服务器,监听地址和端口为: http://127.0.0.1:" + this.port + "/");
            channel.closeFuture().sync();
        } finally {
            bossEventLoopGroup.shutdownGracefully();
            workEventLoopGroup.shutdownGracefully();
        }
    }
}
