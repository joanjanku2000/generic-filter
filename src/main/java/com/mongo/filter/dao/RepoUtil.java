package com.mongo.filter.dao;

import com.mongo.filter.dto.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
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

    public static <T> Predicate extractCriteria(Filter filter, CriteriaBuilder cb, Root<T> root) {
        Predicate predicate;
        Integer intValue = null;

        try {
            intValue = Integer.parseInt(filter.getValue());
        } catch (NumberFormatException numberFormatException){
            logger.info("It isn't int value");
        }

        Join<Object,Object> joinObject = null;
        List<String> nestedFields = extractNestedFields(filter);

        if (filterIsNested(filter)) {
            // Add Neccessary Joins
            joinObject = getJoinObject(root, joinObject, nestedFields);

        }

        switch (filter.getOperator()) {
            case EQUALS:
                predicate = joinObject != null ?
                                cb.equal(joinObject.get(filter.getField().split(".")[nestedFields.size() - 1])
                                        ,filter.getValue())
                                    :
                                cb.equal(root.get(filter.getField()),filter.getValue());
                break;
            case LESS_THAN:
                predicate =
                        joinObject != null ?
                                cb.lt(joinObject.get(filter.getField().split(".")[nestedFields.size() - 1])
                                        ,intValue)
                                    :
                                cb.lt(root.get(filter.getField()),intValue);
                break;
            case GREATER_THAN:
                predicate =
                        joinObject != null ?
                                cb.gt(joinObject.get(filter.getField().split(".")[nestedFields.size() - 1])
                                        ,intValue)
                                :
                                cb.gt(root.get(filter.getField()),intValue);
                break;
            default:
                throw new RuntimeException("Wrong operator");

        }
        return predicate;
    }

    private static <T> Join<Object, Object> getJoinObject(Root<T> root, Join<Object, Object> joinObject, List<String> nestedFields ) {
        int iteration = 1;
        Join<Object, Object> previous = null;
        for (String field : nestedFields.subList(1, nestedFields.size() - 1)) {

            if (iteration == 1)
                joinObject = root.join(field);
            else
                joinObject = previous.join(field);

            previous = joinObject;
            iteration++;
        }
        return joinObject;
    }

    private static List<String> extractNestedFields(Filter filter){
        return Arrays.asList(filter.getField().split("."));
    }

    private static boolean filterIsNested(Filter filter) {
        return filter.getField().contains(".");
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
