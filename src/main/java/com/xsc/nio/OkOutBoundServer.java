package com.xsc.nio;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.DefaultThreadFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Jakexsc
 * 2022/4/17
 */
public class OkOutBoundServer {
    private OkHttpClient okHttpClient;
    private ExecutorService service;
    private String backendUrl;

    public OkOutBoundServer(final String backendUrl) {
        System.out.println("backendUrl = " + backendUrl);
        this.backendUrl = backendUrl.endsWith("/") ? backendUrl.substring(0, backendUrl.length() - 1) : backendUrl;
        System.out.println(this.backendUrl);
        int core = Runtime.getRuntime().availableProcessors() * 2;
        this.service = new ThreadPoolExecutor(core, core, 1000, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(2048), new DefaultThreadFactory("proxyServer"),
                new ThreadPoolExecutor.CallerRunsPolicy());
        this.okHttpClient = new OkHttpClient();
    }

    public void handle(final FullHttpRequest request, final ChannelHandlerContext ctx) {
        String requestUri = request.uri();
        final String uri = this.backendUrl + requestUri;
        service.submit(() -> getResponse(request, ctx, uri));
    }

    private void getResponse(final FullHttpRequest fullHttpRequest, final ChannelHandlerContext ctx, final String uri) {
        Request request = new Request.Builder().url(uri).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            handleResponse(fullHttpRequest, ctx, response);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void handleResponse(final FullHttpRequest fullHttpRequest, final ChannelHandlerContext ctx, final Response response) {
        FullHttpResponse fullHttpResponse = null;
        try {
            byte[] body = response.body().bytes();
            fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(body));
            fullHttpResponse.headers().set("Content-Type", "application/json");
            fullHttpResponse.headers().setInt("Content-Length", Integer.parseInt(Objects.requireNonNull(response.headers().get("Content-Length"))));

        } catch (IOException e) {
            e.printStackTrace();
            fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NO_CONTENT);
            exceptionCaught(ctx, e);
        } finally {
            if (fullHttpRequest != null) {
                if (!HttpUtil.isKeepAlive(fullHttpRequest)) {
                    ctx.write(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
                } else {
                    ctx.write(fullHttpResponse);
                }
            }
            ctx.flush();
        }
    }

    private void exceptionCaught(final ChannelHandlerContext ctx, final IOException e) {

    }
}
