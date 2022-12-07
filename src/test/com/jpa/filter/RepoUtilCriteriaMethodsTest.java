package com.jpa.filter;

import com.jpa.filter.dto.Filter;
import com.jpa.filter.dto.InternalOperator;
import com.jpa.filter.dto.ValueType;
import com.jpa.filter.dto.builder.FilterBuilder;
import com.jpa.filter.util.Book;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.jpa.filter.dao.RepoUtil.extractCriteria;
import static com.jpa.filter.util.Util.mockPath;
import static com.jpa.filter.util.Util.mockPredicateAlias;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class RepoUtilCriteriaMethodsTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private CriteriaBuilder criteriaBuilder = Mockito.mock(CriteriaBuilder.class);

    @Mock
    private CriteriaQuery criteriaQuery = Mockito.mock(CriteriaQuery.class);

    @Mock
    private Root root = Mockito.mock(Root.class);

    private Path<String> path = mockPath();;


    @Test
    void extractCriteria_Test_Pass_String() {

        mockInternalJpa();

        Filter filter = FilterBuilder.createFilter("name")
                .value("Test")
                .operator(InternalOperator.EQUALS)
                .type(ValueType.STRING)
                .build();


        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

        CriteriaQuery<Object> criteriaQuery = cb.createQuery();
        Root<Book> root = criteriaQuery.from(Book.class);

        Mockito.when(cb.equal(root.get("name"), "Test"))
                .thenReturn(mockPredicateAlias()) ;

        Predicate predicate = cb.equal(root.get("name"), "Test");
        Predicate toCompare
                = extractCriteria(filter, cb, root);

        /*
         * toCompare and predicate should come both as a result
         * of cb.equal(root.get("name"), "Test")
         * and cb.equal(root.get("name"), "Test") is mocked
         * to return a "test" value when getAlias is called
         */
        Assertions.assertEquals(toCompare.getAlias(), predicate.getAlias());
    }

    @Test
    void extractCriteria_Test_Fail_String_InCompatibleValue() {
        mockInternalJpa();

        Filter filter = FilterBuilder.createFilter("name")
                .value("Test_Incompatible")
                .operator(InternalOperator.EQUALS)
                .type(ValueType.STRING)
                .build();


        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Object> criteriaQuery = cb.createQuery();
        Root<Book> root = criteriaQuery.from(Book.class);


        Predicate toCompare
                = extractCriteria(filter, cb, root);

        /*
         * toCompare should be null, because there is no Mockito mocking
         * cb.equal(root.get("name") , "Test_Incompatible"),
         * which is the correct result of extractCriteria of
         * the specified filter
         */
        Assertions.assertNull(toCompare);
    }

    @Test
    void extractCriteria_Test_Fail_IllegalArgumenException_1() {

        mockInternalJpa();

        Filter filter = FilterBuilder.createFilter("name")
                .value("Test_Incompatible")
                .operator(InternalOperator.EQUALS)
                .type(ValueType.NUMERIC)
                .build();


        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Object> criteriaQuery = cb.createQuery();
        Root<Book> root = criteriaQuery.from(Book.class);

        /*
            * Illegal Argument Exception is thrown
            * because field name is of type String but is being provided
            * as Numeric in the type field
         */
        Assertions.assertThrows(IllegalArgumentException.class ,
                () -> extractCriteria(filter, cb, root));
    }

    private void mockInternalJpa() {
        when(sessionFactory.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery()).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Book.class)).thenReturn(root);
        when(root.get(any(String.class))).thenReturn(path);
    }


}
