package com.jpa.filter.dao;

import com.jpa.filter.dto.Filter;
import com.jpa.filter.helper.ExceptionMessages;
import com.jpa.filter.helper.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.Chronology;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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

    /**
     * Method used to extract the Predicate from a single Filter , including the validations
     * What it does ?
     * <ol>
     *     <li>Checks if the path even exists. Recursively.</li>
     *     <li>Validates if the provided data type corresponds with the field's datatype</li>
     *     <li>Extracts the predicate for the queried request</li>
     * </ol>
     * <p>
     *  At the moment only 4 data types are supported: Numeric i.e Double , String , LocalDate , LocalDateTime
     *
     * @param filter {@link Filter}
     * @param cb     {@link CriteriaBuilder}
     * @param root   {@link Root}
     * @return {@link Predicate}
     */
    public static <T> Predicate extractCriteria(Filter filter, CriteriaBuilder cb, Root<T> root) {
        logger.info("Extracting criteria ... ");

        Join<Object, Object> joinObject = null;
        List<String> nestedFields = extractNestedFields(filter);
        logger.info("{} nested fields", nestedFields.size());

        if (filterIsNested(filter)) {
            // Add Neccessary Joins
            logger.info("Join java type ");
            joinObject = getJoinObject(root, joinObject, nestedFields);
            logger.info("Join java type {} ", joinObject.getJavaType());
        }

        Path<Double> doublePath;
        Path<String> stringPath;
        Path<ChronoLocalDate> datePath;
        Path<ChronoLocalDateTime> ldateTimePath;

        double doubleValue;
        String stringValue;
        AtomicReference<ChronoLocalDate> lDateValue = new AtomicReference<>();
        AtomicReference<ChronoLocalDateTime> lDateTimeValue = new AtomicReference<>();


        Predicate predicate = null;
        switch (filter.getType()) {
            case NUMERIC:
                doublePath = joinObject != null ? joinObject.get(filter.getField().split("[.]")[nestedFields.size() - 1]) : root.get(filter.getField());

                doubleValue =  execute(() ->
                                Double.parseDouble(filter.getValue())
                                , ExceptionMessages.VALUE_IS_NOT_DOUBLE);

                if (!doublePath.getJavaType().equals(Double.class)) {
                    throw new IllegalArgumentException("Field " + filter.getField() + " should be of type Double , found " + doublePath.getJavaType());
                }

                predicate = getPredicate(filter, cb, doublePath, doubleValue);
                break;
            case STRING:
                stringPath = joinObject != null ? joinObject.get(filter.getField().split("[.]")[nestedFields.size() - 1]) : root.get(filter.getField());
                stringValue = filter.getValue();

                if (!stringPath.getJavaType().equals(String.class)) {
                    throw new IllegalArgumentException("Field " + filter.getField() + " should be of type String , found " + stringPath.getJavaType().getSimpleName());
                }

                predicate = getPredicate(filter, cb, stringPath, stringValue);
                break;
            case LOCAL_DATE:

                datePath = joinObject != null ? joinObject.get(filter.getField().split("[.]")[nestedFields.size() - 1]) : root.get(filter.getField());

                if (!datePath.getJavaType().equals(LocalDate.class)) {
                    throw new IllegalArgumentException("Field " + filter.getField() + " should be of type LocalDate , found " + datePath.getJavaType());
                }

                lDateValue.set(
                        execute(() -> {
                            Chronology chronology = Chronology.ofLocale(Locale.ENGLISH);
                            return chronology.date(LocalDate.parse(filter.getValue()));
                            }
                            , ExceptionMessages.VALUE_IS_NOT_LOCAL_DATE)
                );

                predicate = getPredicate(filter, cb, datePath, lDateValue.get());
                break;
            case LOCAL_DATE_TIME:
                ldateTimePath = joinObject != null ? joinObject.get(filter.getField().split("[.]")[nestedFields.size() - 1]) : root.get(filter.getField());

                if (!ldateTimePath.getJavaType().equals(LocalDateTime.class)) {
                    throw new IllegalArgumentException("Field " + filter.getField() + " should be of type LocalDateTime");
                }

                lDateTimeValue.set(
                        execute(() -> {
                            Chronology chronology = Chronology.ofLocale(Locale.ENGLISH);
                            return chronology.localDateTime(LocalDateTime.parse(filter.getValue()));
                            }
                            , ExceptionMessages.VALUE_IS_NOT_LOCAL_DATE_TIME)
                );

                predicate = getPredicate(filter, cb, ldateTimePath,  lDateTimeValue.get());
        }

        return predicate;

    }

    private static <T> T execute(Executable executable, String message) {
        try {
            return (T) executable.execute();
        } catch (Exception var3) {
            throw new IllegalArgumentException(message);
        }
    }


    private static <T extends Comparable<T>> Predicate getPredicate(Filter filter,
                                                                    CriteriaBuilder cb,
                                                                    Path<T> doublePath,
                                                                    T doubleValue) {
        Predicate predicate;
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
        return predicate;
    }


    private static <T> Join<Object, Object> getJoinObject(Root<T> root,
                                                          Join<Object, Object> joinObject,
                                                          List<String> nestedFields) {

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

    public static Collection<Filter> extractCorrectFilters(Collection<Filter> filters,
                                                           List<Field> declaredFields) {

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
