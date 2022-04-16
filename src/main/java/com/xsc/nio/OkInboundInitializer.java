package com.xsc.nio;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * @author Jakexsc
 * 2022/4/17
 */
public class OkInboundInitializer extends ChannelInitializer<SocketChannel> {
    private String proxyServer;
    private OkOutBoundServer okOutBoundServer;

    public OkInboundInitializer() {
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(1024 * 1024));
        pipeline.addLast(new OkInboundHandle(this.proxyServer));

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest request = (FullHttpRequest) msg;
        okOutBoundServer.handle(request, ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    public OkInboundInitializer(String proxyServer) {
        this.proxyServer = proxyServer;
        okOutBoundServer = new OkOutBoundServer(proxyServer);
    }
}
