package com.gccbenben.qqbotservice.component.Actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.Action;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.IMethodHandleStrategy;
import com.gccbenben.qqbotservice.utils.JSONUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;

/**
 * 返回可用帮助消息
 *
 * @author GccBenben
 * @date 2022/06/17
 */
@Component
@Action(name = "help")
public class HelpAction extends BaseAction implements IMethodHandleStrategy {

    @Value("classpath:actions.json")
    private Resource userResource;

    private static ArrayNode actionList;

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

    @Override
    public String handleMethod(ObjectNode message) {
        StringBuilder messageBuilde = new StringBuilder();
        for(JsonNode action : actionList){
            if(action.has("desc")){
                messageBuilde.append(action.get("rule").asText() + ": ");
                messageBuilde.append(action.get("desc").asText() + "\r\n");
            }
        }
        super.botBaseService.sendMessageAuto(messageBuilde.toString(), message);
        return null;
    }
}
