package com.gccbenben.qqbotservice.service.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gccbenben.qqbotservice.bean.GroupInfo;
import com.gccbenben.qqbotservice.bean.PrivateUserInfo;
import com.gccbenben.qqbotservice.bean.realWebSocketRequestMessage.SendGroupMessageRequestObject;
import com.gccbenben.qqbotservice.bean.realWebSocketRequestMessage.SendPrivateMessageRequestObject;
import com.gccbenben.qqbotservice.component.BotWebSocketServer;
import com.gccbenben.qqbotservice.component.RedisUtil;
import com.gccbenben.qqbotservice.service.BotBaseService;
import com.gccbenben.qqbotservice.utils.HttpUtil;
import com.gccbenben.qqbotservice.utils.JSONUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BotBaseServiceImpl implements BotBaseService {

    @Value("${qq-bot-end-point}")
    private String botEndPoint;

    @Autowired
    private RedisUtil redisUtil;


    /**
     * 通过websocket发送私人消息
     *
     * @param message 消息
     * @param id      id
     * @return boolean
     */
    @Override
    public boolean sendPrivateMessage(String message, String id) {
        SendPrivateMessageRequestObject object = new SendPrivateMessageRequestObject(message, id);
        String key = UUID.randomUUID().toString();
        object.setEcho(key);

        Object user = redisUtil.get(id + "_private");

        if (redisUtil.get(id + "_private") == null) {
            PrivateUserInfo userInfo = new PrivateUserInfo();
            userInfo.setUserId(id);
            redisUtil.put(id + "_private", JSONUtil.toJSONString(userInfo));
        }
//        PrivateUserInfo userInfo = JSONUtil.parseObject(redisUtil.get(id + "_private").toString(), PrivateUserInfo.class);
//        userInfo.setUserId(id);
//        redisUtil.put(id + "_private", userInfo);

        redisUtil.put(key, id + "_echo_private_message");

        BotWebSocketServer.sendMessage(JSONUtil.toJSONString(object), null);
        return true;
    }

    /**
     * 查询好友名单
     *
     * @return {@link ObjectNode}
     */
    @Override
    public ObjectNode queryFriendList() {
        return JSONUtil.toObjectNode(HttpUtil.sendHttpByString(HttpUtil.HttpRequestMethedEnum.HttpGet, botEndPoint + "/get_friend_list", null, null));
    }

    /**
     * 发送群消息
     *
     * @param message 消息
     * @param groupId 组id
     * @return boolean
     */
    @Override
    public boolean sendGroupMessage(String message, String groupId) {
        SendGroupMessageRequestObject object = new SendGroupMessageRequestObject(message, groupId);
        String key = UUID.randomUUID().toString();
        object.setEcho(key);

        if (redisUtil.get(groupId + "_group") == null) {
//            GroupInfo groupInfo = JSONUtil.parseObject(redisUtil.get(groupId + "_group").toString(), GroupInfo.class);
            GroupInfo groupInfo = new GroupInfo();
            groupInfo.setGroupId(groupId);
            redisUtil.put(groupId + "_group", JSONUtil.toJSONString(groupInfo));
        }

        redisUtil.put(key, groupId + "_echo_group_message");

        BotWebSocketServer.sendMessage(JSONUtil.toJSONString(object), null);
        return true;
    }

    /**
     * 自动根据消息类型判断发送群消息还是个人消息发送信息
     *
     * @param message 消息
     * @param object  对象
     * @return boolean
     */
    @Override
    public boolean sendMessageAuto(String message, ObjectNode object) {
        if ("group".equals(object.get("message_type").asText())) {
            String id = object.get("group_id").asText();
            this.sendGroupMessage(message, id);
        } else if ("private".equals(object.get("message_type").asText())) {
            String id = object.get("user_id").asText();
            this.sendPrivateMessage(message, id);
        }
        return false;
    }

    /**
     * 回滚群消息
     *
     * @param groupId 组id
     */
    @Override
    public void rollBackGroupMessage(String groupId) {
        GroupInfo groupInfo = JSONUtil.parseObject(redisUtil.get(groupId + "_group").toString(), GroupInfo.class);
        String lastMessageId = groupInfo.getLastMessageId();
        if(StringUtils.isNotEmpty(lastMessageId)){
            HttpUtil.sendHttpByString(HttpUtil.HttpRequestMethedEnum.HttpGet, botEndPoint + "/delete_msg?message_id=" + lastMessageId, null, null);
            //清空
            groupInfo.setLastMessageId("");
            redisUtil.put(groupId + "_group", JSONUtil.toJSONString(groupInfo));
        }
    }

    /**
     * 回滚私人消息
     *
     * @param id id
     */
    @Override
    public void rollBackPrivateMessage(String id) {
        Object user = redisUtil.get(id + "_private");
        PrivateUserInfo userInfo = JSONUtil.parseObject(redisUtil.get(id + "_private").toString(), PrivateUserInfo.class);
        String lastMessageId = userInfo.getLastMessageId();
        if(StringUtils.isNotEmpty(lastMessageId)){
            String result = HttpUtil.sendHttpByString(HttpUtil.HttpRequestMethedEnum.HttpGet, botEndPoint + "/delete_msg?message_id=" + lastMessageId, null, null);
            //清空
            userInfo.setLastMessageId("");
            redisUtil.put(id + "_private", JSONUtil.toJSONString(userInfo));
        }
    }

    /**
     * 更新最后一条消息信息
     *
     * @param echoId    回声id
     * @param messageId 消息id
     */
    @Override
    public void updateLastMessageInfo(String echoId, String messageId) {
        if (redisUtil.get(echoId) != null) {
            String info = redisUtil.get(echoId).toString();
            if (info.contains("_echo_group_message")) {
                String groupId = info.split("_")[0];
                GroupInfo groupInfo = JSONUtil.parseObject(redisUtil.get(groupId + "_group").toString(), GroupInfo.class);
                groupInfo.setGroupId(groupId);
                groupInfo.setLastMessageId(messageId);
                redisUtil.put(groupId + "_group", JSONUtil.toJSONString(groupInfo));
            }else if(info.contains("_echo_private_message")){
                String userId = info.split("_")[0];
                PrivateUserInfo userInfo = JSONUtil.parseObject(redisUtil.get(userId + "_private").toString(), PrivateUserInfo.class);
                userInfo.setUserId(userId);
                userInfo.setLastMessageId(messageId);
                redisUtil.put(userId + "_private", JSONUtil.toJSONString(userInfo));
            }
        }
    }


}
