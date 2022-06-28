package com.gccbenben.qqbotservice.component.Actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.Action;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.IMethodHandleStrategy;
import com.gccbenben.qqbotservice.service.PixivHandleService;
import com.gccbenben.qqbotservice.utils.HttpUtil;
import com.gccbenben.qqbotservice.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 处理瑟瑟
 *
 * @author GccBenben
 * @date 2022/05/25
 */
@Action(name = "setu")
@Component
@Slf4j
public class SeTuAction extends BaseAction implements IMethodHandleStrategy {

    private final static String huashiUrl = "https://rt.huashi6.com/search/all";

    private final static String pixivEngineUrl = "https://api.obfs.dev/api/pixiv/search";

    private final static String huashiResourceUrl = "https://img2.huashi6.com/";

    private String searchEngine;

    private boolean r18Switch = true;

    protected static PixivHandleService pixivHandleService;

    @Autowired
    public void setPixivHandleService(PixivHandleService pixivHandleService) {
        this.pixivHandleService = pixivHandleService;
    }

    /**
     * 处理方法
     *
     * @param message 消息
     * @return {@link String}
     */
    @Override
    public String handleMethod(ObjectNode message) {
        String[] options = message.get("message").asText().split(" ");

        //判断搜索引擎以及其余的搜索条件
//        if (options[1].contains("-")) {
//            searchEngine = options[1].split("-")[1].toLowerCase(Locale.ROOT);
//        } else {
//            searchEngine = "pixiv";
//        }
        List<String> optionInput = Arrays.stream(options).filter(item -> item.startsWith("-")).collect(Collectors.toList());
        if (optionInput.isEmpty()) {
            searchEngine = "pixiv";
        } else {
            for (String option : optionInput) {
                setOption(option);
            }
        }

        //拼接搜索条件
        if (options.length > 1) {
            StringBuilder searchOptions = new StringBuilder();
            List<String> searchInput = Arrays.stream(options).filter(item -> !item.startsWith("/") && !item.startsWith("-")).collect(Collectors.toList());
            if (!searchInput.isEmpty()) {
                for (String item : searchInput) {
                    searchOptions.append(item).append(" ");
                }
            } else {
                super.botBaseService.sendMessageAuto("未输入瑟瑟关键字", message);
                return "";
            }

            if(r18Switch){
                searchOptions.append(" r18");
            }
//            if (options[1].contains("-") && options.length > 2) {
//                searchOptions = options[2];
//            } else {
//                searchOptions = options[1];
//            }

            if ("huashi".equals(searchEngine)) {
                this.getHuaShiImage(searchOptions.toString(), message);
            } else if ("pixiv".equals(searchEngine)) {
                this.getPixivImage(searchOptions.toString(), message);
            } else {
//                this.getHuaShiImage(searchOptions, message);
                this.getPixivImage(searchOptions.toString(), message);
            }

        } else {
            super.botBaseService.sendMessageAuto("不可以瑟瑟", message);
        }


//        List<String> options = CommanderParameterParser.getOptions(message.get("message").asText());
//        options.stream().forEach(option ->{
//            try{
//                Method method = this.getClass().getDeclaredMethod(option, String.class);
//                if(null != method){
//                    method.invoke(this, option);
//                }else{
//                    log.error("需要执行的参数不存在， option: " + option);
//                }
//            }catch (Exception e){
//                log.error("执行失败, error: ", e);
//            }
//        });


        return "";
    }

    /**
     * 设置选项
     *
     * @param option 选项
     */
    private void setOption(String option) {
        String config = option.split("-")[1].toLowerCase(Locale.ROOT);
        if ("huashi".equals(config)) {
            searchEngine = "huashi";
        }

        if ("pixiv".equals(config)) {
            searchEngine = "pixiv";
        }

        if ("r18".equals(config)) {
            r18Switch = false;
        }

        if ("r-18".equals(config)) {
            r18Switch = false;
        }
    }

    /**
     * 从huashi网站获取随机图像图像
     *
     * @param searchOption 搜索选项
     * @param message      消息
     */
    private void getHuaShiImage(String searchOption, ObjectNode message) {
        Map<String, Object> search = new HashMap<>();
        search.put("word", searchOption);
        search.put("index", 1);
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        try {
            String response = HttpUtil.sendHttp(HttpUtil.HttpRequestMethedEnum.HttpPost, huashiUrl, search, header);
            ObjectNode responseJSON = JSONUtil.toObjectNode(response);
            int count = responseJSON.get("data").get("works").size();
            Random random = new Random();
            int picIndex = random.nextInt(count);
            String resource = huashiResourceUrl + responseJSON.get("data").get("works").get(picIndex).get("coverImage").get("originalPath").asText();
            String responseMessage = "[CQ:image,file=" + resource + "]";
            super.botBaseService.sendMessageAuto(responseMessage, message);
        } catch (Exception e) {
            log.error("图片消息获取错误", e.getStackTrace());
            super.botBaseService.sendMessageAuto("图片消息获取错误", message);
        }
    }

