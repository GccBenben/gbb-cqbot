package com.gccbenben.qqbotservice.component.Actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gccbenben.qqbotservice.bean.ocg.OcgDualCardInfo;
import com.gccbenben.qqbotservice.bean.ocg.YgoCard;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.Action;
import com.gccbenben.qqbotservice.component.messageMethodHandleStrategy.IMethodHandleStrategy;
import com.gccbenben.qqbotservice.service.YgoCardService;
import com.gccbenben.qqbotservice.utils.HttpUtil;
import com.gccbenben.qqbotservice.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.cookie.Cookie;
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
 * @author GccBenben
 * @date 2022/05/31
 */
@Action(name = "our_ocg_search")
@Component
@Slf4j
public class OcgAction extends BaseAction implements IMethodHandleStrategy {

    private static final String SEARCH_URL = "https://www.ourocg.cn/advance";

    private String XSRF_TOKEN = "eyJpdiI6Ik9uajNHaXpCSWQxWlZoNDdodzNGZ1E9PSIsInZhbHVlIjoidG83bk1SaldEdG9aVkdFRGkwelNXSUEyTDREWk9nNXpISnpVRUhvRnBmSXB6eFJKOG1RNytUR0RRanlBS1QyTFp1TVNKdm90VGc5SzI2VWdEMmNPbXc9PSIsIm1hYyI6IjIzYjlmMjdiYzJkMDllODJkMWE2YTIzZTQ1Mzc0NDRiOTE5YjllNmZiMTM2YzMzOTBkM2Y1OTU3NDE1YjQ2MzIifQ==";

    private static final String imageBase = "/ygoInfo/cards/";
//    private static final String imageBase = "/ygo/card/";

    protected static YgoCardService ygoCardService;

    /**
     * 注入处理服务
     *
     * @param ygoCardService pixiv处理服务
     */
    @Autowired
    public void setYgoCardService(YgoCardService ygoCardService) {
        this.ygoCardService = ygoCardService;
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
        List<String> optionInput = Arrays.stream(options).filter(item -> item.startsWith("-")).map(item -> item.substring(1).toLowerCase(Locale.ROOT)).collect(Collectors.toList());

        StringBuilder searchOptions = new StringBuilder();
        List<String> searchInput = Arrays.stream(options).filter(item -> !item.startsWith("/") && !item.startsWith("-")).collect(Collectors.toList());
        if (!searchInput.isEmpty()) {
            for (String item : searchInput) {
                searchOptions.append(item);
            }
        } else {
            super.botBaseService.sendMessageAuto("未输入卡查关键字", message);
            return "";
        }

//        searchCard(message, optionInput, searchOptions.toString());
        searchCardByYgo(message, optionInput, searchOptions.toString());
        return null;
    }

    /**
     * 从ygo解包来的资源获取卡片查询结果
     *
     * @param message 消息
     * @param options 选项
     * @param query   查询
     */
    private void searchCardByYgo(ObjectNode message, List<String> options, String query){
        String groupId = message.get("group_id").asText();
        List<YgoCard> ygoCards = ygoCardService.queryCardByName(query);

        ArrayNode responseArray = JSONUtil.buildJSONArray();
        ygoCards.forEach(card ->{

            ObjectNode baseNode = JSONUtil.buildJSONObject();
            baseNode.put("type", "node");
            ObjectNode dataNode = JSONUtil.buildJSONObject();
            dataNode.put("name", "bakabaka");
            dataNode.put("uin", "2253141704");
            dataNode.put("content",card.toBotResponse(imageBase));
            baseNode.set("data", dataNode);
            responseArray.add(baseNode);
        });

        if(responseArray.isEmpty()){
            super.botBaseService.sendMessageAuto("未查询到卡片", message);
        }else{
            super.botBaseService.sendGroupMessageForward(responseArray, groupId);
        }
    }


