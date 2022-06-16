package com.gccbenben.qqbotservice.component.Actions;


import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gccbenben.qqbotservice.bean.Pixiv.PixivPictureInfo;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.Action;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.IMethodHandleStrategy;
import com.gccbenben.qqbotservice.service.PixivHandleService;
import com.gccbenben.qqbotservice.utils.HttpUtil;
import com.gccbenben.qqbotservice.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.*;
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

    private static final String rankUrl = "https://www.pixiv.net/ranking.php?";

    protected static PixivHandleService pixivHandleService;

    @Autowired
    public void setPixivHandleService(PixivHandleService pixivHandleService) {
        this.pixivHandleService = pixivHandleService;
    }

    @Override
    public String handleMethod(ObjectNode message) {
        if ("group".equals(message.get("message_type").asText())) {
            getPixivRankImage(message);
        } else {
            super.botBaseService.sendMessageAuto("rank功能不支持个人私聊", message);
        }

        return null;
    }

    /**
     * 得到pixiv等级图像
     *
     * @param message 消息
     */
    private void getPixivRankImage(ObjectNode message) {
        String groupId = message.get("group_id").asText();

        //获取参数
        String[] options = message.get("message").asText().split(" ");
        List<String> optionInput = Arrays.stream(options).filter(item -> item.startsWith("-")).map(item -> item.substring(1).toLowerCase(Locale.ROOT)).collect(Collectors.toList());
        //搜索范围参数设置
        String mode = "";

        //搜索其余设置
        boolean random = false;

        if (!optionInput.isEmpty()) {
            for(String option : optionInput){
                if ("week".equals(option)) {
                    mode = "weekly";
                }

                if ("month".equals(option)) {
                    mode = "monthly";
                }

                if ("day".equals(option)) {
                    mode = "daily";
                }

                if ("ori".equals(option)) {
                    mode = "original";
                }

                if ("male".equals(option)) {
                    mode = "male";
                }

                if("random".equals(option)){
                    random = true;
                }
            }

        } else {
            mode = "weekly";
        }

        super.botBaseService.sendMessageAuto("开始获取排行图片，请稍等！", message);

        List<PixivPictureInfo> searchRankImages = getRankImageInfo(mode);

        if (searchRankImages == null || searchRankImages.isEmpty()) {
            super.botBaseService.sendMessageAuto("找不到瑟瑟！", message);
            return;
        }

        //如果是获取随机图片
        if(random){
            Random seed = new Random();
            int picIndex = seed.nextInt(searchRankImages.size());
            PixivPictureInfo pixivPictureInfo = searchRankImages.get(picIndex);

            String resourcePath = pixivHandleService.getPixivImageCash(String.valueOf(pixivPictureInfo.getPid()));
            if (null == resourcePath) {
                try {
                    resourcePath = pixivHandleService.pixivImageDownload(pixivPictureInfo.getMediumUrl());
                    pixivPictureInfo.setLocalAddress(resourcePath);
                    pixivHandleService.saveResourceInfo(pixivPictureInfo);
                } catch (Exception e) {
                    log.error("图片消息获取错误", e.getStackTrace());
                }
            }

            if (StringUtils.isNotEmpty(resourcePath)) {
                String responseMessage = "pid: " + pixivPictureInfo.getPid() + "\r\n";
                responseMessage += "title: " + pixivPictureInfo.getTitle() + "\r\n";
                responseMessage += "artist: " + pixivPictureInfo.getAuthor() + "\r\n";
                responseMessage += "[CQ:image,file=" + resourcePath + "]";
                super.botBaseService.sendMessageAuto(responseMessage, message);
            }

            return;
        }

        //如果获取全rank
        //只发送10条
        ArrayNode responseArray = JSONUtil.buildJSONArray();
        searchRankImages.stream().forEach(pixivPictureInfo -> {
            //缩小量
            if (responseArray.size() > 10) {
                return;
            }

            String resourcePath = pixivHandleService.getPixivImageCash(String.valueOf(pixivPictureInfo.getPid()));
            if (null == resourcePath) {
                try {
                    resourcePath = pixivHandleService.pixivImageDownload(pixivPictureInfo.getLargeUrl());
                    pixivPictureInfo.setLocalAddress(resourcePath);
                    pixivHandleService.saveResourceInfo(pixivPictureInfo);
                } catch (Exception e) {
                    log.error("图片消息获取错误", e.getStackTrace());
                }
            }

            if (StringUtils.isNotEmpty(resourcePath)) {
                String responseMessage = "[CQ:image,file=" + resourcePath + "]";

                ObjectNode baseNode = JSONUtil.buildJSONObject();
                baseNode.put("type", "node");
                ObjectNode dataNode = JSONUtil.buildJSONObject();
                dataNode.put("name", "bakabaka");
                dataNode.put("uin", "2253141704");
                dataNode.put("content", responseMessage);
                baseNode.set("data", dataNode);
                responseArray.add(baseNode);
            }
        });

        if (responseArray.isEmpty()) {
            super.botBaseService.sendMessageAuto("rank获取失败", message);
        } else {
            super.botBaseService.sendGroupMessageForward(responseArray, groupId);
        }


    }

    /**
     * 实用jsoup直接爬去pixiv排行，获取页面html并解析返回图片消息
     *
     * @param mode 模式
     * @return {@link List}<{@link PixivPictureInfo}>
     */
    public List<PixivPictureInfo> getRankImageInfo(String mode) {
        String url = rankUrl;
        if (StringUtils.isNotEmpty(mode)) {
            url += "mode=" + mode;
        }
        log.info("targetUrl: " + url);
        String responseContent = HttpUtil.sendHttp(HttpUtil.HttpRequestMethedEnum.HttpGet, url, null, null);

        Document responseHTML = Jsoup.parse(responseContent);

        List<PixivPictureInfo> rankImageInfos = new ArrayList<>();
        Elements rankPicturesHTML = responseHTML.getElementsByClass("ranking-items");
        if (rankPicturesHTML != null) {
            Elements rankContainer = rankPicturesHTML.get(0).getElementsByTag("section");
            if (rankContainer != null) {
                //获得rank图片，并进行处理
                for (Element pictureInfo : rankContainer) {
                    PixivPictureInfo pixivPictureInfo = new PixivPictureInfo();
                    pixivPictureInfo.setPid(Integer.parseInt(pictureInfo.attr("data-id")));
                    pixivPictureInfo.setTitle(pictureInfo.attr("data-title"));
                    pixivPictureInfo.setAuthor(pictureInfo.attr("data-user-name"));
                    Elements pictureItemHTMl = pictureInfo.getElementsByClass("ranking-image-item");
                    if (pictureItemHTMl != null) {
                        Elements imageSourceHTML = pictureItemHTMl.get(0).getElementsByTag("img");
                        String imageURL = imageSourceHTML.get(0).attr("data-src");
                        pixivPictureInfo.setMediumUrl(imageURL);

                        String largeImageUrl = imageURL.replace("540x540_70", "600x1200_90");
                        pixivPictureInfo.setLargeUrl(largeImageUrl);
                    }

                    rankImageInfos.add(pixivPictureInfo);
                }
            }
        }

        return rankImageInfos;
    }

    public static void main(String[] args) {
        PixivRankAction test = new PixivRankAction();
        test.getRankImageInfo("day");
    }
}
