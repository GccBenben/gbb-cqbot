package com.gccbenben.qqbotservice.bean.realWebSocketRequestMessage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gccbenben.qqbotservice.bean.WebSocketRequestMessage;
import com.gccbenben.qqbotservice.utils.JSONUtil;
import org.apache.ibatis.annotations.Param;

public class SendPrivateMessageRequestObject extends WebSocketRequestMessage {

    public SendPrivateMessageRequestObject(String message, String userId, boolean isText){
        this.action = "send_private_msg";
        this.params = JSONUtil.buildJSONObject();
        params.put("user_id", userId);
        params.put("message", message);
        params.put("auto_escape", isText);
    }

    public SendPrivateMessageRequestObject(String message, String userId){
        this.action = "send_private_msg";
        this.params = JSONUtil.buildJSONObject();
        params.put("user_id", userId);
        params.put("message", message);
        params.put("auto_escape", false);
    }
}
