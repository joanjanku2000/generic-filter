package com.mongo.filter.dao;

import com.mongo.filter.dto.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

public class RepoUtil {

    private static Logger logger = LoggerFactory.getLogger(RepoUtil.class);

    public static boolean isHierarchyPresent(String[] tokens, int i, List<Field> fields) throws ClassNotFoundException {
        if (i >= tokens.length) {
            return true;
        }
        for (Field field : fields) {
            String genericTypeClassName = null;

            if (field.getGenericType().getTypeName().contains("java.util.List")){
                genericTypeClassName = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0].getTypeName();
            }

            logger.info("field {} == {}",field.getName(),tokens[i]);
            if (tokens[i].equals(field.getName())) {
                if (genericTypeClassName != null){ // meaning it's a list
                    logger.info("Das ist list of type {} ",genericTypeClassName);
                    return isHierarchyPresent(tokens
                            , ++i
                            , Arrays.stream(Class.forName(genericTypeClassName)
                                    .getDeclaredFields())
                                    .collect(Collectors.toList()));
                } else {
                    return isHierarchyPresent(tokens
                            , ++i
                            , Arrays.stream(field.getType()
                                    .getDeclaredFields())
                                    .collect(Collectors.toList()));
                }

            }
        }

        return false;
    }

    public static CriteriaDefinition extractCriteria(Filter filter){
        CriteriaDefinition criteriaDefinition ;
        switch (filter.getOperator()){
            case EQUALS:
                criteriaDefinition = Criteria.where(filter.getField()).is(filter.getValue());
                break;
            case LESS_THAN:
                criteriaDefinition = Criteria.where(filter.getField()).lte(filter.getValue());
                break;
            case GREATER_THAN:
                criteriaDefinition = Criteria.where(filter.getField()).gte(filter.getValue());
                break;
            default:
                throw new RuntimeException("Wrong operator");

        }
        return criteriaDefinition;
    }

    public static Predicate extractCriteria(Filter filter, CriteriaBuilder cb, Root root){
        Predicate predicate = null;
        Integer intValue = null;

        try {
            intValue = Integer.parseInt(filter.getValue());
        } catch (NumberFormatException numberFormatException){
            logger.info("It isnt int value");
        }

        switch (filter.getOperator()){
            case EQUALS:
                predicate = cb.equal(root.get(filter.getField()),filter.getValue());
                break;
            case LESS_THAN:
                predicate = cb.lt(root.get(filter.getField()),intValue);
                break;
            case GREATER_THAN:
                predicate = cb.gt(root.get(filter.getField()),intValue);
                break;
            default:
                throw new RuntimeException("Wrong operator");

        }
        return predicate;
    }

    public static Collection<Filter> extractCorrectFilters(Collection<Filter> filters, List<Field> declaredFields) {

        Map<String, Filter> filterMap = new HashMap<>();
        filters.forEach(filter -> filterMap.put(filter.getField(), filter));

        if (filters.isEmpty() && declaredFields.isEmpty()) {
            return filterMap.values();
        }

        filters.stream()
                .map(Filter::getField)
                .forEach(
                        filter -> {
                            if (filter.contains(".")) {

                                String[] tokens = filter.split(".");
                                try {
                                    if (!isHierarchyPresent(tokens, 0, declaredFields)) {
                                        filterMap.remove(filter);
                                    }
                                } catch (ClassNotFoundException e) {
                                    // Do nothing
                                }

                            } else {
                                if (!declaredFields.stream()
                                        .map(Field::getName)
                                        .collect(Collectors.toList())
                                        .contains(filter)) {
                                    filterMap.remove(filter);
                                }
                            }
                        }
                );
        return filterMap.values();
    }

}
