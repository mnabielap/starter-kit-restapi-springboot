package com.example.starter_kit_restapi_springboot.repository.specs;

import com.example.starter_kit_restapi_springboot.entity.Role;
import com.example.starter_kit_restapi_springboot.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> getSpecification(String search, String scope, Role role) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filter by Role
            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }

            // 2. Filter by Search & Scope
            if (search != null && !search.isEmpty()) {
                String likePattern = "%" + search.toLowerCase() + "%";
                Predicate searchPredicate = null;

                switch (scope.toLowerCase()) {
                    case "name":
                        searchPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern);
                        break;
                    case "email":
                        searchPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern);
                        break;
                    case "id":
                        try {
                            long id = Long.parseLong(search);
                            searchPredicate = criteriaBuilder.equal(root.get("id"), id);
                        } catch (NumberFormatException e) {
                            searchPredicate = criteriaBuilder.disjunction();
                        }
                        break;
                    case "all":
                    default:
                        Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern);
                        Predicate emailLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern);
                        
                        try {
                            long id = Long.parseLong(search);
                            Predicate idEqual = criteriaBuilder.equal(root.get("id"), id);
                            searchPredicate = criteriaBuilder.or(nameLike, emailLike, idEqual);
                        } catch (NumberFormatException e) {
                            searchPredicate = criteriaBuilder.or(nameLike, emailLike);
                        }
                        break;
                }
                
                if (searchPredicate != null) {
                    predicates.add(searchPredicate);
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}