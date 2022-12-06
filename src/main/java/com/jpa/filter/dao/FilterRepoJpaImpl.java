package com.jpa.filter.dao;

import com.jpa.filter.dto.Filter;
import com.jpa.filter.dto.FilterWrap;
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
    public <ENTITY> List<ENTITY> filter(FilterWrap filterWrap, Class<ENTITY> collectionClass) {
        Collection<Filter> filters = filterWrap.getFilters();

        List<Field> declaredClassFields = Arrays
                .stream(collectionClass.getDeclaredFields())
                .collect(Collectors.toList());

        filters = RepoUtil.extractCorrectFilters(filters, declaredClassFields);

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ENTITY> criteriaQuery = criteriaBuilder.createQuery(collectionClass);
        Root<ENTITY> root = criteriaQuery.from(collectionClass);

        List<Predicate> predicates = predicates(filters, criteriaBuilder, root);

        criteriaQuery.select(root).where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    /**
     * Helper method used to extract the predicates from the given filters
     * @param filters {@link Collection<Filter>}
     * @param criteriaBuilder {@link CriteriaBuilder}
     * @param root {@link Root}
     * @return {@link List<Predicate>}
     * @param <ENTITY>
     */
    private <ENTITY> List<Predicate> predicates(Collection<Filter> filters
                                                , CriteriaBuilder criteriaBuilder
                                                , Root<ENTITY> root
                                               ) {
        return filters
                .stream()
                .map(filter -> RepoUtil.extractCriteria(filter, criteriaBuilder, root))
                .collect(Collectors.toList());
    }
}
