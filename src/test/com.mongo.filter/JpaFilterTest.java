package com.mongo.filter;

import com.mongo.filter.dto.filter.Filter;
import com.mongo.filter.dto.filter.InternalOperator;
import com.mongo.filter.util.Book;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static com.mongo.filter.dao.RepoUtil.extractCriteria;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class JpaFilterTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Mock
    SessionFactory sessionFactory;

    @Mock
    CriteriaBuilder criteriaBuilder = Mockito.mock(CriteriaBuilder.class);

    @Mock
    CriteriaQuery criteriaQuery = Mockito.mock(CriteriaQuery.class);

    @Mock
    Root root = Mockito.mock(Root.class);


    @Test
    void extractCriteria_Test_Pass() {
        when(sessionFactory.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery()).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Book.class)).thenReturn(root);
        Filter filter = new Filter();
        filter.setField("name");
        filter.setValue("Test");
        filter.setOperator(InternalOperator.EQUALS);

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

        CriteriaQuery<Object> criteriaQuery = cb.createQuery();
        Root<Book> root = criteriaQuery.from(Book.class);
        Predicate predicate = cb.equal(root.get("name"), "Test");

//        Predicate toCompare
//                = extractCriteria(filter, cb, root);
//        logger.info("{}", toCompare.getAlias());
//        Assertions.assertEquals(toCompare.getAlias(), predicate.getAlias());
    }
}
