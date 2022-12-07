package com.jpa.filter.util;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Util {

    public static Path<String> mockPath(){
        return new Path<String>() {
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

    public static Predicate mockPredicateAlias(){
        return new Predicate() {
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
        } ;
    }
}
