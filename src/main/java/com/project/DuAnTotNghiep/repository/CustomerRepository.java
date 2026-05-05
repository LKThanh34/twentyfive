package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.dto.CustomerDto.CustomerDto;
import com.project.DuAnTotNghiep.dto.Statistic.TopCustomerBuy;
import com.project.DuAnTotNghiep.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByCode(String code);



    Customer findTopByOrderByIdDesc();

    @Query("SELECT c FROM Customer c WHERE c.code LIKE %:keyword% OR c.name LIKE %:keyword% OR c.phoneNumber LIKE %:keyword%")
    Page<Customer> searchCustomerKeyword(String keyword,Pageable pageable);

//    @Query("SELECT distinct c from Customer c join Bill b on c.id = b.customer.id join BillDetail bd on b.id = bd.id where bd.id = :billDetailId")
//    Customer findByBillDetailId(@Param("billDetailId") Long billDetailId);
//
//    @Query("SELECT distinct c from Customer c join Bill b on c.id = b.customer.id join BillDetail bd on b.id = bd.id join ReturnDetail rd on bd.id = rd.billDetail.id join BillReturn br on br.id = rd.billReturn.id where bd.id = :billReturnId")
//    Customer findByBillBillReturnId(@Param("billReturnId") Long billReturnId);

    @Query(value = "SELECT LIMIT 5 c.code, c.name, COUNT(c.id) AS totalPurchases, sum(b.amount) as revenue\n" +
            "           FROM Customer c\n" +
            "           JOIN bill b on b.customer_id = c.id\n" +
            "           JOIN bill_detail bd on b.id = bd.bill_id\n" +
            "           GROUP BY c.id, c.name, c.code \n" +
            "           ORDER BY totalPurchases DESC", nativeQuery = true)
    List<TopCustomerBuy> findTopCustomersByPurchases();

    boolean existsByPhoneNumber(String phoneNumber);

    Customer findByPhoneNumber(String phoneNumber);

    Customer findByAccount_Id(Long id);
    Customer findByAccount_Email(String email);
}
