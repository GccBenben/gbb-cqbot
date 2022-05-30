package com.gccbenben.qqbotservice.component.Actions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gccbenben.qqbotservice.service.BotBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 基本action
 *
 * @author GccBenben
 * @date 2022/05/25
 */
@Component
public class BaseAction {


    protected static BotBaseService botBaseService;

    /**
     * 注入service
     *
     * @param botBaseService 机器人基础服务
     */
    @Autowired
    public void setBotBaseService(BotBaseService botBaseService) {
        BaseAction.botBaseService = botBaseService;
    }

    public void echoMessageHandle(String echoId, String messageId){
        botBaseService.updateLastMessageInfo(echoId, messageId);
    }
}
