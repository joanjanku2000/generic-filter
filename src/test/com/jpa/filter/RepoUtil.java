package com.jpa.filter;

import com.jpa.filter.dto.Filter;
import com.jpa.filter.dto.InternalOperator;
import com.jpa.filter.dto.ValueType;
import com.jpa.filter.dto.builder.FilterBuilder;
import com.jpa.filter.util.Book;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.jpa.filter.dao.RepoUtil.*;

class RepoUtil {

    @Test
    void test_isHierarchyPresent_Pass() throws ClassNotFoundException {

        String[] filters = new String[]{"author", "name"};
        Assertions.assertTrue(isHierarchyPresent(
                filters
                , 0
                , Arrays.stream(
                                Book.class
                                        .getDeclaredFields())
                        .collect(Collectors.toList()))
        );

    }

    @Test
    void test_isHierarchyPresent_Pass_1() throws ClassNotFoundException {

        String[] filters = new String[]{"author"};
        Assertions.assertTrue(isHierarchyPresent(
                filters
                , 0
                , Arrays.stream(
                                Book.class
                                        .getDeclaredFields())
                        .collect(Collectors.toList()))
        );

    }

    @Test
    void test_isHierarchyPresent_Fail() throws ClassNotFoundException {

        String[] filters = new String[]{"author", "namee"};
        Assertions.assertFalse(isHierarchyPresent(
                filters
                , 0
                , Arrays.stream(
                                Book.class
                                        .getDeclaredFields())
                        .collect(Collectors.toList()))
        );

    }

    @Test
    void test_extractCorrectFilters(){
        List<Filter> filters = Arrays.asList(FilterBuilder
                .createFilter("name")
                .value("test_name")
                .operator(InternalOperator.EQUALS)
                .type(ValueType.NUMERIC)
                .build()
        );

        Collection<Filter> filterCollectionResult
                = extractCorrectFilters(filters,Arrays.asList(Book.class.getDeclaredFields()));

        Assertions.assertEquals(filterCollectionResult.size() , filters.size());
        Assertions.assertEquals(
                filters.get(0).getField(),
                filterCollectionResult.stream()
                .map(Filter::getField)
                .findFirst().orElse(null)

        );
    }

    @DisplayName("Recusive field")
    @Test
    void test_extractCorrectFilters_Pass_2(){
        List<Filter> filters = Arrays.asList(FilterBuilder
                .createFilter("author.name")
                .value("test_name")
                .operator(InternalOperator.EQUALS)
                .type(ValueType.NUMERIC)
                .build()
        );

        Collection<Filter> filterCollectionResult
                = extractCorrectFilters(filters,Arrays.asList(Book.class.getDeclaredFields()));

        Assertions.assertEquals(filterCollectionResult.size() , filters.size());
        Assertions.assertEquals(
                filters.get(0).getField(),
                filterCollectionResult.stream()
                        .map(Filter::getField)
                        .findFirst().orElse(null)

        );
    }

    @Test
    void test_extractCorrectFilters_Fail(){
        List<Filter> filters = Arrays.asList(FilterBuilder
                .createFilter("not_existing_name")
                .value("test_name")
                .operator(InternalOperator.EQUALS)
                .type(ValueType.NUMERIC)
                .build()
        );

        Collection<Filter> filterCollectionResult
                = extractCorrectFilters(filters,Arrays.asList(Book.class.getDeclaredFields()));

        Assertions.assertEquals(filterCollectionResult.size() , 0);

    }

    @Test
    void test_filterIsNested(){
        Filter filter = FilterBuilder
                .createFilter("author.name")
                .value("test_name")
                .operator(InternalOperator.EQUALS)
                .type(ValueType.NUMERIC)
                .build();

        Assertions.assertTrue(filterIsNested(filter));
    }
    
    @Test
    void test_extractNestedFields() {
        Filter filter = FilterBuilder
                .createFilter("author.name")
                .value("test_name")
                .operator(InternalOperator.EQUALS)
                .type(ValueType.NUMERIC)
                .build();
        
        List<String> stringsResult = Arrays.asList("author" , "name");
        
        Assertions.assertTrue(listOfStringsIsEqual(stringsResult,extractNestedFields(filter)));
    }
    
    private boolean listOfStringsIsEqual(List<String> list , List<String> list2){
       return !list.retainAll(list2);
    }
}