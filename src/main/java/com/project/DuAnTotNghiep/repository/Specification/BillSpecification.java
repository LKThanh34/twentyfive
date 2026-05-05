package com.project.DuAnTotNghiep.repository.Specification;

import com.project.DuAnTotNghiep.dto.Bill.SearchBillDto;
import com.project.DuAnTotNghiep.entity.Bill;
import com.project.DuAnTotNghiep.entity.Customer;
import com.project.DuAnTotNghiep.entity.enumClass.BillStatus;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BillSpecification implements Specification<Bill> {
    private SearchBillDto searchBillDto;

    public BillSpecification(SearchBillDto searchBillDto) {
        this.searchBillDto = searchBillDto;
    }

    @Override
    public Predicate toPredicate(Root<Bill> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        if (searchBillDto.getKeyword() != null) {
            String keyword = searchBillDto.getKeyword();

            // Create an alias for the customer join to make the left join explicit
            Join<Bill, Customer> customerJoin = root.join("customer", JoinType.LEFT);

            Predicate billCodePredicate = criteriaBuilder.like(root.get("code"), "%" + keyword + "%");
            Predicate customerNamePredicate = criteriaBuilder.like(customerJoin.get("name"), "%" + keyword + "%");
            Predicate phoneNumberPredicate = criteriaBuilder.like(customerJoin.get("phoneNumber"), "%" + keyword + "%");

            Predicate combinedPredicate = criteriaBuilder.or(
                    billCodePredicate,
//                    criteriaBuilder.isNull(root.get("customer")),
                    customerNamePredicate,
                    phoneNumberPredicate
            );

            predicates.add(combinedPredicate);
        }
        LocalDateTime sevenDaysAgoStartOfDay = LocalDateTime.now().minusDays(7).with(LocalTime.MIN);
        LocalDateTime nowEndOfDay = LocalDateTime.now().with(LocalTime.MAX);

        predicates.add(criteriaBuilder.equal(root.get("status"), BillStatus.HOAN_THANH));
        predicates.add(criteriaBuilder.between(root.get("createDate"), sevenDaysAgoStartOfDay, nowEndOfDay));
        return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}
