package com.gccbenben.qqbotservice.component.Actions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.Action;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.IMethodHandleStrategy;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 骰子
 *
 * @author GccBenben
 * @date 2022/05/25
 */
@Component
@Action(name = "dice")
public class DiceAction extends BaseAction implements IMethodHandleStrategy {
    @Override
    public String handleMethod(ObjectNode message) {
        String text = message.get("message").asText();
        String[] words = text.split("\\s+");

        String response = "格式错误";
        Random random = new Random();
        if (words.length == 1) {
            response = String.valueOf(random.nextInt(100));
        } else if (StringUtils.isNumeric(words[1]) && words.length == 2) {
            //如果是 /roll 100这种模式
            response = String.valueOf(random.nextInt(Integer.parseInt(words[1])));
        } else if (words[1].contains("-")) {
            //如果是 /roll 1-100这种模式
            String[] rollRange = words[1].split("-");
            if (StringUtils.isNumeric(rollRange[0]) && StringUtils.isNumeric(rollRange[1]) && Integer.parseInt(rollRange[1]) > Integer.parseInt(rollRange[0])) {
                response = String.valueOf(random.nextInt(Integer.parseInt(rollRange[1]) - Integer.parseInt(rollRange[0])) + Integer.parseInt(rollRange[0]));
            }
        }

        super.botBaseService.sendMessageAuto(response, message);
        return "";
    }
}
