package com.project.DuAnTotNghiep.dto.Bill;

public interface BillDetailProduct {

    Long getId();
    Long getProductId();

    String getBillDetailId();

    String getImageUrl();
    String getTenSanPham();

    String getTenMau();

    String getKichCo();

    Double getGiaTien();

    int getSoLuong();

    Double getTongTien();
}
