package com.mongo.filter.dao;


import com.mongo.filter.dto.filter.Filter;
import com.mongo.filter.dto.filter.FilterWrap;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.mongo.filter.dao.RepoUtil.extractCorrectFilters;
import static com.mongo.filter.dao.RepoUtil.extractCriteria;


@Repository
public class FilterRepoMongoImpl implements FilterRepo {

    private final MongoTemplate mongoTemplate;

    public FilterRepoMongoImpl(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public <DOCUMENT> List<DOCUMENT> filter(FilterWrap filterWrap, Class<DOCUMENT> collectionClass) {
        Collection<Filter> filters = filterWrap.getFilters();

        List<Field> declaredClassFields = Arrays
                .stream(collectionClass.getDeclaredFields())
                .collect(Collectors.toList());

        filters = extractCorrectFilters(filters, declaredClassFields);

        Query query = new Query();
        filters
                .forEach(
                        filter -> query.addCriteria(extractCriteria(filter))
                );

        return mongoTemplate.find(query,collectionClass);
    }





}





