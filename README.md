# Generic Multi-Purpose Filter

## Aim
The aim of this repository is to make the life of developers using filters easier.
By integrating this JAR into their project, you have an off-the-shelf filter
which you can plug in your source code and use it immediately.
It relies heaviliy in Java Reflection API to recursively search for
fields in @Document annotated classes. By recursively, it means even fields
in nested documents ex. book.author.location.name

## Requirements
- Java 11
- Spring Boot 
- PreConfigured Spring Data Mongo Configuration

## How to use ?

At the moment the project isn't hosted in any Maven Central,
so you have to download
the Jar and add it manually in your project.
The FilterRepo interface provides a filter method requiring a 
FilterWrap object and the Class object where the filter will search.
This can be wrapped in a service where the Class of the Document can be provided or can be 
used directly in the controller.
The FilterWrap class contains a List of Filter objects which represent
the field to be filter 
ex. `[ {field = author.name , value=Test , operator=InternalOperator.EQUALS} , {field = year , value=2001 , operator=InternalOperator.LESS_THAN} ]`

We can easily draw the conclusion that the "field" variable holds the name of the field 
in the @Document POJO to be searched, the "value" variable holds the value to be searched and operator
is an enum , which has 3 values: EQUALS, GREATER_THAN , LESS_THAN.

To use the Filter inject the FilterRepo interface in your service/controller using the desired method of dependency injection
in Spring and use it as you wish.




