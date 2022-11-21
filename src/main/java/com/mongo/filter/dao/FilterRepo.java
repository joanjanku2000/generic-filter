package com.mongo.filter.dao;

import com.mongo.filter.dto.filter.FilterWrap;

import java.util.List;

public interface FilterRepo {

    <DOCUMENT> List<DOCUMENT> filter(FilterWrap filterWrap, Class<DOCUMENT> collectionClass);
}
