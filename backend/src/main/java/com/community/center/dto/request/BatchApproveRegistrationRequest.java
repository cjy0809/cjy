package com.community.center.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Schema(description = "批量审核报名请求")
public class BatchApproveRegistrationRequest {

    @NotEmpty(message = "报名ID列表不能为空")
    @Schema(description = "报名ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> registrationIds;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态：2-已通过，3-已拒绝", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "备注信息")
    private String remark;
}