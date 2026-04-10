package com.community.center.common;

/**
 * 场地状态枚举类
 * UNAVAILABLE: 不可用 (0)
 * AVAILABLE: 可用 (1)
 * MAINTENANCE: 维护中 (2)
 */
public enum VenueStatusEnum {
    
    UNAVAILABLE(0, "不可用"),
    AVAILABLE(1, "可用"),
    MAINTENANCE(2, "维护中");
    
    private final Integer code;
    private final String description;
    
    VenueStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据状态码获取枚举实例
     * @param code 状态码
     * @return 枚举实例，若未找到则返回null
     */
    public static VenueStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        
        for (VenueStatusEnum status : VenueStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        
        return null;
    }
}