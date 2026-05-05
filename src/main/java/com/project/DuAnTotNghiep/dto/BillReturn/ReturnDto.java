package com.project.DuAnTotNghiep.dto.BillReturn;

import lombok.Data;

@Data
public class ReturnDto {
    private Long productDetailId;

    //Số lượng khách hàng đổi
    private Integer quantityReturn;
}