    /**
     * 从pixiv获取随机图像
     *
     * @param searchOption 搜索选项
     * @param message      消息
     */
    private void getPixivImage(String searchOption, ObjectNode message) {
        try {
            String targetUrl = searchOption + " 1000users入り";
            targetUrl = pixivEngineUrl + "?word=" + URLEncoder.encode(targetUrl, "UTF-8");
            String response = HttpUtil.sendHttp(HttpUtil.HttpRequestMethedEnum.HttpGet, targetUrl, null, null);
            //如果接口调用没有返回值
            if (null == response) {
                super.botBaseService.sendMessageAuto("不准瑟瑟！", message);
                return;
            }
            ObjectNode responseJSON = JSONUtil.toObjectNode(response);

            //如果没有搜索到图片
            if (responseJSON.has("error")) {
                super.botBaseService.sendMessageAuto("找不到瑟瑟！", message);
                return;
            }

            //获取随机图片index
            int count = responseJSON.get("illusts").size();
            if (count == 0) {
                //进行降级，直接搜索全量
                targetUrl = searchOption;
                targetUrl = pixivEngineUrl + "?word=" + URLEncoder.encode(targetUrl, "UTF-8");
                response = HttpUtil.sendHttp(HttpUtil.HttpRequestMethedEnum.HttpGet, targetUrl, null, null);
                responseJSON = JSONUtil.toObjectNode(response);

                if (responseJSON.get("illusts").size() == 0) {
                    super.botBaseService.sendMessageAuto("找不到瑟图！", message);
                    return;
                } else {
                    count = responseJSON.get("illusts").size();
                }
            }
            Random random = new Random();
            int picIndex = random.nextInt(count);

            //获取pixiv图片信息
            JsonNode resourceInfo = responseJSON.get("illusts").get(picIndex);
            String resourceWebUrl = resourceInfo.get("image_urls").get("medium").asText();
            String resourceWebUrlLarge = resourceInfo.get("image_urls").get("large").asText();
            String pid = resourceInfo.get("id").asText();
            String title = resourceInfo.get("title").asText();
            String artistName = resourceInfo.get("user").get("name").asText();

            //图片查询是否存在数据库缓存，如果存在则直接发送，否则进行下载
            String resourcePath = pixivHandleService.getPixivImageCash(pid);
            if (null == resourcePath) {
                try {
                    resourcePath = pixivHandleService.pixivImageDownload(resourceWebUrlLarge);
                    pixivHandleService.saveResourceInfo(pid, artistName, title, resourcePath, resourceWebUrl, resourceWebUrlLarge);
                } catch (Exception e) {
                    log.error("图片消息获取错误", e.getStackTrace());
                    super.botBaseService.sendMessageAuto("图片消息获取错误,无法连接到pixiv服务器", message);
                    return;
                }
            }

            ArrayNode tags = (ArrayNode) responseJSON.get("illusts").get(picIndex).get("tags");
            boolean r18Tag = false;
            for (JsonNode tag : tags) {
                if ("R-18".equals(tag.get("name").asText())) {
                    r18Tag = true;
                } else if ("R-18G".equals(tag.get("name").asText())) {
                    r18Tag = true;
                }
            }

            //如果是r-18则看参数重是否允许放过r18，否则直接返回
            if (r18Tag && r18Switch) {
                String r18WebUrl = "http://ec2-18-237-230-219.us-west-2.compute.amazonaws.com:8999/pixiv" + resourcePath;
//                String responseMessage = "图片包含r-18,地址：" + resourceWebUrl;
                String responseMessage = "图片包含r-18,地址：" + r18WebUrl;
                log.info(responseMessage);
                super.botBaseService.sendMessageAuto(responseMessage, message);
                return;
            }

            //返回消息封装
            String responseMessage = "pid: " + pid + "\r\n";
            responseMessage += "title: " + title + "\r\n";
            responseMessage += "artist: " + artistName + "\r\n";
            responseMessage += "[CQ:image,file=" + resourcePath + "]";
            super.botBaseService.sendMessageAuto(responseMessage, message);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("图片消息获取错误", e.getStackTrace());
            super.botBaseService.sendMessageAuto("图片消息获取错误", message);
        }

    }
}
