package com.gccbenben.qqbotservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gccbenben.qqbotservice.bean.Pixiv.PixivPictureInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * pixiv mapper
 *
 * @author GccBenben
 * @date 2022/06/13
 */
@Mapper
public interface PixivMapper extends BaseMapper<PixivPictureInfo> {

}
