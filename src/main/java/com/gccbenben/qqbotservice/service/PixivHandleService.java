package com.gccbenben.qqbotservice.service;

import com.gccbenben.qqbotservice.bean.Pixiv.PixivPictureInfo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * pixiv处理服务
 *
 * @author GccBenben
 * @date 2022/06/13
 */
@Service
public interface PixivHandleService {

    /**
     * 更新单一pixiv图片信息
     */
    void updateSinglePixivPictureInfo(PixivPictureInfo pixivPictureInfo);

    PixivPictureInfo queryPixivPictureRecord(String pid);

    List<PixivPictureInfo> test(PixivPictureInfo pixivPictureInfo);

    String getPixivImageCash(String pid);

    String pixivImageDownload(String resourceWebUrl) throws Exception;

    void saveResourceInfo(String pid, String artistName, String title, String resourcePath, String medium, String large);
}
