package com.project.DuAnTotNghiep.repository.Specification;

import com.project.DuAnTotNghiep.dto.Bill.SearchBillDto;
import com.project.DuAnTotNghiep.entity.Bill;
import com.project.DuAnTotNghiep.entity.enumClass.BillStatus;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BillSpecification2 implements Specification<Bill> {
    private SearchBillDto searchBillDto;

    public BillSpecification2(SearchBillDto searchBillDto) {
        this.searchBillDto = searchBillDto;
    }

    @Override
    public Predicate toPredicate(Root<Bill> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        if(searchBillDto.getKeyword() != null) {
            String keyword = searchBillDto.getKeyword();

            Predicate billCodePredicate = criteriaBuilder.like(root.get("code"), "%" + keyword + "%");
            Predicate customerNamePredicate = criteriaBuilder.like(root.get("customer").get("name"), "%" + keyword + "%");
            Predicate phoneNumberPredicate = criteriaBuilder.like(root.get("customer").get("phoneNumber"), "%" + keyword + "%");

            Predicate combinedPredicate = criteriaBuilder.or(billCodePredicate, customerNamePredicate, phoneNumberPredicate);

            predicates.add(combinedPredicate);
        }

        if(searchBillDto.getFromDate() != null) {
            Predicate predicate = criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"),  searchBillDto.getFromDate());
            predicates.add(predicate);
        }

        if(searchBillDto.getToDate() != null) {
            Predicate predicate = criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), searchBillDto.getToDate() );
            predicates.add(predicate);
        }

        if(searchBillDto.getBillStatus() != null) {
            Predicate predicate = criteriaBuilder.lessThanOrEqualTo(root.get("status"), searchBillDto.getBillStatus() );
            predicates.add(predicate);
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}
