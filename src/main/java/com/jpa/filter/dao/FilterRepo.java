package com.jpa.filter.dao;

import com.jpa.filter.dto.FilterWrap;

import java.util.List;

/**
 * FilterRepo Interface.
 * Contains one single method used for filtering
 */
public interface FilterRepo {

    /**
     * Filter method to be used by the client
     *
     * @param filterWrap {@link FilterWrap}
     * @param collectionClass {@link Class}
     * @return {@link List<ENTITY>} -- List of Results from filtering
     * <br/>
     * throws {@link IllegalArgumentException}
     */
    <ENTITY> List<ENTITY> filter(FilterWrap filterWrap, Class<ENTITY> collectionClass);
}