    /**
     * 搜索卡，从第三方网站ocg获取
     *
     * @param message 消息
     * @param options 选项
     * @param query   查询
     */
    private void searchCard(ObjectNode message, List<String> options, String query){
        String groupId = message.get("group_id").asText();

        //设置查询query
        ObjectNode queryNode = JSONUtil.buildJSONObject();
        queryNode.put("query", query);
        queryNode.put("page", 1);
        queryNode.put("orderType", "0");

        int contentLength = getContentLength(queryNode);
        String csrfToken = null;

        //获取cookie和csrf-token
        Map responseLogin = HttpUtil.sendHttpByStringGetCookie(HttpUtil.HttpRequestMethedEnum.HttpGet, SEARCH_URL, null, null);
        String responseContent = responseLogin.get("responseContent").toString();
        Document parse = Jsoup.parse(responseContent);

        List<Cookie> cookies = (List<Cookie>) responseLogin.get("cookies");

        Elements attr = parse.select("meta[name=csrf-token]");
        if(attr!=null){
            Element element = attr.get(0);
            csrfToken = element.attr("content");
        }

        //如果没有获取到csrf则拦截
        if(StringUtils.isEmpty(csrfToken)){
            super.botBaseService.sendMessageAuto("查询token获取失败", message);
            return;
        }

        //设置header
        Map<String, String> header = new HashMap<>();

        header.put("X-CSRF-TOKEN", csrfToken);
        header.put("Referer", "https://www.ourocg.cn/advance");
        header.put("Accept", "application/json, text/plain, */*");
        header.put("Content-Type", "application/json;charset=utf-8");
        header.put("Origin", "https://www.ourocg.cn");
        header.put("Host", "www.ourocg.cn");
        header.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.1 Safari/605.1.15");

        String response = HttpUtil.sendHttpByJson(HttpUtil.HttpRequestMethedEnum.HttpPost, SEARCH_URL, queryNode, header, cookies);
        ArrayNode cards = (ArrayNode) JSONUtil.toObjectNode(response).get("cards");

        if(cards == null || cards.isEmpty()){
            super.botBaseService.sendMessageAuto("未查询到卡片", message);
            return;
        }

        ArrayNode responseArray = JSONUtil.buildJSONArray();
        cards.forEach(card ->{
            //缩小量
            if(responseArray.size() > 5){
                return;
            }

            OcgDualCardInfo cardInfo = new OcgDualCardInfo(card);

            ObjectNode baseNode = JSONUtil.buildJSONObject();
            baseNode.put("type", "node");
            ObjectNode dataNode = JSONUtil.buildJSONObject();
            dataNode.put("name", "bakabaka");
            dataNode.put("uin", "2253141704");
            dataNode.put("content",cardInfo.toBotResponse());
            baseNode.set("data", dataNode);
            responseArray.add(baseNode);
        });

        if(responseArray.isEmpty()){
            super.botBaseService.sendMessageAuto("未查询到卡片", message);
        }else{
            super.botBaseService.sendGroupMessageForward(responseArray, groupId);
        }

    }

    private String getCSRFToken() {
        Map response = HttpUtil.sendHttpByStringGetCookie(HttpUtil.HttpRequestMethedEnum.HttpGet, SEARCH_URL, null, null);
        String responseContent = response.get("responseContent").toString();
        Document parse = Jsoup.parse(responseContent);

        List<Cookie> cookies = (List<Cookie>) response.get("cookies");

        Elements attr = parse.select("meta[name=csrf-token]");
        if(attr!=null){
            Element element = attr.get(0);
            return element.attr("content");
        }
        return null;
    }

    /**
     * 获取header content length长度，如果header不添加则查询会被拦截
     *
     * @param queryNode 查询节点
     * @return int
     */
    private int getContentLength(ObjectNode queryNode){
        String query = JSONUtil.toJSONString(queryNode);
        byte[] queryBytes = new byte[512];
        try {
            queryBytes = query.getBytes("UTF-8");
        }
        catch ( UnsupportedEncodingException e ) {
            log.error("My computer hates UTF-8");
        }

        int contentLength_ = queryBytes.length;
        return contentLength_;
    }

    public static void main(String[] args){
        ObjectNode queryNode = JSONUtil.buildJSONObject();
        queryNode.put("query", "青眼白龙 ");
        queryNode.put("page", 1);
        queryNode.put("orderType", "0");

//        String response = "{\"query\":\"青眼白龙 \",\"page\":1,\"orderType\":\"0\"}";
        String query = JSONUtil.toJSONString(queryNode);
        log.info("send query is: " + query);
        byte[] responseBytes = new byte[512];
        try {
            responseBytes = query.getBytes("UTF-8");
        }
        catch ( UnsupportedEncodingException e ) {
            System.err.print("My computer hates UTF-8");
        }

        int contentLength_ = responseBytes.length;
        log.info("contentLength is: " + contentLength_);
    }
}
