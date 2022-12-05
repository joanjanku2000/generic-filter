package com.jpa.filter.dao;

import com.jpa.filter.dto.filter.FilterWrap;

import java.util.List;

/**
 * FilterRepo Interface.
 * Contains one single method used for filtering
 */
public interface FilterRepo {

    /**
     * Filter method to be used by the client
     *
     * @param filterWrap {@link com.jpa.filter.dto.filter.FilterWrap}
     * @param collectionClass {@link Class}
     * @return {@link List<DOCUMENT>} -- List of Results from filtering
     * <br/>
     * throws {@link IllegalArgumentException}
     */
    <DOCUMENT> List<DOCUMENT> filter(FilterWrap filterWrap, Class<DOCUMENT> collectionClass);
}
