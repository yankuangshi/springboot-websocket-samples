package com.github.kyan.websocket.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.UPGRADE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private WebSocketServerHandshaker handshaker;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        handleHttpRequest(ctx, req);
    }

    /**
     * 处理客户端向服务端发起的http握手请求业务
     * 如果客户端需要建立websocket连接，则请求内容应该如下：
     * ----------------------------------
     * | GET /chat HTTP/1.1             |
     * | Host: server.example.com       |
     * | Upgrade: websocket             |
     * | Connection: Upgrade            |
     * | Origin: http://example.com     |
     * ----------------------------------
     * @param ctx
     * @param req
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        log.info("Http request received");
        HttpHeaders headers = req.headers();
        log.info("Connection: {}", headers.get(CONNECTION));
        log.info("Upgrade: {}", headers.get(UPGRADE));

        if ("Upgrade".equalsIgnoreCase(headers.get(CONNECTION))
                && "WebSocket".equalsIgnoreCase(headers.get(UPGRADE))) {
            log.info("Add WebSocketServerHandler to pipeline");
            ctx.pipeline().remove(ctx.name());
            ctx.pipeline().addLast(new WebSocketServerHandler());
            handleHandShake(ctx, req);
        } else {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
        }
    }

    /**
     * 处理握手请求
     * @param ctx
     * @param req
     */
    private void handleHandShake(ChannelHandlerContext ctx, FullHttpRequest req) {
        log.info("Handshaking...");
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(req), null, true);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
        log.info("Handshake is done");
    }


    /**
     * 服务端向客户端发送响应消息
     * @param ctx
     * @param req
     * @param res
     */
    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        int statusCode = res.status().code();
        if (statusCode != OK.code() && res.content().readableBytes() == 0) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        HttpUtil.setContentLength(res, res.content().readableBytes());
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || statusCode != OK.code()) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private String getWebSocketURL(FullHttpRequest req) {
        String url = Constants.ENDPOINT + ":" + Constants.PORT + req.uri();
        return url;
    }
}
