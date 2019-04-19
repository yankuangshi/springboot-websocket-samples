package com.github.kyan.websocket.socket;

import com.github.kyan.websocket.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Configuration
public class WebSocketClientConfig {

    @Bean
    public WebSocketClient webSocketClient() {
        return new StandardWebSocketClient();
    }

    @Bean
    public WebSocketHandler webSocketHandler() {
        return new DefaultWebSocketHandler();
    }

    @Bean
    public WebSocketConnectionManager connectionManager(final WebSocketClient webSocketClient,
                                                        final WebSocketHandler webSocketHandler) {
        final WebSocketConnectionManager connectionManager = new WebSocketConnectionManager(webSocketClient,
                webSocketHandler, Constants.ENDPOINT);
        connectionManager.setAutoStartup(true);
        return connectionManager;
    }
}
