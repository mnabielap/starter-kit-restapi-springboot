package com.example.starter_kit_restapi_springboot.repository.specs;

import com.example.starter_kit_restapi_springboot.entity.Role;
import com.example.starter_kit_restapi_springboot.entity.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserSpecificationTest {

    @Test
    void constructorShouldBeInstantiable() {
        assertThat(new UserSpecification()).isNotNull();
    }

    @Test
    void getSpecificationShouldSupportRoleAndNameScope() {
        TestCriteria criteria = new TestCriteria();

        execute(UserSpecification.getSpecification("alice", "name", Role.ADMIN), criteria);

        assertThat(criteria.andPredicate).isNotNull();
    }

    @Test
    void getSpecificationShouldSupportEmailScope() {
        TestCriteria criteria = new TestCriteria();

        execute(UserSpecification.getSpecification("alice@example.com", "email", null), criteria);

        assertThat(criteria.andPredicate).isNotNull();
    }

    @Test
    void getSpecificationShouldSupportIdScopeWithNumericSearch() {
        TestCriteria criteria = new TestCriteria();

        execute(UserSpecification.getSpecification("42", "id", null), criteria);

        assertThat(criteria.andPredicate).isNotNull();
    }

    @Test
    void getSpecificationShouldSupportIdScopeWithNonNumericSearch() {
        TestCriteria criteria = new TestCriteria();

        execute(UserSpecification.getSpecification("abc", "id", null), criteria);

        assertThat(criteria.andPredicate).isNotNull();
    }

    @Test
    void getSpecificationShouldSupportAllScopeWithNumericSearch() {
        TestCriteria criteria = new TestCriteria();

        execute(UserSpecification.getSpecification("42", "all", null), criteria);

        assertThat(criteria.andPredicate).isNotNull();
    }

    @Test
    void getSpecificationShouldSupportAllScopeWithTextSearch() {
        TestCriteria criteria = new TestCriteria();

        execute(UserSpecification.getSpecification("alice", "all", null), criteria);

        assertThat(criteria.andPredicate).isNotNull();
    }

    @Test
    void getSpecificationShouldSupportDefaultScopeFallback() {
        TestCriteria criteria = new TestCriteria();

        execute(UserSpecification.getSpecification("alice", "unknown", null), criteria);

        assertThat(criteria.andPredicate).isNotNull();
    }

    @Test
    void getSpecificationShouldReturnAndPredicateWhenSearchIsEmpty() {
        TestCriteria criteria = new TestCriteria();

        execute(UserSpecification.getSpecification("", "all", null), criteria);

        assertThat(criteria.andPredicate).isNotNull();
    }

    @Test
    void getSpecificationShouldReturnAndPredicateWhenSearchIsNull() {
        TestCriteria criteria = new TestCriteria();

        execute(UserSpecification.getSpecification(null, "all", null), criteria);

        assertThat(criteria.andPredicate).isNotNull();
    }

    private void execute(Specification<User> specification, TestCriteria criteria) {
        specification.toPredicate(criteria.root, criteria.query, criteria.builder);
    }

    private static final class TestCriteria {
        private final Root<User> root = mock(Root.class);
        private final CriteriaQuery<?> query = mock(CriteriaQuery.class);
        private final CriteriaBuilder builder = mock(CriteriaBuilder.class);
        private final Path<Object> idPath = mock(Path.class);
        private final Path<Object> namePath = mock(Path.class);
        private final Path<Object> emailPath = mock(Path.class);
        private final Expression<String> loweredName = mock(Expression.class);
        private final Expression<String> loweredEmail = mock(Expression.class);
        private final Predicate andPredicate = mock(Predicate.class);
        private final Predicate equalPredicate = mock(Predicate.class);
        private final Predicate likePredicate = mock(Predicate.class);
        private final Predicate orPredicate = mock(Predicate.class);
        private final Predicate disjunctionPredicate = mock(Predicate.class);

        private TestCriteria() {
            when(root.get("id")).thenReturn(idPath);
            when(root.get("name")).thenReturn(namePath);
            when(root.get("email")).thenReturn(emailPath);
            when(builder.equal(any(), any())).thenReturn(equalPredicate);
            when(builder.like(any(Expression.class), anyString())).thenReturn(likePredicate);
            when(builder.lower(namePath.as(String.class))).thenReturn(loweredName);
            when(builder.lower(emailPath.as(String.class))).thenReturn(loweredEmail);
            when(builder.or(any(Predicate.class), any(Predicate.class))).thenReturn(orPredicate);
            when(builder.or(any(Predicate.class), any(Predicate.class), any(Predicate.class))).thenReturn(orPredicate);
            when(builder.disjunction()).thenReturn(disjunctionPredicate);
            when(builder.and(any(Predicate[].class))).thenReturn(andPredicate);
        }
    }
}
