package com.community.center.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 服务预约数据传输对象
 */
@Data
public class ServiceReservationDTO {
    private Long id;
    private Long serviceId;
    private Long userId;
    private LocalDate reservationDate;
    private String reservationTime;
    private Integer status;
    private String remark;
    private String servicePerson;
    private LocalDateTime createTime;
    private String serviceName; // 服务项目名称
    private String userName; // 用户名
    private String statusName; // 状态名称
}
