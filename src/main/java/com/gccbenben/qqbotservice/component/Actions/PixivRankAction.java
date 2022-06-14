package com.gccbenben.qqbotservice.component.Actions;


import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.Action;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.IMethodHandleStrategy;
import com.gccbenben.qqbotservice.service.PixivHandleService;
import com.gccbenben.qqbotservice.utils.HttpUtil;
import com.gccbenben.qqbotservice.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * pixiv排行查询
 * 支持的查询关键字有：
 * 'day', 'week', 'month', 'day_male', 'day_female', 'week_original', 'week_rookie',
 * 'day_r18', 'day_male_r18', 'day_female_r18', 'week_r18', 'week_r18g
 *
 * @author GccBenben
 * @date 2022/05/31
 */
@Action(name = "pixiv_rank")
@Component
@Slf4j
public class PixivRankAction extends BaseAction implements IMethodHandleStrategy {

    private static final String apiUrl = "https://api.obfs.dev/api/pixiv/rank";

    protected static PixivHandleService pixivHandleService;

    @Autowired
    public void setPixivHandleService(PixivHandleService pixivHandleService) {
        this.pixivHandleService = pixivHandleService;
    }

    @Override
    public String handleMethod(ObjectNode message) {
        if ("group".equals(message.get("message_type").asText())) {
            getPixivRankImage(message);
        }else{
            super.botBaseService.sendMessageAuto("rank功能不支持个人私聊", message);
        }

        return null;
    }

    private void getPixivRankImage(ObjectNode message){
        String groupId = message.get("group_id").asText();

        //获取参数
        String[] options = message.get("message").asText().split(" ");
        List<String> optionInput = Arrays.stream(options).filter(item -> item.startsWith("-")).map(item -> item.substring(1).toLowerCase(Locale.ROOT)).collect(Collectors.toList());
        String mode = "";
        if(!optionInput.isEmpty()){
            if("week".equals(optionInput.get(0))){
                mode = "week";
            }

            if("month".equals(optionInput.get(0))){
                mode = "month";
            }

            if("day".equals(optionInput.get(0))){
                mode = "day";
            }

            if("ori".equals(optionInput.get(0))){
                mode = "week_original";
            }
        }else{
            mode = "week";
        }
        String targetUrl = apiUrl + "?mode=" + mode + "&size=15";
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

        super.botBaseService.sendMessageAuto("开始获取排行图片，请稍等！", message);

        //只发送10条
        ArrayNode responseArray = JSONUtil.buildJSONArray();
        responseJSON.get("illusts").forEach(pictureInfo ->{
            //缩小量
            if(responseArray.size() > 10){
                return;
            }
            String largeImageUrl = pictureInfo.get("image_urls").get("large").asText();
            String mediumImageUrl = pictureInfo.get("image_urls").get("medium").asText();
            String pid = pictureInfo.get("id").asText();
            String title = pictureInfo.get("title").asText();
            String artistName = pictureInfo.get("user").get("name").asText();


            //暂时先不管r18的情况
            String resourcePath = pixivHandleService.getPixivImageCash(pid);
            if (null == resourcePath) {
                try {
                    resourcePath = pixivHandleService.pixivImageDownload(largeImageUrl);
                    pixivHandleService.saveResourceInfo(pid, artistName, title, resourcePath, mediumImageUrl, largeImageUrl);
                } catch (Exception e) {
                    log.error("图片消息获取错误", e.getStackTrace());
                }
            }

            if(StringUtils.isNotEmpty(resourcePath)){
                String responseMessage = "[CQ:image,file=" + resourcePath + "]";

                ObjectNode baseNode = JSONUtil.buildJSONObject();
                baseNode.put("type", "node");
                ObjectNode dataNode = JSONUtil.buildJSONObject();
                dataNode.put("name", "bakabaka");
                dataNode.put("uin", "2253141704");
                dataNode.put("content",responseMessage);
                baseNode.set("data", dataNode);
                responseArray.add(baseNode);
            }
        });

        if(responseArray.isEmpty()){
            super.botBaseService.sendMessageAuto("rank获取失败", message);
        }else{
            super.botBaseService.sendGroupMessageForward(responseArray, groupId);
        }


    }
}
