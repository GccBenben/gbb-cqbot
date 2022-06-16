package com.gccbenben.qqbotservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gccbenben.qqbotservice.bean.Pixiv.PixivPictureInfo;
import com.gccbenben.qqbotservice.mapper.PixivMapper;
import com.gccbenben.qqbotservice.service.PixivHandleService;
import com.gccbenben.qqbotservice.utils.ImageDownloadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * impl pixiv处理服务
 *
 * @author GccBenben
 * @date 2022/06/13
 */
@Service
public class PixivHandleServiceImpl implements PixivHandleService {

    @Autowired
    private PixivMapper pixivMapper;

//    private final static String setuPath = "/Users/gccbenben/qqbot/go-cqhttp_darwin_arm64/data/images/setu";
    private final static String setuPath = "/home/ubuntu/qqbot/data/images/setu";

    /**
     * 更新图片信息数据，如果不存在则插入，如果存在则更新
     *
     * @param pictureInfo 图片信息
     */
    @Override
    public void updateSinglePixivPictureInfo(PixivPictureInfo pictureInfo) {
        HashMap<String, Object> searchOption = new HashMap<>();
        int pid = pictureInfo.getPid();
        searchOption.put("pid", pid);
        List<PixivPictureInfo> exist = pixivMapper.selectByMap(searchOption);
        if (exist.isEmpty()) {
            pixivMapper.insert(pictureInfo);
        } else {
            UpdateWrapper<PixivPictureInfo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("pid", pid);
            pixivMapper.update(pictureInfo, updateWrapper);
        }


    }

    @Override
    public PixivPictureInfo queryPixivPictureRecord(String pid) {
        HashMap<String, Object> searchOption = new HashMap<>();
        searchOption.put("pid", pid);
        List<PixivPictureInfo> exist = pixivMapper.selectByMap(searchOption);
        if (exist.isEmpty()) {
            return null;
        } else {
            return exist.get(0);
        }
    }

    @Override
    public void saveResourceInfo(String pid, String artistName, String title, String resourcePath, String medium, String large) {
        PixivPictureInfo pixivPictureInfo = new PixivPictureInfo();
        pixivPictureInfo.setPid(Integer.parseInt(pid));
        pixivPictureInfo.setAuthor(artistName);
        pixivPictureInfo.setTitle(title);
        pixivPictureInfo.setLocalAddress(resourcePath);
        pixivPictureInfo.setMediumUrl(medium);
        pixivPictureInfo.setLargeUrl(large);
        updateSinglePixivPictureInfo(pixivPictureInfo);
    }

    @Override
    public void saveResourceInfo(PixivPictureInfo pixivPictureInfo){
        updateSinglePixivPictureInfo(pixivPictureInfo);
    }

    @Override
    public String pixivImageDownload(String resourceWebUrl) throws Exception {
        //图片下载
        Map header = new HashMap<>();
        header.put("Referer", "https://www.pixiv.net/");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
        Date date = new Date();
        String time = sdf.format(date);
        String savePath = setuPath + "/" + time;
        String fileName = ImageDownloadUtil.download(resourceWebUrl, savePath, header);

        return "/setu" + "/" + time + fileName;
    }

    /**
     * 得到pixiv数据库记录
     *
     * @param pid pid
     * @return {@link String}
     */
    @Override
    public String getPixivImageCash(String pid) {
        PixivPictureInfo pixivPictureInfo = queryPixivPictureRecord(pid);
        if (pixivPictureInfo != null) {
            String localAddress = pixivPictureInfo.getLocalAddress();
            if (localAddress.endsWith(".jpg")) {
                return localAddress;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


    @Override
    public List<PixivPictureInfo> test(PixivPictureInfo pixivPictureInfo) {
        return pixivMapper.selectList(null);
    }


}
