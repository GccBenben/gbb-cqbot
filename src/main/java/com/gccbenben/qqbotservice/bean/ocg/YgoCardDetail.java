package com.gccbenben.qqbotservice.bean.ocg;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ygo_card_status_detail")
public class YgoCardDetail {
    int id;

    int ot;

    int alias;

    int setcode;

    int type;

    int atk;

    int def;

    int level;

    int race;

    int attribute;

    int category;

    @Override
    public String toString() {
        return "YgoCardDetail{" +
                "id=" + id +
                ", ot=" + ot +
                ", alias=" + alias +
                ", setcode=" + setcode +
                ", type=" + type +
                ", atk=" + atk +
                ", def=" + def +
                ", level=" + level +
                ", race=" + race +
                ", attribute=" + attribute +
                ", category=" + category +
                '}';
    }
}
