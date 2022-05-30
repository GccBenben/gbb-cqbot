package com.gccbenben.qqbotservice.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gccbenben.qqbotservice.bean.Actions;
import com.gccbenben.qqbotservice.component.Actions.BaseAction;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.MethodStrategyContext;
import com.gccbenben.qqbotservice.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import org.apache.commons.lang.StringUtils;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 机器人消息处理程序
 *
 * @author GccBenben
 * @date 2022/05/25
 */
@Component
@Slf4j
public class BotMessageHandler {

    private static ArrayNode actionList;

    @Value("classpath:actions.json")
    private Resource userResource;

    /**
     * 得到行为
     *
     * @return {@link ArrayNode}
     */
    @PostConstruct
    public ArrayNode getActions() {
        try {
            String json = IOUtils.toString(userResource.getInputStream(), Charset.forName("UTF-8"));
            actionList = JSONUtil.parseObject(json, ArrayNode.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return actionList;
    }

    /**
     * 处理消息
     *
     * @param message 消息
     */
    public void handleMessage(ObjectNode message) {

        String method = "";
        String text = message.get("message").asText();

        //获取到命令
        String command = text.toLowerCase(Locale.ROOT).split(" ")[0];

        for(JsonNode node : actionList){
            if(command.equals(node.get("rule").asText())){
                method = node.get("action").asText();
            }
        }

        //进行分发到指定action
        if (StringUtils.isNotBlank(method)) {
            // 策略上下文
            MethodStrategyContext methodStrategyContext = new MethodStrategyContext(method.toLowerCase());
            // 执行策略
            methodStrategyContext.handleMethod(message, method);
        }else{
            log.info("不需要处理");
        }
    }

    /**
     * 回声消息处理
     * 因为暂时只会往websocket里面发送消息，所以目前默认都是更新groupinfo或者是privateinfo
     *
     * @param object 对象
     */
    public void echo(ObjectNode object) {
        BaseAction baseAction = new BaseAction();
        String echo = object.get("echo").asText();
        String messageId = object.get("data").get("message_id").asText();
        baseAction.echoMessageHandle(echo, messageId);
    }
}
