package com.project.DuAnTotNghiep.dto.BillReturn;

import com.project.DuAnTotNghiep.dto.CustomerDto.CustomerDto;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
public class BillReturnDto {

    private Long id;

    private String code;

    private String returnReason;

    private CustomerDto customer;

    private LocalDateTime returnDate;

    private Double returnMoney;

    private boolean isCancel;

    private int returnStatus;

}
