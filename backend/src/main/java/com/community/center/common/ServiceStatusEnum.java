package com.community.center.common;

/**
 * 服务状态枚举类
 * INACTIVE: 未启用 (0)
 * ACTIVE: 启用中 (1)
 * DISABLED: 已下架 (2)
 */
public enum ServiceStatusEnum {
    
    INACTIVE(0, "未启用"),
    ACTIVE(1, "启用中"),
    DISABLED(2, "已下架");
    
    private final Integer code;
    private final String description;
    
    ServiceStatusEnum(Integer code, String description) {
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
    public static ServiceStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        
        for (ServiceStatusEnum status : ServiceStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        
        return null;
    }
}