package com.bwtp.staffService.Enum;

import lombok.Getter;

import java.util.Objects;
/**
 * <p>
 * 描述：申请常量
 * </p>
 *
 * @author blp
 * @since 2023/1/13
 */
public interface positionEnum {

    /**
     * 申请状态
     */
    enum UnbindApplyTypeEnum {

        Manager(1, "经理"),
        ProManager(2, "副经理"),
        GroupLeader(3, "小组长"),
        ShopAssistant(4, "店员");

        @Getter
        private Integer code;
        @Getter
        private String desc;

      UnbindApplyTypeEnum(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String valueOf(Integer code) {
            if (code == null) return "";
            for (UnbindApplyTypeEnum type : UnbindApplyTypeEnum.values()) {
                if (Objects.equals(type.getCode(), code)) {
                    return type.getDesc();
                }
            }
            return "";
        }

    }
}
