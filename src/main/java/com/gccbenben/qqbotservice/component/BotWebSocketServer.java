package com.gccbenben.qqbotservice.component;

import java.io.IOException;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gccbenben.qqbotservice.config.WebSocketClientConfig;
import com.gccbenben.qqbotservice.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


/**
 * 机器人网络套接字服务器
 *
 * @author GccBenben
 * @date 2022/05/25
 */
@Slf4j
@ServerEndpoint("/api/message")
@Component
@Service
public class BotWebSocketServer {

    private static CopyOnWriteArraySet<BotWebSocketServer> webSocketSet = new CopyOnWriteArraySet<BotWebSocketServer>();

    private Session session;

    private String sid;

    private static BotMessageHandler botMessageHandler;

    @Autowired
    public void setBotMessageHandler(BotMessageHandler botMessageHandler) {
        BotWebSocketServer.botMessageHandler = botMessageHandler;
    }


    @OnOpen
    public void opOpen(Session session) {
        this.session = session;
        webSocketSet.add(this);
        this.sid = UUID.randomUUID().toString();
        log.info("new qq bot web socket open, sid: " + this.sid);
    }

    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        log.info("release websocket sid: " + this.sid);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        botMessageHandler.messageHandle(message, log);
    }

    @OnError
    public void onError(Session session, Throwable error) {

        log.error("error reason: " + error.getMessage());
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 发送消息
     *
     * @param sendMessage 发送消息
     * @param sid         sid
     */
    public static void sendMessage(String sendMessage, String sid) {
        for (BotWebSocketServer server : webSocketSet) {
            try {
                if (sid == null) {
                    server.sendMessage(sendMessage);
                    log.info("sendMessage: " + sendMessage);
                } else if (sid.equals(server.sid)) {
                    server.sendMessage(sendMessage);
                    log.info("sendMessage: " + sendMessage);
                }

            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }

    }
}
