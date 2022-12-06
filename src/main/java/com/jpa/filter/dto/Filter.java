package com.jpa.filter.dto;


/**
 * Filter Object
 * <ul>
 *     <li> field - The field to be queried. It must be only the name if the field is in the first level entity,
 *     or if the field is inside a number of arbitrarily nested objects
 *     than the required field should be accessed using a dot (.). </li>
 *     <li> value - The value the field must be queried with </li>
 *     <li> operator - EQUALS , GREATER_THAN , LESS_THAN </li>
 *     <li> type - String , Numeric , LocalDate , LocalDateTime </li>
 * </ul>
 */
public class Filter {
    private String field;
    private String value;
    private InternalOperator operator;

    private ValueType type;


    public Filter() {
        // EMPTY
    }

    public ValueType getType() {
        return type;
    }

    public void setType(ValueType type) {
        this.type = type;
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
