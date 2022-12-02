package com.mongo.filter.dao;

import com.mongo.filter.dto.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;

import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class RepoUtil {
    private RepoUtil(){
        // Empty Constructor to avoid instantiation
    }
    private static final Logger logger = LoggerFactory.getLogger(RepoUtil.class);

    public static boolean isHierarchyPresent(String[] tokens, int i, List<Field> fields) throws ClassNotFoundException {
        if (i >= tokens.length) {
            return true;
        }
        for (Field field : fields) {
            String genericTypeClassName = null;

            if (field.getGenericType().getTypeName().contains("java.util.List")) {
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

    public static CriteriaDefinition extractCriteria(Filter filter) {
        CriteriaDefinition criteriaDefinition ;

        switch (filter.getOperator()) {
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

    public static <T,S extends Comparable> Predicate extractCriteria(Filter filter, CriteriaBuilder cb, Root<T> root, Class clazz) {
        logger.info("Extracting criteria ... ");
        Predicate predicate;
        Integer intValue = null;

        try {
            intValue = Integer.parseInt((String) filter.getValue());
        } catch (NumberFormatException numberFormatException){
            logger.info("Value isn't int value");
        }

        Join<Object,Object> joinObject = null;
        List<String> nestedFields = extractNestedFields(filter);
        logger.info("{} nested fields",nestedFields.size());

        if (filterIsNested(filter)) {
            // Add Neccessary Joins
            logger.info("Join java type ");
            joinObject = getJoinObject(root, joinObject, nestedFields);
            logger.info("Join java type {} " ,joinObject.getJavaType() );
            clazz = joinObject.getJavaType();
        }

        Class fieldToQueryType ;

        boolean fieldToQueryIsOfTypeDate = false;

        try {
            fieldToQueryType = clazz.getField(filter.getField()).getType();

            if (fieldIsDate(fieldToQueryType)) {
                fieldToQueryIsOfTypeDate = true;
            }

        } catch (NoSuchFieldException ex) {
            fieldToQueryType = null;
            logger.info("No such field , which isn't supposed to happen at this point");
        }

        Path<S> path = joinObject != null ? joinObject.get(filter.getField().split("[.]")[nestedFields.size() - 1])
                :
                root.get(filter.getField());

        switch (filter.getOperator()) {
            case EQUALS:
                predicate = cb.equal(path,filter.getValue());
                break;
            case LESS_THAN:
                predicate = cb.lessThan(path,filter.getValue());
                break;
            case GREATER_THAN:
                predicate = cb.greaterThan(path,filter.getValue());
                break;
            default:
                throw new RuntimeException("Wrong operator");

        }
        return predicate;
    }

    private static boolean fieldIsDate(Class fieldToQueryType) {
        return fieldToQueryType.equals(LocalDate.class)
                || fieldToQueryType.equals(LocalDateTime.class)
                || fieldToQueryType.equals(Date.class);
    }

    private static <T> Join<Object, Object> getJoinObject(Root<T> root, Join<Object, Object> joinObject, List<String> nestedFields ) {
        logger.info("Adding necessary join predicates ... ");
        int iteration = 1;
        Join<Object, Object> previous = null;
        for (String field : nestedFields.subList(0,nestedFields.size()-1)) {
            logger.info("Field {}",field);
            logger.info("Previous Join {}",previous != null ? previous.getJavaType().getName() : null);

            if (iteration == 1)
                joinObject = root.join(field);
            else
                joinObject = previous.join(field);

            previous = joinObject;
            iteration++;
        }
        logger.info("Returning Last Join as {}",joinObject.getJavaType().getName());
        return joinObject;
    }

    private static List<String> extractNestedFields(Filter filter){
        return Arrays.asList(filter.getField().split("[.]"));
    }

    private static boolean filterIsNested(Filter filter) {
        logger.info("Filter contains . {} ",filter.getField().contains("."));
        return filter.getField().contains(".");
    }

    public static Collection<Filter> extractCorrectFilters(Collection<Filter> filters, List<Field> declaredFields) {
        logger.info("Filters size {} ",filters.size());
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

                                String[] tokens = filter.split("[.]");
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
