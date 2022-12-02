package com.mongo.filter.aop;

import com.mongo.filter.dto.filter.Filter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Component
@Aspect
public class FilterAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Before("@within(com.mongo.filter.aop.GenericFilter)")
    public void deserializeJsonBody(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] arguments = joinPoint.getArgs();
        if (arguments.length > 1){
            throw new IllegalArgumentException("Not allowed to have more than one argument");
        }

        Filter filter = (Filter) arguments[0];

        String value = (String) filter.getValue();
        log.info("Got value from request {}",value);
        try {
            Integer intVal = Integer.parseInt(value);

            Filter<Integer> newFilter = new Filter<>();
            newFilter.setOperator(filter.getOperator());
            newFilter.setField(filter.getField());
            newFilter.setValue(intVal);

            joinPoint.proceed(new Object[]{newFilter});

            Double doubleVal = Double.parseDouble(value);

            Filter<Integer> newDoubleFilter = new Filter<>();
            newFilter.setOperator(filter.getOperator());
            newFilter.setField(filter.getField());
            newFilter.setValue(intVal);

            joinPoint.proceed(new Object[]{newDoubleFilter});

        } catch (NumberFormatException e){
            log.info("It isnt number");
        }

        try {
            LocalDate ldateVal = LocalDate.parse(value);

            Filter<LocalDate> newFilter = new Filter<>();
            newFilter.setOperator(filter.getOperator());
            newFilter.setField(filter.getField());
            newFilter.setValue(ldateVal);

            joinPoint.proceed(new Object[]{newFilter});

        } catch (DateTimeParseException e){
            log.info("It isnt string");
        }

        try {
            LocalDateTime ldateTimeVal = LocalDateTime.parse(value);

            Filter<LocalDateTime> newFilter = new Filter<>();
            newFilter.setOperator(filter.getOperator());
            newFilter.setField(filter.getField());
            newFilter.setValue(ldateTimeVal);

            joinPoint.proceed(new Object[]{newFilter});

        } catch (DateTimeParseException e){
            log.info("It isnt string");
        }

        try {
            String strVal = value;

            Filter<String> newFilter = new Filter<>();
            newFilter.setOperator(filter.getOperator());
            newFilter.setField(filter.getField());
            newFilter.setValue(strVal);

            joinPoint.proceed(new Object[]{newFilter});

        } catch (Exception e){
            log.info("It isnt string");
        }
        joinPoint.proceed(new Object[]{filter});
    }
}
