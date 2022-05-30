package com.gccbenben.qqbotservice.utils;

import io.netty.util.internal.ObjectUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 命令参数解析器
 * 可以用来解析-xxx命令
 *
 * @author GccBenben
 * @date 2022/05/30
 */
public class CommanderParameterParser {

    /**
     * 参数解析
     *
     * @param commander 待解析命令
     * @return {@link List}
     */
    public static List getOptions(String commander) {
        String[] parameters = commander.toLowerCase(Locale.ROOT).split(" ");
        List options = Arrays.asList(parameters)
                .stream()
                .filter(item -> item.startsWith("-"))
                .peek(item -> item.replaceAll("-", ""))
                .collect(Collectors.toList());
        if(options.isEmpty()){
            return new ArrayList();
        }else{
            return options;
        }
    }
}
