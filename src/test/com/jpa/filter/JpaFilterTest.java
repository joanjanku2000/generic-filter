package com.jpa.filter;

import com.jpa.filter.dto.Filter;
import com.jpa.filter.dto.InternalOperator;
import com.jpa.filter.dto.ValueType;
import com.jpa.filter.util.Book;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

import java.time.chrono.ChronoLocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.jpa.filter.dao.RepoUtil.extractCriteria;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    Path<String> path ;


    private void initializePath(){
        path = new Path<String>() {
            @Override
            public Bindable<String> getModel() {
                return null;
            }

            @Override
            public Path<?> getParentPath() {
                return null;
            }

            @Override
            public <Y> Path<Y> get(SingularAttribute<? super String, Y> singularAttribute) {
                return null;
            }

            @Override
            public <E, C extends Collection<E>> Expression<C> get(PluralAttribute<String, C, E> pluralAttribute) {
                return null;
            }

            @Override
            public <K, V, M extends Map<K, V>> Expression<M> get(MapAttribute<String, K, V> mapAttribute) {
                return null;
            }

            @Override
            public Expression<Class<? extends String>> type() {
                return null;
            }

            @Override
            public <Y> Path<Y> get(String s) {
                return null;
            }

            @Override
            public Predicate isNull() {
                return null;
            }

            @Override
            public Predicate isNotNull() {
                return null;
            }

            @Override
            public Predicate in(Object... objects) {
                return null;
            }

            @Override
            public Predicate in(Expression<?>... expressions) {
                return null;
            }

            @Override
            public Predicate in(Collection<?> collection) {
                return null;
            }

            @Override
            public Predicate in(Expression<Collection<?>> expression) {
                return null;
            }

            @Override
            public <X> Expression<X> as(Class<X> aClass) {
                return null;
            }

            @Override
            public Selection<String> alias(String s) {
                return null;
            }

            @Override
            public boolean isCompoundSelection() {
                return false;
            }

            @Override
            public List<Selection<?>> getCompoundSelectionItems() {
                return null;
            }

            @Override
            public Class<? extends String> getJavaType() {
                return String.class;
            }

            @Override
            public String getAlias() {
                return null;
            }
        } ;
    }


    @Test
    void extractCriteria_Test_Pass_String() {
        initializePath();

        when(sessionFactory.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery()).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Book.class)).thenReturn(root);
        when(root.get(any(String.class))).thenReturn(path);

        Filter filter = new Filter();
        filter.setField("name");
        filter.setValue("Test");
        filter.setOperator(InternalOperator.EQUALS);
        filter.setType(ValueType.STRING);

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

        CriteriaQuery<Object> criteriaQuery = cb.createQuery();
        Root<Book> root = criteriaQuery.from(Book.class);

        Mockito.when(cb.equal(root.get("name"), "Test"))
                .thenReturn(new Predicate() {
            @Override
            public BooleanOperator getOperator() {
                return null;
            }

            @Override
            public boolean isNegated() {
                return false;
            }

            @Override
            public List<Expression<Boolean>> getExpressions() {
                return null;
            }

            @Override
            public Predicate not() {
                return null;
            }

            @Override
            public Predicate isNull() {
                return null;
            }

            @Override
            public Predicate isNotNull() {
                return null;
            }

            @Override
            public Predicate in(Object... objects) {
                return null;
            }

            @Override
            public Predicate in(Expression<?>... expressions) {
                return null;
            }

            @Override
            public Predicate in(Collection<?> collection) {
                return null;
            }

            @Override
            public Predicate in(Expression<Collection<?>> expression) {
                return null;
            }

            @Override
            public <X> Expression<X> as(Class<X> aClass) {
                return null;
            }

            @Override
            public Selection<Boolean> alias(String s) {
                return null;
            }

            @Override
            public boolean isCompoundSelection() {
                return false;
            }

            @Override
            public List<Selection<?>> getCompoundSelectionItems() {
                return null;
            }

            @Override
            public Class<? extends Boolean> getJavaType() {
                return null;
            }

            @Override
            public String getAlias() {
                return "test";
            }
        }) ;

        Predicate predicate = cb.equal(root.get("name"), "Test");
        Predicate toCompare
                = extractCriteria(filter, cb, root);
        logger.info("{}", predicate.getAlias());
        logger.info("{}", toCompare.getAlias());
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
        initializePath();

        when(sessionFactory.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery()).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Book.class)).thenReturn(root);
        when(root.get(any(String.class))).thenReturn(path);

        Filter filter = new Filter();
        filter.setField("name");
        filter.setValue("Test_Incompatible");
        filter.setOperator(InternalOperator.EQUALS);
        filter.setType(ValueType.STRING);

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

        CriteriaQuery<Object> criteriaQuery = cb.createQuery();
        Root<Book> root = criteriaQuery.from(Book.class);

        Mockito.when(cb.equal(root.get("name"), "Test"))
                .thenReturn(new Predicate() {
                    @Override
                    public BooleanOperator getOperator() {
                        return null;
                    }

                    @Override
                    public boolean isNegated() {
                        return false;
                    }

                    @Override
                    public List<Expression<Boolean>> getExpressions() {
                        return null;
                    }

                    @Override
                    public Predicate not() {
                        return null;
                    }

                    @Override
                    public Predicate isNull() {
                        return null;
                    }

                    @Override
                    public Predicate isNotNull() {
                        return null;
                    }

                    @Override
                    public Predicate in(Object... objects) {
                        return null;
                    }

                    @Override
                    public Predicate in(Expression<?>... expressions) {
                        return null;
                    }

                    @Override
                    public Predicate in(Collection<?> collection) {
                        return null;
                    }

                    @Override
                    public Predicate in(Expression<Collection<?>> expression) {
                        return null;
                    }

                    @Override
                    public <X> Expression<X> as(Class<X> aClass) {
                        return null;
                    }

                    @Override
                    public Selection<Boolean> alias(String s) {
                        return null;
                    }

                    @Override
                    public boolean isCompoundSelection() {
                        return false;
                    }

                    @Override
                    public List<Selection<?>> getCompoundSelectionItems() {
                        return null;
                    }

                    @Override
                    public Class<? extends Boolean> getJavaType() {
                        return null;
                    }

                    @Override
                    public String getAlias() {
                        return "test";
                    }
                }) ;

        Predicate predicate = cb.equal(root.get("name"), "Test");
        Predicate toCompare
                = extractCriteria(filter, cb, root);
        logger.info("{}", predicate.getAlias());

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
        initializePath();

        when(sessionFactory.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery()).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Book.class)).thenReturn(root);
        when(root.get(any(String.class))).thenReturn(path);

        Filter filter = new Filter();
        filter.setField("name");
        filter.setValue("Test");
        filter.setOperator(InternalOperator.EQUALS);
        filter.setType(ValueType.NUMERIC);

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





}
