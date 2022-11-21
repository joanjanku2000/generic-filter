package com.mongo.filter.dto.filter;


import java.util.List;

public class FilterWrap {
    private List<Filter> filters;
    private String collection;

    public FilterWrap() {
        // Empty
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }
}
