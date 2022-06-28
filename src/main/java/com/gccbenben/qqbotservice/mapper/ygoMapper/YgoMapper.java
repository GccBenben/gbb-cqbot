package com.gccbenben.qqbotservice.mapper.ygoMapper;

import com.gccbenben.qqbotservice.bean.ocg.YgoCard;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface YgoMapper {

    List<YgoCard> queryCardByName(String cardName);
}
