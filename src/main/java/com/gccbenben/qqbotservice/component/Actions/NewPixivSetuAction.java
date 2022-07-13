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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author GccBenben
 * @date 2022/07/13
 */
@Action(name = "new_setu_action")
@Component
@Slf4j
public class NewPixivSetuAction extends BaseAction implements IMethodHandleStrategy {

    private static final String tagUrl = "https://www.pixiv.net/ajax/search/illustrations/";

    protected static PixivHandleService pixivHandleService;

    private static final int pixivPictureTotalMin = 10;

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
        String[] options = message.get("message").asText().split(" ");
        List<String> optionInput = Arrays.stream(options).filter(item -> item.startsWith("-")).map(item -> item.substring(1).toLowerCase(Locale.ROOT)).collect(Collectors.toList());

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

            try {
                this.getPixivImage(searchOptions.toString(), message, optionInput);
            } catch (UnsupportedEncodingException e) {
                log.error("图片消息获取错误", e.getStackTrace());
                super.botBaseService.sendMessageAuto("图片消息获取错误", message);
            }


        } else {
            super.botBaseService.sendMessageAuto("不可以瑟瑟", message);
        }

        return "";
    }

    /**
     * 得到pixiv图像
     *
     * @param message       消息
     * @param searchOptions 搜索选项
     * @throws UnsupportedEncodingException 不支持编码异常
     */
    private void getPixivImage(String searchOptions, ObjectNode message, List<String> optionInput) throws UnsupportedEncodingException {
        String targetUrl = tagUrl;
        //默认搜索1000以上的，如果没有则就行降级
        String searchOption = searchOptions + "1000users入り";

        String searchWord = URLEncoder.encode(searchOption, "UTF-8");
        searchWord = searchWord.replace("+", "%20");

        targetUrl += searchWord;
        targetUrl += "?word=" + searchWord;

        String otherOptionParam = "";
        String r18mode = "";
        if (!optionInput.isEmpty()) {
            for (String option : optionInput) {
                if ("r18".equals(option) || "r".equals(option)) {
                    r18mode = "r18";
                }

                if ("all".equals(option) || "a".equals(option)) {
                    r18mode = "all";
                }
            }
        }

        if (StringUtils.isNotEmpty(r18mode)) {
            if ("all".equals(r18mode)) {
                otherOptionParam += "&order=date_d&mode=all&p=2&s_mode=s_tag&type=illust_and_ugoira&lang=zh";
            } else if ("r18".equals(r18mode)) {
                otherOptionParam += "&order=date_d&mode=r18&p=1&s_mode=s_tag&type=illust_and_ugoira&lang=zh";
            } else {
                otherOptionParam += "&order=date_d&mode=safe&p=1&s_mode=s_tag&type=illust_and_ugoira&lang=zh";
            }
        } else {
            otherOptionParam += "&order=date_d&mode=safe&p=1&s_mode=s_tag&type=illust_and_ugoira&lang=zh";
        }

        log.info("targetUrl: " + targetUrl + otherOptionParam);
        Map<String, String> header = new HashMap<>();
        header.put("Cookie", "PHPSESSID=61923269_bz1ocXLID5aRnDDB2SQvm6gWwQKbqWtv;");
        header.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.1 Safari/605.1.15");

        String tagSearchResponse = HttpUtil.sendHttp(HttpUtil.HttpRequestMethedEnum.HttpGet, targetUrl + otherOptionParam, null, header);

        //如果没有返回则直接报错
        if (null == tagSearchResponse) {
            super.botBaseService.sendMessageAuto("不准瑟瑟！", message);
            return;
        }

        //获取图片消息
        ObjectNode responseJSON = JSONUtil.toObjectNode(tagSearchResponse);
        if (responseJSON.has("error") && "false".equals(responseJSON.get("error").asText())) {
            int total = responseJSON.get("body").get("illust").get("total").asInt();
            if (total < pixivPictureTotalMin) {
                targetUrl = tagUrl;
                //如果没有则降级为100以上
                searchOption = searchOptions + "100users入り";

                searchWord = URLEncoder.encode(searchOption, "UTF-8");
                searchWord = searchWord.replace("+", "%20");

                targetUrl += searchWord;
                targetUrl += "?word=" + searchWord;

                tagSearchResponse = HttpUtil.sendHttp(HttpUtil.HttpRequestMethedEnum.HttpGet, targetUrl + otherOptionParam, null, header);

                //如果没有返回则直接报错
                if (null == tagSearchResponse) {
                    super.botBaseService.sendMessageAuto("不准瑟瑟！", message);
                    return;
                }

                responseJSON = JSONUtil.toObjectNode(tagSearchResponse);
                if (responseJSON.has("error") && "false".equals(responseJSON.get("error").asText())) {
                    total = responseJSON.get("body").get("illust").get("total").asInt();
                    if (total < pixivPictureTotalMin) {
                        targetUrl = tagUrl;
                        //如果没有则降级为0以上
                        searchOption = searchOptions;

                        searchWord = URLEncoder.encode(searchOption, "UTF-8");
                        searchWord = searchWord.replace("+", "%20");

                        targetUrl += searchWord;
                        targetUrl += "?word=" + searchWord;

                        tagSearchResponse = HttpUtil.sendHttp(HttpUtil.HttpRequestMethedEnum.HttpGet, targetUrl + otherOptionParam, null, header);

                        //如果没有返回则直接报错
                        if (null == tagSearchResponse) {
                            super.botBaseService.sendMessageAuto("不准瑟瑟！", message);
                            return;
                        }

                        //获取一张随机图片
                        ArrayNode imgDataNodes = (ArrayNode) responseJSON.get("body").get("illust").get("data");
                        this.sendRandomPixivImage(message, imgDataNodes);

                    } else {
                        //如果超过则获取一张随机图片
                        ArrayNode imgDataNodes = (ArrayNode) responseJSON.get("body").get("illust").get("data");
                        this.sendRandomPixivImage(message, imgDataNodes);
                    }
                }
            } else {
                //如果超过则获取一张随机图片
                ArrayNode imgDataNodes = (ArrayNode) responseJSON.get("body").get("illust").get("data");
                this.sendRandomPixivImage(message, imgDataNodes);
            }
        }
    }


    /**
     * 发送随机pixiv形象
     *
     * @param message      消息
     * @param pictureNodes 图节点
     */
    private void sendRandomPixivImage(ObjectNode message, ArrayNode pictureNodes) {
        //如果没有，则返回报错
        if(pictureNodes.isEmpty()){
            super.botBaseService.sendMessageAuto("找不到相关tag图，这种瑟瑟也太奇怪了！", message);
            return;
        }

        int picIndex = new Random().nextInt(pictureNodes.size());

        PixivPictureInfo pixivPictureInfo = BuildPixivPictureInfoFromNode(pictureNodes, picIndex);

        String responseMessage = "";

        String resourcePath = pixivHandleService.getPixivImageCash(String.valueOf(pixivPictureInfo.getPid()));
        if (null == resourcePath) {
            try {
                resourcePath = pixivHandleService.pixivImageDownload(pixivPictureInfo.getLargeUrl());
                pixivPictureInfo.setLocalAddress(resourcePath);
                pixivHandleService.saveResourceInfo(pixivPictureInfo);
            } catch (Exception e) {
                log.error("图片消息获取错误", e.getStackTrace());
                super.botBaseService.sendMessageAuto("图片消息获失败", message);
            }
        }

        if (StringUtils.isNotEmpty(resourcePath)) {
            responseMessage += "pid: " + pixivPictureInfo.getPid() + "\r\n";
            responseMessage += "title: " + pixivPictureInfo.getTitle() + "\r\n";
            responseMessage += "artist: " + pixivPictureInfo.getAuthor() + "\r\n";
            responseMessage += "[CQ:image,file=" + resourcePath + "]";
            super.botBaseService.sendMessageAuto(responseMessage, message);
        }
    }

    /**
     * 从节点构建pixiv图片信息
     *
     * @param imgDataNodes img数据节点
     * @param picIndex     图片索引
     * @return {@link PixivPictureInfo}
     */
    private PixivPictureInfo BuildPixivPictureInfoFromNode(ArrayNode imgDataNodes, int picIndex) {
        ObjectNode pictureNode = (ObjectNode) imgDataNodes.get(picIndex);
        PixivPictureInfo pixivPictureInfo = new PixivPictureInfo();
        pixivPictureInfo.setPid(Integer.parseInt(pictureNode.get("id").asText()));
        pixivPictureInfo.setTitle(pictureNode.get("title").asText());
        pixivPictureInfo.setAuthor(pictureNode.get("userName").asText());
        String mediumUrl = pictureNode.get("url").asText();
        String largeImageUrl = mediumUrl.replace("/c/250x250_80_a2", "");
        pixivPictureInfo.setMediumUrl(mediumUrl);
        pixivPictureInfo.setLargeUrl(largeImageUrl);

        return pixivPictureInfo;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {

        String message = "/setu_new -r keqing";
        String[] options = message.split(" ");

        if (options.length > 1) {
            StringBuilder searchOptions = new StringBuilder();
            List<String> searchInput = Arrays.stream(options).filter(item -> !item.startsWith("/") && !item.startsWith("-")).collect(Collectors.toList());
            if (!searchInput.isEmpty()) {
                for (String item : searchInput) {
                    searchOptions.append(item).append(" ");
                }
            }

            String targetUrl = tagUrl;
            //默认搜索1000以上的，如果没有则就行降级
            String searchOption = searchOptions + "1000users入り";

            String searchWord = URLEncoder.encode(searchOption, "UTF-8");

            searchWord = searchWord.replace("+", "%20");
            targetUrl += searchWord;
            targetUrl += "?word=" + searchWord;

            log.info(targetUrl);

            List<String> optionInput = Arrays.stream(options).filter(item -> item.startsWith("-")).map(item -> item.substring(1).toLowerCase(Locale.ROOT)).collect(Collectors.toList());
            String r18mode = "";
            if (!optionInput.isEmpty()) {
                for (String option : optionInput) {
                    if ("r18".equals(option) || "r".equals(option)) {
                        r18mode = "r18";
                    }

                    if ("all".equals(option) || "a".equals(option)) {
                        r18mode = "all";
                    }
                }
            }

            String otherOptionParam = "";
            if (StringUtils.isNotEmpty(r18mode)) {
                if ("all".equals(r18mode)) {
                    otherOptionParam += "&order=date_d&mode=all&p=2&s_mode=s_tag&type=illust_and_ugoira&lang=zh";
                } else if ("r18".equals(r18mode)) {
                    otherOptionParam += "&order=date_d&mode=r18&p=1&s_mode=s_tag&type=illust_and_ugoira&lang=zh";
                } else {
                    otherOptionParam += "&order=date_d&mode=safe&p=1&s_mode=s_tag&type=illust_and_ugoira&lang=zh";
                }
            } else {
                otherOptionParam += "&order=date_d&mode=safe&p=1&s_mode=s_tag&type=illust_and_ugoira&lang=zh";
            }


            log.info("otherOptionParam: " + otherOptionParam);
        }


    }
}
