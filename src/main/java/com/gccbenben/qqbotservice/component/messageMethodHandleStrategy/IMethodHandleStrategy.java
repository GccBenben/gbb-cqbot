package com.gccbenben.qqbotservice.component.messageMethodHandleStrategy;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface IMethodHandleStrategy {
    String handleMethod(ObjectNode message);
}
