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
import java.net.URLEncoder;
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

    private static final String rankUrl = "https://www.pixiv.net/ranking.php?";

    private static final String tagUrl = "https://www.pixiv.net/ajax/search/illustrations/";

    protected static PixivHandleService pixivHandleService;

    /**
     * 注入pixiv处理服务
     *
     * @param pixivHandleService pixiv处理服务
     */
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
        boolean tag = false;

        if (!optionInput.isEmpty()) {
            for (String option : optionInput) {
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

                if ("random".equals(option) || "rd".equals(option)) {
                    random = true;
                }

                if ("tag".equals(option) || "t".equals(option)) {
                    tag = true;
                }
            }

        } else {
            mode = "weekly";
        }

        super.botBaseService.sendMessageAuto("开始获取排行图片，请稍等！", message);

        if (tag) {
            try {
                getTagPopularImages(options, message, groupId);
            } catch (UnsupportedEncodingException e) {
                log.error("tag图片获取失败" + e.getStackTrace());
                super.botBaseService.sendMessageAuto("tag图片获取失败", message);
            }
            return;
        }

        List<PixivPictureInfo> searchRankImages = getRankImageInfo(mode);

        if (searchRankImages == null || searchRankImages.isEmpty()) {
            super.botBaseService.sendMessageAuto("找不到瑟瑟！", message);
            return;
        }

        //如果是获取随机图片
        if (random) {
            getRandomPixivRankPicture(searchRankImages, message);
            return;
        }


        //如果获取全rank
        //只发送10条
        ArrayNode responseArray = JSONUtil.buildJSONArray();
        searchRankImages.forEach(pixivPictureInfo -> {
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
//                String responseMessage = "[CQ:image,file=" + resourcePath + "]";
                String responseMessage = "pid: " + pixivPictureInfo.getPid() + "\r\n";
                responseMessage += "title: " + pixivPictureInfo.getTitle() + "\r\n";
                responseMessage += "artist: " + pixivPictureInfo.getAuthor() + "\r\n";
                responseMessage += "[CQ:image,file=" + resourcePath + "]";

                buildForwadMessage(responseArray, responseMessage);
            }
        });

        if (responseArray.isEmpty()) {
            super.botBaseService.sendMessageAuto("rank获取失败", message);
        } else {
            super.botBaseService.sendGroupMessageForward(responseArray, groupId);
        }


    }

    /**
     * 根据tag获取流行图片
     *
     * @param options 选项
     * @param message 消息
     * @param groupId 组id
     * @throws UnsupportedEncodingException 不支持编码异常
     */
    private void getTagPopularImages(String[] options, ObjectNode message, String groupId) throws UnsupportedEncodingException {
        String targetUrl = tagUrl;
        //拼接tag搜索选项
        StringBuilder searchOptions = new StringBuilder();
        List<String> searchInput = Arrays.stream(options).filter(item -> !item.startsWith("/") && !item.startsWith("-")).collect(Collectors.toList());
        if (!searchInput.isEmpty()) {
            for (String item : searchInput) {
                searchOptions.append(item).append("");
            }
        } else {
            super.botBaseService.sendMessageAuto("未输入瑟瑟关键字", message);
            return;
        }

        String searchWord = URLEncoder.encode(searchOptions.toString(), "UTF-8");
        targetUrl += searchWord;
        targetUrl += "?word=" + searchWord;
        targetUrl += "&order=date_d&mode=all&p=1&s_mode=s_tag_full&type=illust_and_ugoira&lang=zh";
        String tagSearchResponse = HttpUtil.sendHttp(HttpUtil.HttpRequestMethedEnum.HttpGet, targetUrl, null, null);

        if (null == tagSearchResponse) {
            super.botBaseService.sendMessageAuto("不准瑟瑟！", message);
            return;
        }
        ObjectNode responseJSON = JSONUtil.toObjectNode(tagSearchResponse);
        if (responseJSON.has("error") && "false".equals(responseJSON.get("error").asText())) {
            ArrayNode pictureNodes = null;

            if (Arrays.stream(options).anyMatch(option -> option.equals("-r") || option.equals("-recent"))) {
                //最近流行的
                pictureNodes = (ArrayNode) responseJSON.get("body").get("popular").get("recent");
            } else if (Arrays.stream(options).anyMatch(option -> option.equals("-p") || option.equals("-permanent"))) {
                //长期流行的
                pictureNodes = (ArrayNode) responseJSON.get("body").get("popular").get("permanent");
            }else{
                //默认获取最近的
                pictureNodes = (ArrayNode) responseJSON.get("body").get("popular").get("recent");
            }

            assert pictureNodes != null;
            if(pictureNodes.isEmpty()){
                //如果瑟瑟不存在则直接搜索随机一张相关tag的图

                super.botBaseService.sendMessageAuto("瑟瑟不存在！", message);
                return;
            }

            //发送随机一张
            if (Arrays.stream(options).anyMatch(option -> option.equals("-rd") || option.equals("-random"))) {
                Random random = new Random();
                int picIndex = random.nextInt(pictureNodes.size());
                ObjectNode pictureNode = (ObjectNode) pictureNodes.get(picIndex);
                PixivPictureInfo pixivPictureInfo = new PixivPictureInfo();
                pixivPictureInfo.setPid(Integer.parseInt(pictureNode.get("id").asText()));
                pixivPictureInfo.setTitle(pictureNode.get("title").asText());
                pixivPictureInfo.setAuthor(pictureNode.get("userName").asText());
                String mediumUrl = pictureNode.get("url").asText();
                String largeImageUrl = mediumUrl.replace("/c/250x250_80_a2","");
                pixivPictureInfo.setMediumUrl(mediumUrl);
                pixivPictureInfo.setLargeUrl(largeImageUrl);

                this.getSingleImageAndSend(message, pixivPictureInfo);
            } else {
                //发送全量
                ArrayNode responseArray = JSONUtil.buildJSONArray();
                pictureNodes.forEach(pictureNode -> {
                    PixivPictureInfo pixivPictureInfo = new PixivPictureInfo();
                    pixivPictureInfo.setPid(Integer.parseInt(pictureNode.get("id").asText()));
                    pixivPictureInfo.setTitle(pictureNode.get("title").asText());
                    pixivPictureInfo.setAuthor(pictureNode.get("userName").asText());
                    String mediumUrl = pictureNode.get("url").asText();
                    String largeImageUrl = mediumUrl.replace("/c/250x250_80_a2","");
                    pixivPictureInfo.setMediumUrl(mediumUrl);
                    pixivPictureInfo.setLargeUrl(largeImageUrl);

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
                        String responseMessage = "pid: " + pixivPictureInfo.getPid() + "\r\n";
                        responseMessage += "title: " + pixivPictureInfo.getTitle() + "\r\n";
                        responseMessage += "artist: " + pixivPictureInfo.getAuthor() + "\r\n";
                        responseMessage += "[CQ:image,file=" + resourcePath + "]";

                        buildForwadMessage(responseArray, responseMessage);
                    }
                });

                if (responseArray.isEmpty()) {
                    super.botBaseService.sendMessageAuto("rank获取失败", message);
                } else {
                    super.botBaseService.sendGroupMessageForward(responseArray, groupId);
                }
            }

        }


    }

    /**
     * 构造转发消息体
     *
     * @param responseArray   响应数组
     * @param responseMessage 响应消息
     */
    private void buildForwadMessage(ArrayNode responseArray, String responseMessage) {
        ObjectNode baseNode = JSONUtil.buildJSONObject();
        baseNode.put("type", "node");
        ObjectNode dataNode = JSONUtil.buildJSONObject();
        dataNode.put("name", "bakabaka");
        dataNode.put("uin", "2253141704");
        dataNode.put("content", responseMessage);
        baseNode.set("data", dataNode);
        responseArray.add(baseNode);
    }

    /**
     * 得到随机pixiv rank图，并发送
     *
     * @param searchRankImages 搜索排名图片
     * @param message          消息
     */
    private void getRandomPixivRankPicture(List<PixivPictureInfo> searchRankImages, ObjectNode message) {
        Random seed = new Random();
        int picIndex = seed.nextInt(searchRankImages.size());
        PixivPictureInfo pixivPictureInfo = searchRankImages.get(picIndex);

        this.getSingleImageAndSend(message, pixivPictureInfo);
    }

    /**
     * 获得一张图片和发送
     *
     * @param message          消息
     * @param pixivPictureInfo pixiv图片信息
     */
    private void getSingleImageAndSend(ObjectNode message, PixivPictureInfo pixivPictureInfo) {
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
            String responseMessage = "pid: " + pixivPictureInfo.getPid() + "\r\n";
            responseMessage += "title: " + pixivPictureInfo.getTitle() + "\r\n";
            responseMessage += "artist: " + pixivPictureInfo.getAuthor() + "\r\n";
            responseMessage += "[CQ:image,file=" + resourcePath + "]";
            super.botBaseService.sendMessageAuto(responseMessage, message);
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

//                        String largeImageUrl = imageURL.replace("540x540_70", "600x1200_90");
                        String largeImageUrl = imageURL.replace("/c/240x480", "");
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
