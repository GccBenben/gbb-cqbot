package com.gccbenben.qqbotservice.bean;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * websocket请求消息
 *
 * @author GccBenben
 * @date 2022/05/24
 */
@Data
public class WebSocketRequestMessage {

    protected String action;

    protected ObjectNode params;

    protected String echo;

}
