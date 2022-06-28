package com.gccbenben.qqbotservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gccbenben.qqbotservice.bean.ocg.YgoCard;
import com.gccbenben.qqbotservice.bean.ocg.YgoCardDetail;
import com.gccbenben.qqbotservice.bean.ocg.YgoCardInfo;
import com.gccbenben.qqbotservice.mapper.ygoMapper.YgoCardDetailMapper;
import com.gccbenben.qqbotservice.mapper.ygoMapper.YgoCardInfoMapper;
import com.gccbenben.qqbotservice.mapper.ygoMapper.YgoMapper;
import com.gccbenben.qqbotservice.service.YgoCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * impl ygo卡服务
 *
 * @author GccBenben
 * @date 2022/06/28
 */
@Service
public class YgoCardServiceImpl implements YgoCardService {

    @Autowired
    private YgoCardInfoMapper ygoCardInfoMapper;

    @Autowired
    private YgoCardDetailMapper ygoCardDetailMapper;

    @Autowired
    private YgoMapper ygoMapper;


    /**
     * 查询卡片名字,返回卡片消息
     *
     * @param name 名字
     * @return {@link List}<{@link YgoCard}>
     */
    @Override
    public List<YgoCard> queryCardByName(String name) {
//        QueryWrapper<YgoCardInfo> cardInfoQueryWrapper = new QueryWrapper<>();
//        cardInfoQueryWrapper.like("name", name);
//        List<YgoCardInfo> cardInfos = ygoCardInfoMapper.selectList(cardInfoQueryWrapper);
//
//        Set<Integer> cardIds = cardInfos.stream().map(YgoCardInfo::getId).collect(Collectors.toSet());
//        LambdaQueryWrapper<YgoCardDetail> wrapper = Wrappers.lambdaQuery(YgoCardDetail.class).in(YgoCardDetail::getId, cardIds);
//
//        List<YgoCardDetail> cardDetails = ygoCardDetailMapper.selectList(wrapper);
//
//        List<YgoCard> result = new ArrayList<>();
//
//        cardInfos.stream().forEach(cardInfo ->{
//            cardDetails.stream().forEach(cardDetail ->{
//                if(cardInfo.getId() == cardDetail.getId()){
//                    YgoCard ygoCard = new YgoCard(cardDetail, cardInfo);
//                    result.add(ygoCard);
//                }
//            });
//        });

        List<YgoCard> result = ygoMapper.queryCardByName(name);

        return result;
    }
}
