package com.gccbenben.qqbotservice.component.Actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.Action;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.IMethodHandleStrategy;
import com.gccbenben.qqbotservice.utils.CommanderParameterParser;
import com.gccbenben.qqbotservice.utils.HttpUtil;
import com.gccbenben.qqbotservice.utils.ImageDownloadUtil;
import com.gccbenben.qqbotservice.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

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

//    private final static String setuPath = "/Users/gccbenben/qqbot/go-cqhttp_darwin_arm64/data/images/setu";
    private final static String setuPath = "/home/ubuntu/qqbot/data/images/setu";

    @Override
    public String handleMethod(ObjectNode message) {
        String[] options = message.get("message").asText().split(" ");
        String searchEngine = "";
        if (options[1].contains("-")) {
            searchEngine = options[1].split("-")[1].toLowerCase(Locale.ROOT);
        } else {
            searchEngine = "huashi";
        }
        if (options.length > 1) {
            String searchOptions = "";
            if (options[1].contains("-") && options.length > 2) {
                searchOptions = options[2];
            } else {
                searchOptions = options[1];
            }

            if ("huashi".equals(searchEngine)) {
                this.getHuaShiImage(searchOptions, message);
            } else if ("pixiv".equals(searchEngine)) {
                this.getPixivImage(searchOptions, message);
            } else {
                this.getHuaShiImage(searchOptions, message);
            }

        } else {
            super.botBaseService.sendMessageAuto("不可以瑟瑟", message);
        }



//        List options = CommanderParameterParser.getOptions(message.get("message").asText());


        return "";
    }

    /**
     * 从huashi网站获取随机图像图像
     *
     * @param searchOption 搜索选项
     * @param message      消息
     */
    private void getHuaShiImage(String searchOption, ObjectNode message) {
        Map search = new HashMap<>();
        search.put("word", searchOption);
        search.put("index", 1);
        Map header = new HashMap<>();
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
            ObjectNode responseJSON = JSONUtil.toObjectNode(response);

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
                }else{
                    count = responseJSON.get("illusts").size();
                }
            }
            Random random = new Random();
            int picIndex = random.nextInt(count);

            //获取pixiv图片信息
            JsonNode resourceInfo = responseJSON.get("illusts").get(picIndex);
            String resourceWebUrl = resourceInfo.get("image_urls").get("medium").asText();
            String pid = resourceInfo.get("id").asText();
            String title = resourceInfo.get("title").asText();
            String artistName = resourceInfo.get("user").get("name").asText();


            ArrayNode tags = (ArrayNode) responseJSON.get("illusts").get(picIndex).get("tags");
            Boolean r18Tag = false;
            for (JsonNode tag : tags) {
                if ("R-18".equals(tag.get("name"))) {
                    r18Tag = true;
                }
            }

            //如果是r-18则直接返回
            if (r18Tag) {
                String responseMessage = "图片包含r-18,地址：" + resourceWebUrl;
                log.info(responseMessage);
                super.botBaseService.sendMessageAuto(responseMessage, message);
                return;
            }

            Map header = new HashMap<>();
            header.put("Referer", "https://www.pixiv.net/");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
            Date date = new Date();
            String time = sdf.format(date);
            String savePath = setuPath + "/" + time;
            String fileName = ImageDownloadUtil.download(resourceWebUrl, savePath, header);

            String resourcePath = "/setu" + "/" + time + fileName;

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
