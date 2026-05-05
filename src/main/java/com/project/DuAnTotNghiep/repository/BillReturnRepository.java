package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.dto.Refund.RefundDto;
import com.project.DuAnTotNghiep.entity.BillReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BillReturnRepository extends JpaRepository<BillReturn, Long>, JpaSpecificationExecutor<BillReturn> {
    BillReturn findTopByOrderByIdDesc();

    Optional<BillReturn> findByCode(String code);

    @Query(value = "select b.code as billCode, b.id as billId, br.id as billReturnId, pm.order_id as orderId, br.code as billReturnCode, c.name as customerName, br.return_date as returnDate, br.return_money as returnMoney, pme.name as paymentName, pm.status_exchange as statusExchange from bill b left join customer c on b.customer_id = c.id join bill_return br on br.bill_id = b.id left join payment pm on pm.bill_id = b.id \n" +
            "join payment_method pme on pme.id = b.payment_method_id where return_money > 0 order by br.return_date desc", nativeQuery = true)
    List<RefundDto> findListNeedRefund();
}
