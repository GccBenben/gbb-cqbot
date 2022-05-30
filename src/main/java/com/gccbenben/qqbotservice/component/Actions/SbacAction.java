package com.gccbenben.qqbotservice.component.Actions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.Action;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.IMethodHandleStrategy;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 骰子
 *
 * @author GccBenben
 * @date 2022/05/25
 */
@Component
@Action(name = "sbac")
public class SbacAction extends BaseAction implements IMethodHandleStrategy {
    @Override
    public String handleMethod(ObjectNode message) {
        String id = message.get("user_id").asText();
        if("491330842".equals(id)){
            super.botBaseService.sendMessageAuto("[CQ:at" + id + "] sbac", message);
        }
        return "";
    }
}
