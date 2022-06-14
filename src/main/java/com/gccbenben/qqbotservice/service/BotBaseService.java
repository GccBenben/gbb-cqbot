package com.gccbenben.qqbotservice.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

/**
 * 机器人基础服务
 *
 * @author GccBenben
 * @date 2022/05/25
 */
@Service
public interface BotBaseService {

    boolean sendPrivateMessage(String message, String id);

    ObjectNode queryFriendList();

    boolean sendGroupMessage(String message, String groupId);

    boolean sendMessageAuto(String message, ObjectNode object);

    void rollBackGroupMessage(String groupId);

    void rollBackPrivateMessage(String id);

    void updateLastMessageInfo(String echoId, String messageId);

    void sendGroupMessageForward(ArrayNode message, String groupId);
}
