package com.gccbenben.qqbotservice.bean.ocg;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ygo_card_texts")
public class YgoCardInfo {

    int id;

    String name;

    String cardText;

    String str1;
    String str2;
    String str3;
    String str4;
    String str5;
    String str6;
    String str7;
    String str8;
    String str9;
    String str10;
    String str11;
    String str12;
    String str13;
    String str14;
    String str15;
    String str16;

    @Override
    public String toString() {
        return "YgoCardInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cardText='" + cardText + '\'' +
                ", str1='" + str1 + '\'' +
                ", str2='" + str2 + '\'' +
                ", str3='" + str3 + '\'' +
                ", str4='" + str4 + '\'' +
                ", str5='" + str5 + '\'' +
                ", str6='" + str6 + '\'' +
                ", str7='" + str7 + '\'' +
                ", str8='" + str8 + '\'' +
                ", str9='" + str9 + '\'' +
                ", str10='" + str10 + '\'' +
                ", str11='" + str11 + '\'' +
                ", str12='" + str12 + '\'' +
                ", str13='" + str13 + '\'' +
                ", str14='" + str14 + '\'' +
                ", str15='" + str15 + '\'' +
                ", str16='" + str16 + '\'' +
                '}';
    }
}
