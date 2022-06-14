package com.gccbenben.qqbotservice.bean.Pixiv;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * pixiv图片信息
 *
 * @author GccBenben
 * @date 2022/06/13
 */
@Data
@TableName("pixiv_picture_info")
public class PixivPictureInfo {

    /**
     * pid
     */
    private int pid;

    /**
     * 作者
     */
    private String author;

    /**
     * 标题
     */
    private String title;

    /**
     * 本地地址
     */
    private String localAddress;

    /**
     * 中等size图片url
     */
    private String mediumUrl;

    /**
     * 大size图片url
     */
    private String largeUrl;

    @Override
    public String toString() {
        return "PixivPictureInfo{" +
                "pid=" + pid +
                ", author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", local_address='" + localAddress + '\'' +
                ", medium_url='" + mediumUrl + '\'' +
                ", large_url='" + largeUrl + '\'' +
                '}';
    }
}
