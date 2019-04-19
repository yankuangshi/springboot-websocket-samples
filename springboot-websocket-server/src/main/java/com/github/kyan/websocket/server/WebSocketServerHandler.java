package com.github.kyan.websocket.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        handleWebSocketFrame(ctx, frame);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
       log.info("This is a WebSocket frame");
       log.info("Client Channel : " + ctx.channel());
        if (frame instanceof BinaryWebSocketFrame) {
           log.info("BinaryWebSocketFrame Received : {}", ((BinaryWebSocketFrame) frame).content());
        } else if (frame instanceof TextWebSocketFrame) {
           log.info("TextWebSocketFrame Received : {}", ((TextWebSocketFrame) frame).text());
            ctx.channel().writeAndFlush(
                    new TextWebSocketFrame("Message recieved : " + ((TextWebSocketFrame) frame).text()));
           log.info(((TextWebSocketFrame) frame).text());
        } else if (frame instanceof PingWebSocketFrame) {
           log.info("PingWebSocketFrame Received : {}", ((PingWebSocketFrame) frame).content());
        } else if (frame instanceof PongWebSocketFrame) {
           log.info("PongWebSocketFrame Received : {}", ((PongWebSocketFrame) frame).content());
        } else if (frame instanceof CloseWebSocketFrame) {
           log.info("CloseWebSocketFrame Received : ");
           log.info("ReasonText : {}", ((CloseWebSocketFrame) frame).reasonText());
           log.info("StatusCode : {}", ((CloseWebSocketFrame) frame).statusCode());
        } else {
           log.info("Unsupported WebSocketFrame");
        }
    }
}
