package com.project.DuAnTotNghiep.dto.Brand;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandDto {
    private Long id;

    @NotBlank(message = "Mã nhãn hàng không được để trống")
    @NotNull(message = "Mã nhãn hàng không được để trống")
    @NotEmpty(message = "Mã nhãn hàng không được để trống")
    @Size(max = 20, message = "Mã nhãn hàng không được vượt quá 20 ký tự")
    private String code;

    @NotBlank(message = "Tên nhãn hàng không được để trống")
    @NotNull(message = "Tên nhãn hàng không được để trống")
    @Size(max = 50, message = "Tên nhãn hàng không được vượt quá 50 ký tự")
    private String name;
}
