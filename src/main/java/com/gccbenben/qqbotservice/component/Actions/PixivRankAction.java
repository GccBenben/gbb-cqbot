package com.gccbenben.qqbotservice.component.Actions;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.Action;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.IMethodHandleStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author GccBenben
 * @date 2022/05/31
 */
@Action(name = "pixivRank")
@Component
@Slf4j
public class PixivRankAction extends BaseAction implements IMethodHandleStrategy {
    @Override
    public String handleMethod(ObjectNode message) {
        return null;
    }
}
