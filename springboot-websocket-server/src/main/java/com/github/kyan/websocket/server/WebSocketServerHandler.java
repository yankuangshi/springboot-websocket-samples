package com.github.kyan.websocket.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket Protocol: https://tools.ietf.org/html/rfc6455
 */
@Slf4j
public class WebSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        handleWebSocketFrame(ctx, frame);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 处理客户端向服务端发起的websocket请求
     * @param ctx
     * @param frame
     */
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
       log.info("This is a WebSocket frame");
       log.info("Client Channel : " + ctx.channel());
        if (frame instanceof BinaryWebSocketFrame) {
            //二进制请求
           log.info("BinaryWebSocketFrame Received : {}", ((BinaryWebSocketFrame) frame).content());
        } else if (frame instanceof PingWebSocketFrame) {
            //PING请求
           log.info("PingWebSocketFrame Received : {}", ((PingWebSocketFrame) frame).content());
           ctx.channel().write(new PongWebSocketFrame(((PingWebSocketFrame) frame).content().retain()));
        } else if (frame instanceof CloseWebSocketFrame) {
            //请求关闭websocket
           log.info("CloseWebSocketFrame Received with ReasonText: {}, StatusCode: {}",
                   ((CloseWebSocketFrame) frame).reasonText(),
                   ((CloseWebSocketFrame) frame).statusCode());
           ctx.close();
        } else if (frame instanceof TextWebSocketFrame) {
            String request = ((TextWebSocketFrame) frame).text();
            log.info("TextWebSocketFrame Received : {}", request);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(ctx.channel().id() + ":" + request));
        } else {
           log.error("Unsupported WebSocketFrame");
           throw new RuntimeException("Unsupported WebSocketFrame");
        }
    }

}
