package com.jpa.filter;

import com.jpa.filter.util.Book;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.jpa.filter.dao.RepoUtil.isHierarchyPresent;

class FilterTest {

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
}