package com.gccbenben.qqbotservice.bean.realWebSocketRequestMessage;

import com.gccbenben.qqbotservice.bean.WebSocketRequestMessage;
import com.gccbenben.qqbotservice.utils.JSONUtil;

public class SendGroupMessageRequestObject extends WebSocketRequestMessage {
    public SendGroupMessageRequestObject(String message, String groupId){
        this.action = "send_group_msg";
        this.params = JSONUtil.buildJSONObject();
        this.params.put("group_id", groupId);
        this.params.put("message", message);
    }
}
