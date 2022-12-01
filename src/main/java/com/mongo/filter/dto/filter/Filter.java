package com.mongo.filter.dto.filter;


public class Filter {
    private String field;
    private String value;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public InternalOperator getOperator() {
        return operator;
    }

    public void setOperator(InternalOperator operator) {
        this.operator = operator;
    }
}