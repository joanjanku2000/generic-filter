package com.jpa.filter.helper;

public class ExceptionMessages {

    private ExceptionMessages(){
        // not initializable
    }

    public static String VALUE_IS_NOT_LOCAL_DATE = "Provided value isn't of type LocalDateTime, it should comply with: YYYY-MM-DD ";
    public static String VALUE_IS_NOT_LOCAL_DATE_TIME = "Provided value isn't of type LocalDateTime, it should comply with: YYYY-MM-DD T hh:mm:ss";
    public static String VALUE_IS_NOT_DOUBLE = "Provided value isn't of type Double";

}
