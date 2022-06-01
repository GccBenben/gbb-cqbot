package com.gccbenben.qqbotservice.config;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gccbenben.qqbotservice.component.BotMessageHandler;
import com.gccbenben.qqbotservice.component.BotWebSocketServer;
import com.gccbenben.qqbotservice.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * websocket客户端配置
 *
 * @author GccBenben
 * @date 2022/06/01
 */
@Slf4j
//@Component
public class WebSocketClientConfig {

    @Value("${server-websocket-uri}")
    private String webSocketClientURI;

    private static BotMessageHandler botMessageHandler;

    @Autowired
    public void setBotMessageHandler(BotMessageHandler botMessageHandler) {
        WebSocketClientConfig.botMessageHandler = botMessageHandler;
    }

    @Bean
    public WebSocketClient webSocketClient() {
        try {
            WebSocketClient webSocketClient = new WebSocketClient(new URI(webSocketClientURI), new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    log.info("websocket handshake success");
                }

                @Override
                public void onMessage(String message) {
                    botMessageHandler.messageHandle(message, log);
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    log.info("close websocket");
                }

                @Override
                public void onError(Exception e) {
                    log.error("socket connect error", e);
                }
            };
            webSocketClient.connect();
            return webSocketClient;
        } catch (Exception e) {
            log.error("websocket连接建立失败", e);
        }
        return null;
    }
}
