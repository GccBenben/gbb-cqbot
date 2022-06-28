package com.gccbenben.qqbotservice.service;

import com.gccbenben.qqbotservice.bean.ocg.YgoCard;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ygo卡服务
 *
 * @author GccBenben
 * @date 2022/06/28
 */
@Service
public interface YgoCardService {

    List<YgoCard> queryCardByName(String name);
}
