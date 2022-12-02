package com.mongo.filter.dto.filter;


public class Filter<T extends Comparable> {
    private String field;
    private T value;
    private InternalOperator operator;

    public Filter() {
       // EMPTY
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public InternalOperator getOperator() {
        return operator;
    }

    public void setOperator(InternalOperator operator) {
        this.operator = operator;
    }
}
