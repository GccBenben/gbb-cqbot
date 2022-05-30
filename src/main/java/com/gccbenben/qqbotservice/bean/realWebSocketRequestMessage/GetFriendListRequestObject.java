package com.gccbenben.qqbotservice.bean.realWebSocketRequestMessage;

import com.gccbenben.qqbotservice.bean.WebSocketRequestMessage;

public class GetFriendListRequestObject extends WebSocketRequestMessage {
    public GetFriendListRequestObject(){
        this.action = "get_friend_list";
    }
}
