package com.github.kyan.websocket.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    WebSocketServerHandshaker handshaker;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        handlerHttpRequest(ctx, req);
    }

    private void handlerHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        log.info("Http request received");
        HttpHeaders headers = req.headers();
        log.info("Connection: {}", headers.get("Connection"));
        log.info("Upgrade: {}", headers.get("Upgrade"));

        if ("Upgrade".equalsIgnoreCase(headers.get(HttpHeaderNames.CONNECTION))
                && "WebSocket".equalsIgnoreCase(headers.get(HttpHeaderNames.UPGRADE))) {
            log.info("Add WebSocketServerHandler to pipeline");
            ctx.pipeline().remove(ctx.name());
            ctx.pipeline().addLast(new WebSocketServerHandler());
            log.info("Handshaking...");
            handleHandShake(ctx, req);
            log.info("Handshake is done");
        }
    }

    private void handleHandShake(ChannelHandlerContext ctx, FullHttpRequest req) {
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(req), null, true);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    private String getWebSocketURL(FullHttpRequest req) {
        String url = "ws://" + req.headers().get(HttpHeaderNames.HOST) + req.uri();
        return url;
    }
}
