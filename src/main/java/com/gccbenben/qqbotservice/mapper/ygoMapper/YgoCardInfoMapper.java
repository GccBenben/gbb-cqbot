package com.gccbenben.qqbotservice.mapper.ygoMapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gccbenben.qqbotservice.bean.ocg.OcgDualCardInfo;
import com.gccbenben.qqbotservice.bean.ocg.YgoCardInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ygo卡信息映射器
 *
 * @author GccBenben
 * @date 2022/06/28
 */
@Mapper
public interface YgoCardInfoMapper extends BaseMapper<YgoCardInfo> {
}
