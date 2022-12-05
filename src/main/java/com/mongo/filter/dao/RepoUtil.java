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
    private static final Logger logger = LoggerFactory.getLogger(RepoUtil.class);

    private RepoUtil() {
        // Empty Constructor to avoid instantiation
    }

    public static boolean isHierarchyPresent(String[] tokens, int i, List<Field> fields) throws ClassNotFoundException {
        if (i >= tokens.length) {
            return true;
        }
        for (Field field : fields) {
            String genericTypeClassName = null;

            if (field.getGenericType().getTypeName().contains("java.util.List")) {
                genericTypeClassName = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0].getTypeName();
            }

            logger.info("field {} == {}", field.getName(), tokens[i]);
            if (tokens[i].equals(field.getName())) {
                if (genericTypeClassName != null) { // meaning it's a list
                    logger.info("Das ist list of type {} ", genericTypeClassName);
                    return isHierarchyPresent(tokens, ++i, Arrays.stream(Class.forName(genericTypeClassName).getDeclaredFields()).collect(Collectors.toList()));
                } else {
                    return isHierarchyPresent(tokens, ++i, Arrays.stream(field.getType().getDeclaredFields()).collect(Collectors.toList()));
                }

            }
        }

        return false;
    }

    public static CriteriaDefinition extractCriteria(Filter filter) {
        CriteriaDefinition criteriaDefinition;

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

    public static <T> Predicate extractCriteria(Filter filter, CriteriaBuilder cb, Root<T> root, Class clazz) {
        logger.info("Extracting criteria ... ");

        Join<Object, Object> joinObject = null;
        List<String> nestedFields = extractNestedFields(filter);
        logger.info("{} nested fields", nestedFields.size());

        if (filterIsNested(filter)) {
            // Add Neccessary Joins
            logger.info("Join java type ");
            joinObject = getJoinObject(root, joinObject, nestedFields);
            logger.info("Join java type {} ", joinObject.getJavaType());
            clazz = joinObject.getJavaType();
        }

        Path<Double> doublePath;
        Path<String> stringPath;
        Path<LocalDate> datePath;
        Path<LocalDateTime> ldateTimePath;

        Double doubleValue;
        String stringValue;
        LocalDate lDateValue;
        LocalDateTime lDateTimeValue;

        Predicate predicate = null;
        try {
            switch (filter.getType()) {
                case NUMERIC:
                    doublePath = joinObject != null ? joinObject.get(filter.getField().split("[.]")[nestedFields.size() - 1]) : root.get(filter.getField());
                    doubleValue = Double.parseDouble(filter.getValue());
                    switch (filter.getOperator()) {
                        case LESS_THAN:
                            predicate = cb.lessThan(doublePath, doubleValue);
                            break;
                        case GREATER_THAN:
                            predicate = cb.greaterThan(doublePath, doubleValue);
                            break;

                        case EQUALS:
                        default:
                            predicate = cb.equal(doublePath, doubleValue);

                    }
                    break;
                case STRING:
                    stringPath = joinObject != null ? joinObject.get(filter.getField().split("[.]")[nestedFields.size() - 1]) : root.get(filter.getField());
                    stringValue = filter.getValue();

                    switch (filter.getOperator()) {

                        case LESS_THAN:
                            predicate = cb.lessThan(stringPath, stringValue);
                            break;

                        case GREATER_THAN:
                            predicate = cb.greaterThan(stringPath, stringValue);
                            break;

                        case EQUALS:
                        default:
                            predicate = cb.equal(stringPath, stringValue);

                    }

                    break;
                case LOCAL_DATE:
                    datePath = joinObject != null ? joinObject.get(filter.getField().split("[.]")[nestedFields.size() - 1]) : root.get(filter.getField());
                    lDateValue = LocalDate.parse(filter.getValue());

                    switch (filter.getOperator()) {
                        case LESS_THAN:
                            predicate = cb.lessThan(datePath, lDateValue);
                            break;
                        case GREATER_THAN:
                            predicate = cb.greaterThan(datePath, lDateValue);
                            break;

                        case EQUALS:
                        default:
                            predicate = cb.equal(datePath, lDateValue);
                            break;

                    }

                    break;
                case LOCAL_DATE_TIME:
                    ldateTimePath = joinObject != null ? joinObject.get(filter.getField().split("[.]")[nestedFields.size() - 1]) : root.get(filter.getField());
                    lDateTimeValue = LocalDateTime.parse(filter.getValue());

                    switch (filter.getOperator()) {
                        case LESS_THAN:
                            predicate = cb.lessThan(ldateTimePath, lDateTimeValue);
                            break;
                        case GREATER_THAN:
                            predicate = cb.greaterThan(ldateTimePath, lDateTimeValue);
                            break;

                        case EQUALS:
                        default:
                            predicate = cb.equal(ldateTimePath, lDateTimeValue);
                    }

                    break;

            }
            return predicate;
        } catch (Exception e) {
            logger.error("Caught exception message {} ", e.getMessage());
            throw new RuntimeException("Error with one of the datatypes / operators provided ");
        }

    }


    private static <T> Join<Object, Object> getJoinObject(Root<T> root, Join<Object, Object> joinObject, List<String> nestedFields) {
        logger.info("Adding necessary join predicates ... ");
        int iteration = 1;
        Join<Object, Object> previous = null;
        for (String field : nestedFields.subList(0, nestedFields.size() - 1)) {
            logger.info("Field {}", field);
            logger.info("Previous Join {}", previous != null ? previous.getJavaType().getName() : null);

            if (iteration == 1) joinObject = root.join(field);
            else joinObject = previous.join(field);

            previous = joinObject;
            iteration++;
        }
        logger.info("Returning Last Join as {}", joinObject.getJavaType().getName());
        return joinObject;
    }

    private static List<String> extractNestedFields(Filter filter) {
        return Arrays.asList(filter.getField().split("[.]"));
    }

    private static boolean filterIsNested(Filter filter) {
        logger.info("Filter contains . {} ", filter.getField().contains("."));
        return filter.getField().contains(".");
    }

    public static Collection<Filter> extractCorrectFilters(Collection<Filter> filters, List<Field> declaredFields) {
        logger.info("Filters size {} ", filters.size());
        Map<String, Filter> filterMap = new HashMap<>();
        filters.forEach(filter -> filterMap.put(filter.getField(), filter));

        if (filters.isEmpty() && declaredFields.isEmpty()) {
            return filterMap.values();
        }

        filters.stream().map(Filter::getField).forEach(filter -> {
            if (filter.contains(".")) {

                String[] tokens = filter.split("[.]");
                try {
                    if (!isHierarchyPresent(tokens, 0, declaredFields)) {
                        filterMap.remove(filter);
                        throw new IllegalArgumentException("This filter does not exist");
                    }
                } catch (ClassNotFoundException e) {
                    // Do nothing
                }

            } else {
                if (!declaredFields.stream().map(Field::getName).collect(Collectors.toList()).contains(filter)) {
                    filterMap.remove(filter);
                }
            }
        });
        return filterMap.values();
    }

}
