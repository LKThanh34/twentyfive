package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.dto.Bill.BillDetailDtoInterface;
import com.project.DuAnTotNghiep.dto.Bill.BillDetailProduct;
import com.project.DuAnTotNghiep.dto.Bill.BillDtoInterface;
import com.project.DuAnTotNghiep.dto.Refund.RefundDto;
import com.project.DuAnTotNghiep.dto.Statistic.DayInMonthStatistic;
import com.project.DuAnTotNghiep.dto.Statistic.MonthInYearStatistic;
import com.project.DuAnTotNghiep.dto.Statistic.OrderStatistic;
import com.project.DuAnTotNghiep.entity.Account;
import com.project.DuAnTotNghiep.entity.Bill;
import com.project.DuAnTotNghiep.entity.Customer;
import com.project.DuAnTotNghiep.entity.Product;
import com.project.DuAnTotNghiep.entity.enumClass.BillStatus;
import com.project.DuAnTotNghiep.entity.enumClass.InvoiceType;
import com.project.DuAnTotNghiep.repository.Specification.BillSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long>, JpaSpecificationExecutor<Bill> {

    @Query(value = "SELECT DISTINCT b.id AS maHoaDon,b.code AS maDinhDanh, a.name AS hoVaTen, a.phoneNumber " +
            "AS soDienThoai,b.createDate AS ngayTao, b.amount AS tongTien, b.status AS trangThai, b.invoiceType " +
            "AS loaiDon, pm.name AS hinhThucThanhToan, coalesce(br.code, '') as maDoiTra, pmt.orderId as maGiaoDich " +
            "FROM Bill b " +
            "JOIN Payment pmt on b.id = pmt.bill.id " +
            "LEFT JOIN Customer a ON b.customer.id = a.id " +
            "LEFT JOIN BillDetail bd ON b.id = bd.bill.id " +
            "LEFT JOIN PaymentMethod pm ON b.paymentMethod.id = pm.id LEFT JOIN BillReturn br on b.id = br.bill.id")
    Page<BillDtoInterface> listBill(Pageable pageable);

    @Query(value = "SELECT DISTINCT b.id AS maHoaDon,b.code AS maDinhDanh, a.name AS hoVaTen, a.phoneNumber " +
            "AS soDienThoai, b.createDate AS ngayTao, b.amount AS tongTien, b.status AS trangThai, b.invoiceType " +
            "AS loaiDon, pm.name AS hinhThucThanhToan, coalesce(br.code, '') as maDoiTra " +
            "FROM Bill b " +
            "LEFT JOIN Customer a ON b.customer.id = a.id " +
            "LEFT JOIN BillDetail bd ON b.id = bd.bill.id " +
            "LEFT JOIN PaymentMethod pm ON b.paymentMethod.id = pm.id left join BillReturn br on b.id = br.bill.id " +
            "WHERE (:maDinhDanh IS NULL OR b.code LIKE CONCAT('%', :maDinhDanh, '%')) " +
            "AND (:ngayTaoStart IS NULL OR :ngayTaoEnd IS NULL OR (b.createDate BETWEEN :ngayTaoStart AND :ngayTaoEnd)) " +
            "AND (:trangThai IS NULL OR b.status = :trangThai) " +
            "AND (:loaiDon IS NULL OR b.invoiceType= :loaiDon) "+
            "AND (:soDienThoai IS NULL OR a.phoneNumber IS NULL OR a.phoneNumber LIKE CONCAT('%', :soDienThoai, '%')) "+
            "AND (:hoVaTen IS NULL OR a.name IS NULL OR a.name LIKE CONCAT('%', :hoVaTen, '%'))")
    Page<BillDtoInterface> listSearchBill(
            @Param("maDinhDanh") String maDinhDanh,
            @Param("ngayTaoStart") LocalDateTime ngayTaoStart,
            @Param("ngayTaoEnd") LocalDateTime ngayTaoEnd,
            @Param("trangThai") BillStatus trangThai,
            @Param("loaiDon") InvoiceType loaiDon,
            @Param("soDienThoai") String soDienThoai,
            @Param("hoVaTen") String hoVaTen,
            Pageable pageable);

    @Modifying
    @Query(value = "UPDATE bill SET status=:status WHERE id=:id", nativeQuery = true)
    @Transactional
    int updateStatus(@Param("status") String status,@Param("id") Long id);


    @Query(value = "select distinct b.id as maDonHang,b.code as maDinhDanh,b.billing_address as diaChi," +
            " b.amount as tongTien,b.promotion_price as tienKhuyenMai,a.name as tenKhachHang," +
            "a.phone_number as soDienThoai,a.email as email, b.status as trangThaiDonHang, pmt.order_id as maGiaoDich, " +
            "pm.name as phuongThucThanhToan,b.invoice_type as loaiHoaDon, dc.code as voucherName, b.create_date as createdDate " +
            "from bill b full join customer a on b.customer_id=a.id full join discount_code dc on b.discount_code_id=dc.id" +
            " full join bill_detail bd on b.id=bd.bill_id join payment pmt on b.id = pmt.bill_id left join payment_method pm on b.payment_method_id=pm.id where b.id=:maHoaDon",nativeQuery = true)
    BillDetailDtoInterface getBillDetail(@Param("maHoaDon") Long maHoaDon);

    @Query(value = "select pd.id, bd.id as billDetailId, p.id as productId, p.name as tenSanPham,c.name as tenMau, s.name as kichCo, bd.moment_price as giaTien,bd.quantity as soLuong, bd.moment_price*bd.quantity as tongTien,  (\n" +
            "           SELECT top(1) link\n" +
            "           FROM image\n" +
            "           WHERE p.id = image.product_id\n" +

            "       ) AS imageUrl " +
            "from bill b join bill_detail bd on b.id=bd.bill_id join" +
            " product_detail pd on bd.product_detail_id =pd.id join" +
            " product p on pd.product_id=p.id join color c on pd.color_id=c.id join size s on pd.size_id = s.id " +
            "where b.id=:maHoaDon",nativeQuery = true)
    List<BillDetailProduct> getBillDetailProduct(@Param("maHoaDon") Long maHoaDon);

    Bill findTopByOrderByIdDesc();

    Page<Bill> findAllByStatusAndCustomer_Account_Id(BillStatus status, Long id, Pageable pageable);

    Page<Bill> findByCustomer_Account_Id(Long id, Pageable pageable);

    @Query(value = "select * from bill b where DATEDIFF(DAY, b.create_date, GETDATE()) <= 7 and b.status='HOAN_THANH'", nativeQuery = true)
    Page<Bill> findValidBillToReturn(Pageable pageable);

    @Query(value = "SELECT \n" +
            "    COALESCE(SUM(b.amount), 0) - COALESCE(SUM(br.return_money), 0) + COALESCE(SUM(rd.quantity_return * pd.price), 0) AS total\n" +
            "FROM \n" +
            "    bill b \n" +
            "    LEFT JOIN bill_return br ON b.id = br.bill_id \n" +
            "    LEFT JOIN return_detail rd ON br.id = rd.id \n" +
            "    LEFT JOIN product_detail pd ON rd.product_detail_id = pd.id \n" +
            "WHERE \n" +
            "    b.status = 'HOAN_THANH';", nativeQuery = true)
    Double calculateTotalRevenue();

    @Query(value = "SELECT\n" +
            "    COALESCE(SUM(b.amount), 0) - COALESCE(SUM(br.return_money), 0) + COALESCE(SUM(rd.quantity_return * pd.price), 0) AS total\n" +
            "FROM\n" +
            "    bill b\n" +
            "LEFT JOIN\n" +
            "    bill_return br ON b.id = br.bill_id\n" +
            "LEFT JOIN\n" +
            "    return_detail rd ON br.id = rd.id\n" +
            "LEFT JOIN\n" +
            "    product_detail pd ON rd.product_detail_id = pd.id\n" +
            "WHERE\n" +
            "    b.status = 'HOAN_THANH'\n" +
            "    AND (\n" +
            "        (b.create_date BETWEEN :startDate AND :endDate)\n" +

            "    )", nativeQuery = true)
    Double calculateTotalRevenueFromDate(String startDate, String endDate);


    @Query(value = "SELECT CONVERT(DATE, create_date) AS date, COALESCE(SUM(b.amount), 0) - COALESCE(SUM(br.return_money), 0) + COALESCE(SUM(rd.quantity_return * pd.price), 0) AS revenue\n" +
            "FROM bill b LEFT JOIN bill_return br ON b.id = br.bill_id LEFT JOIN return_detail rd ON br.id = rd.id\n" +
            "LEFT JOIN product_detail pd ON rd.product_detail_id = pd.id " +
            "WHERE (YEAR(b.create_date) = :year AND MONTH(b.create_date) = :month AND b.status='HOAN_THANH' ) " +
            "GROUP BY CONVERT(DATE, create_date)\n" +
            "ORDER BY CONVERT(DATE, create_date);", nativeQuery = true)
    List<Object[]> statisticRevenueDayInMonth(String month, String year);

    @Query(value = "SELECT MONTH(create_date) AS month, COALESCE(SUM(b.amount), 0) - COALESCE(SUM(br.return_money), 0) + COALESCE(SUM(rd.quantity_return * pd.price), 0) AS revenue\n" +
            "FROM bill b LEFT JOIN bill_return br ON b.id = br.bill_id LEFT JOIN return_detail rd ON br.id = rd.id\n" +
            "LEFT JOIN product_detail pd ON rd.product_detail_id = pd.id\n" +
            "WHERE YEAR(b.create_date) = :year and b.status='HOAN_THANH' \n" +
            "GROUP BY MONTH(b.create_date)\n" +
            "ORDER BY MONTH(b.create_date)", nativeQuery = true)
    List<Object[]> statisticRevenueMonthInYear(String year);

    @Query("select b from Bill b where b.status = 'HOAN_THANH' AND b.createDate between :fromDate and :toDate")
    List<Bill> findAllCompletedByDate(@Param("fromDate") LocalDateTime fromDate,@Param("toDate") LocalDateTime toDate);

    @Query("select count(b) from Bill b where b.status='CHO_XAC_NHAN'")
    int getTotalBillStatusWaiting();

    @Query(value = "SELECT \n" +
            "FORMAT(b.create_date, 'MM-yyyy') AS date,\n" +
            "COALESCE(SUM(b.amount), 0) - COALESCE(SUM(br.return_money), 0) + COALESCE(SUM(rd.quantity_return * pd.price), 0) AS revenue\n" +
            "FROM bill b LEFT JOIN bill_return br ON b.id = br.bill_id LEFT JOIN return_detail rd ON br.id = rd.id\n" +
            "LEFT JOIN product_detail pd ON rd.product_detail_id = pd.id\n" +
            "WHERE b.status = 'HOAN_THANH' AND ( (b.create_date BETWEEN :fromDate AND :toDate)) \n" +
            "GROUP BY \n" +
            "FORMAT(b.create_date, 'MM-yyyy')\n" +
            "ORDER BY \n" +
            "FORMAT(b.create_date, 'MM-yyyy');", nativeQuery = true)
    List<Object[]> statisticRevenueFormMonth(String fromDate, String toDate);

    @Query(value = "SELECT \n" +
            "    CONVERT(varchar, b.create_date, 23) AS date,\n" +
            "    COALESCE(SUM(b.amount), 0) - COALESCE(SUM(br.return_money), 0) + COALESCE(SUM(rd.quantity_return * pd.price), 0) AS revenue\n" +
            "FROM \n" +
            "    bill b \n" +
            "    LEFT JOIN bill_return br ON b.id = br.bill_id \n" +
            "    LEFT JOIN return_detail rd ON br.id = rd.id\n" +
            "    LEFT JOIN product_detail pd ON rd.product_detail_id = pd.id\n" +
            "WHERE \n" +
            "    b.status = 'HOAN_THANH' AND \n" +
            "    (\n" +
            "        (b.create_date BETWEEN :fromDate AND :toDate)\n" +
            "    )\n" +
            "GROUP BY \n" +
            "    CONVERT(varchar, b.create_date, 23)\n" +
            "ORDER BY \n" +
            "    CONVERT(varchar, b.create_date, 23)", nativeQuery = true)
    List<Object[]> statisticRevenueDaily(String fromDate, String toDate);

    @Query(value = "select status, count(b.status) as quantity, sum(b.amount) as revenue from bill b group by b.status", nativeQuery = true)
    List<OrderStatistic> statisticOrder();

    @Query(value = "select b.code as billCode, b.id as billId, pm.order_id as orderId, c.name as customerName, b.update_date as cancelDate, b.amount as totalAmount, pm.status_exchange as statusExchange from bill b left join customer c on b.customer_id = c.id  left join payment pm on pm.bill_id = b.id \n" +
            "join payment_method pme on pme.id = b.payment_method_id where b.status='HUY' and pme.name='CHUYEN_KHOAN' order by b.update_date desc", nativeQuery = true)
    List<RefundDto> findListNeedRefund();

    @Query(value = "SELECT DISTINCT b.id AS maHoaDon,b.code AS maDinhDanh, a.name AS hoVaTen, a.phoneNumber " +
            "AS soDienThoai,b.createDate AS ngayTao, b.amount AS tongTien, b.status AS trangThai, b.invoiceType " +
            "AS loaiDon, pm.name AS hinhThucThanhToan, coalesce(br.code, '') as maDoiTra, pmt.orderId as maGiaoDich " +
            "FROM Bill b " +
            "JOIN Payment pmt on b.id = pmt.bill.id " +
            "LEFT JOIN Customer a ON b.customer.id = a.id " +
            "LEFT JOIN BillDetail bd ON b.id = bd.bill.id " +
            "LEFT JOIN PaymentMethod pm ON b.paymentMethod.id = pm.id LEFT JOIN BillReturn br on b.id = br.bill.id")
    List<BillDtoInterface> listBill();

    @Query(value = "SELECT DISTINCT b.id AS maHoaDon,b.code AS maDinhDanh, a.name AS hoVaTen, a.phoneNumber " +
            "AS soDienThoai, b.createDate AS ngayTao, b.amount AS tongTien, b.status AS trangThai, b.invoiceType " +
            "AS loaiDon, pm.name AS hinhThucThanhToan, coalesce(br.code, '') as maDoiTra " +
            "FROM Bill b " +
            "LEFT JOIN Customer a ON b.customer.id = a.id " +
            "LEFT JOIN BillDetail bd ON b.id = bd.bill.id " +
            "LEFT JOIN PaymentMethod pm ON b.paymentMethod.id = pm.id left join BillReturn br on b.id = br.bill.id " +
            "WHERE (:maDinhDanh IS NULL OR b.code LIKE CONCAT('%', :maDinhDanh, '%')) " +
            "AND (:ngayTaoStart IS NULL OR :ngayTaoEnd IS NULL OR (b.createDate BETWEEN :ngayTaoStart AND :ngayTaoEnd)) " +
            "AND (:trangThai IS NULL OR b.status = :trangThai) " +
            "AND (:loaiDon IS NULL OR b.invoiceType= :loaiDon) "+
            "AND (:soDienThoai IS NULL OR a.phoneNumber IS NULL OR a.phoneNumber LIKE CONCAT('%', :soDienThoai, '%')) "+
            "AND (:hoVaTen IS NULL OR a.name IS NULL OR a.name LIKE CONCAT('%', :hoVaTen, '%'))")
    List<BillDtoInterface> listSearchBill( @Param("maDinhDanh") String maDinhDanh,
                                           @Param("ngayTaoStart") LocalDateTime ngayTaoStart,
                                           @Param("ngayTaoEnd") LocalDateTime ngayTaoEnd,
                                           @Param("trangThai") BillStatus trangThai,
                                           @Param("loaiDon") InvoiceType loaiDon,
                                           @Param("soDienThoai") String soDienThoai,
                                           @Param("hoVaTen") String hoVaTen);
}