package com.gccbenben.qqbotservice.controller;

import com.gccbenben.qqbotservice.bean.HttpCommonResponse;
import com.gccbenben.qqbotservice.bean.WebSocketRequestMessage;
import com.gccbenben.qqbotservice.bean.realWebSocketRequestMessage.GetFriendListRequestObject;
import com.gccbenben.qqbotservice.bean.realWebSocketRequestMessage.SendPrivateMessageRequestObject;
import com.gccbenben.qqbotservice.component.BotWebSocketServer;
import com.gccbenben.qqbotservice.service.BotBaseService;
import com.gccbenben.qqbotservice.service.PixivHandleService;
import com.gccbenben.qqbotservice.utils.HttpUtil;
import com.gccbenben.qqbotservice.utils.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/bot")
public class BaseBotController {

    @Autowired
    private BotBaseService botBaseService;

    @Autowired
    private PixivHandleService pixivHandleService;

    @PostMapping("/test")
    @ResponseBody
    public String testContronller() {
        return JSONUtil.toJSONString(pixivHandleService.test(null));
    }

    /**
     * 发送消息私人
     *
     * @param message 消息
     * @param id      id
     * @return {@link HttpCommonResponse}
     */
    @PostMapping("/sendPrivateMessage")
    @ResponseBody
    public HttpCommonResponse sendMessagePrivate(@RequestParam("message") String message, @RequestParam("id") String id) {
        botBaseService.sendPrivateMessage(message, id);
        return HttpCommonResponse.success("ok");
    }

    /**
     * 发送群消息
     *
     * @param message 消息
     * @param groupId groupId
     * @return {@link HttpCommonResponse}
     */
    @PostMapping("/sendGroupMessage")
    @ResponseBody
    public HttpCommonResponse sendGroupMessage(@RequestParam("message") String message, @RequestParam("groupId") String groupId) {
        botBaseService.sendGroupMessage(message, groupId);
        return HttpCommonResponse.success("ok");
    }

    /**
     * 得到朋友列表
     *
     * @return {@link HttpCommonResponse}
     */
    @GetMapping("/getFriendList")
    @ResponseBody
    public HttpCommonResponse getFriendList() {
        return HttpCommonResponse.success(botBaseService.queryFriendList());
    }

    /**
     * 通过websocket获取朋友列表
     *
     * @return {@link HttpCommonResponse}
     */
    @GetMapping("/getFriendListWebSocket")
    @ResponseBody
    public HttpCommonResponse getFriendListWebSocket() {
        GetFriendListRequestObject object = new GetFriendListRequestObject();
        BotWebSocketServer.sendMessage(JSONUtil.toJSONString(object), null);
        return HttpCommonResponse.success("ok");
    }
}
