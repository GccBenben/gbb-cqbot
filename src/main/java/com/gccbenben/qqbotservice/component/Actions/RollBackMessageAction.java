package com.gccbenben.qqbotservice.component.Actions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.Action;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.IMethodHandleStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Action(name = "back_message")
@Component
@Slf4j
public class RollBackMessageAction extends BaseAction implements IMethodHandleStrategy {

    @Override
    public String handleMethod(ObjectNode message) {
        if("group".equals(message.get("message_type").asText())){
            //撤回群消息
            String groupId = message.get("group_id").asText();
            super.botBaseService.rollBackGroupMessage(groupId);
        }else{
            //撤回私人消息
            String id = message.get("user_id").asText();
            super.botBaseService.rollBackPrivateMessage(id);
        }
        return null;
    }
}
