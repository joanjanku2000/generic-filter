package com.jpa.filter.dao;

import com.jpa.filter.dto.filter.Filter;
import com.jpa.filter.dto.filter.FilterWrap;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class FilterRepoJpaImpl implements FilterRepo {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public <DOCUMENT> List<DOCUMENT> filter(FilterWrap filterWrap, Class<DOCUMENT> collectionClass) {
        Collection<Filter> filters = filterWrap.getFilters();

        List<Field> declaredClassFields = Arrays
                .stream(collectionClass.getDeclaredFields())
                .collect(Collectors.toList());

        filters = RepoUtil.extractCorrectFilters(filters, declaredClassFields);

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<DOCUMENT> criteriaQuery = criteriaBuilder.createQuery(collectionClass);
        Root<DOCUMENT> root = criteriaQuery.from(collectionClass);

        List<Predicate> predicates = predicates(filters, criteriaBuilder, root,collectionClass);

        criteriaQuery.select(root).where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    /**
     * Helper method used to extract the predicates from the given filters
     * @param filters {@link Collection<Filter>}
     * @param criteriaBuilder {@link CriteriaBuilder}
     * @param root {@link Root}
     * @param clazz {@link Class}
     * @return {@link List<Predicate>}
     * @param <DOCUMENT>
     */
    private <DOCUMENT> List<Predicate> predicates(Collection<Filter> filters
                                                , CriteriaBuilder criteriaBuilder
                                                , Root<DOCUMENT> root
                                                ,Class<DOCUMENT> clazz) {
        return filters
                .stream()
                .map(filter -> RepoUtil.extractCriteria(filter, criteriaBuilder, root,clazz))
                .collect(Collectors.toList());
    }
}
