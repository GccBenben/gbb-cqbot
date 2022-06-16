package com.gccbenben.qqbotservice.bean.ocg;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

@Data
@TableName("dual_card_info")
public class OcgDualCardInfo {

    public OcgDualCardInfo(JsonNode card){
        if(card.has("name")){
            setName(card.get("name").asText());
        }

        if(card.has("name_ja")){
            setNameJp(card.get("name_ja").asText());
        }

        if(card.has("type_st")){
            setTypeSt(card.get("type_st").asText());
        }

        if(card.has("type_val") && !"null".equals(card.get("type_val").asText())){
            setTypeVal(Integer.parseInt(card.get("type_val").asText()));
        }

        if(card.has("level") && !"null".equals(card.get("level").asText())){
            setLevel(Integer.parseInt(card.get("level").asText()));
        }

        if(card.has("img_url")){
            setImageUrl(card.get("img_url").asText());
        }

        if(card.has("attribute")){
            setAttribute(card.get("attribute").asText());
        }

        if(card.has("race")){
            setRace(card.get("race").asText());
        }

        if(card.has("desc_nw")){
            setDesc(card.get("desc_nw").asText());
        }

        if(card.has("atk")){
//            setAttack(Integer.parseInt(card.get("atk").asText()));
            if(StringUtils.isNotEmpty(card.get("atk").asText()) && !"null".equals(card.get("atk").asText())){
                setAttack(Integer.parseInt(card.get("atk").asText()));
            }else{
                setAttack(0);
            }
        }

        if(card.has("def")){
            if(StringUtils.isNotEmpty(card.get("def").asText()) && !"null".equals(card.get("def").asText())){
                setDefence(Integer.parseInt(card.get("def").asText()));
            }else{
                setDefence(0);
            }

        }
    }

    @TableId(type= IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String nameJp;

    private String typeSt;

    private int typeVal;

    private int level;

    private String imageUrl;

    private String attribute;

    private String race;

    private String desc;

    private int attack;

    private int defence;

    @Override
    public String toString() {
        return "OcgDualCardInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", nameJp='" + nameJp + '\'' +
                ", typeSt='" + typeSt + '\'' +
                ", typeVal=" + typeVal +
                ", level=" + level +
                ", imageUrl='" + imageUrl + '\'' +
                ", attribute='" + attribute + '\'' +
                ", desc='" + desc + '\'' +
                ", attack=" + attack +
                ", defence=" + defence +
                '}';
    }

    /**
     * 生成当前卡发送消息内容
     *
     * @return {@link String}
     */
    public String toBotResponse(){
        StringBuilder response = new StringBuilder();
        response.append("[CQ:image,file=" + this.imageUrl + "]" + "\r\n");
        response.append(this.name + "    /类型：" + this.typeSt +  "\r\n");
        if(this.typeVal == 1){
            response.append("攻: " + this.attack + "  /防: " + this.defence + " / " + this.attribute + " / " + this.race + "\r\n");
        }
        response.append("描述: " + this.desc);

        return response.toString();
    }
}
