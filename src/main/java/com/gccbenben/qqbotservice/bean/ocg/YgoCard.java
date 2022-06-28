package com.gccbenben.qqbotservice.bean.ocg;

import lombok.Data;

/**
 * ygo卡牌消息
 *
 * @author GccBenben
 * @date 2022/06/28
 */
@Data
public class YgoCard {

    YgoCardDetail ygoCardDetail;

    YgoCardInfo ygoCardInfo;

    int id;

    String name;

    String cardText;

    String attack;

    String defence;

    String level;

    int typeKey;

    String type;

    int raceKey;

    String race;

    String imageUrl;

    int attributeKey;

    String attribute;

    @Override
    public String toString() {
        return "YgoCard{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", desc='" + cardText + '\'' +
                ", attack='" + attack + '\'' +
                ", defence='" + defence + '\'' +
                ", level='" + level + '\'' +
                ", type='" + type + '\'' +
                ", race='" + race + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", attribute='" + attribute + '\'' +
                '}';
    }

    public String toBotResponse(String imageBase) {
        this.imageUrl = this.id + ".jpg";
        StringBuilder response = new StringBuilder();
        response.append("[CQ:image,file=" + imageBase + this.imageUrl + "]" + "\r\n");
        response.append(this.name + "    类型：" + this.type + "\r\n");
        if (0 != this.raceKey) {
            if (Integer.parseInt(this.attack) <= 0) {
                response.append("攻: ?");
            } else {
                response.append("攻: " + this.attack);
            }


            if (Integer.parseInt(this.defence) <= 0) {
                response.append("  防: ?");
            } else {
                response.append("  防: " + this.defence);
            }

            response.append("  属性: " + this.attribute + "   种族: " + this.race + "\r\n");
        }
        response.append("描述: " + this.cardText);

        return response.toString();
    }
}
