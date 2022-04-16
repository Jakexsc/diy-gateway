package com.xsc.nio;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;

/**
 * @author Jakexsc
 * 2022/4/17
 */
public class OkInboundHandle extends ChannelInboundHandlerAdapter {
    private String proxyServer;
    private OkOutBoundServer okOutBoundServer;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            this.okOutBoundServer.handle(fullHttpRequest, ctx);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    public OkInboundHandle(String proxyServer) {
        this.proxyServer = proxyServer;
        this.okOutBoundServer = new OkOutBoundServer(this.proxyServer);
    }
}
