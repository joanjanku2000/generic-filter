# Generic Multi-Purpose Filter

## Aim
The aim of this repository is to make the life of developers using filters easier.
By integrating this JAR into their project, you have an off-the-shelf filter
which you can plug in your source code and use it immediately.
It relies heaviliy in Java Reflection API to recursively search for
fields in @ENTITY annotated classes. By recursively, it means even fields
in nested ENTITYs ex. book.author.location.name

## Requirements
- Java 11
- Spring Boot 
- PreConfigured Spring Data JPA

## How to use ?

At the moment the project isn't hosted in any Maven Central,
so you have to download
the Jar and add it manually in your project or run the code and install it 
in your local maven.
The FilterRepo interface provides a filter method requiring a 
FilterWrap object and the Class object where the filter will search.
This can be wrapped in a service where the Class of the ENTITY can be provided or can be 
used directly in the controller.
The FilterWrap class contains a List of Filter objects which represent
the field to be filter 
ex. `[ {field = author.name , value=Test , operator=InternalOperator.EQUALS} , type = ValueType.STRING , {field = year , value=2001 , operator=InternalOperator.LESS_THAN} , type = ValueType.NUMERIC ]`

We can easily draw the conclusion that the "field" variable holds the name of the field 
in the @Entity annotated POJO to be searched, the "value" variable holds the value to be searched and operator
is an enum , which has 3 values: EQUALS, GREATER_THAN , LESS_THAN , while the type represents an enum used to
indicate the data type of the value to be searched. Currently , ValueType value is one of the following 
{ 0 = STRING , 1 = NUMERIC , 2 = LOCAL_DATE , 3 = LOCAL_DATE_TIME }. The values fed into the value node , must be
in any of the above formats otherwise filter will not work. When being called through the API the numeric values
of enum will be fed into the "type" node.

To use the Filter inject the FilterRepo interface in your service/controller using the desired method of dependency injection
in Spring and use it as you wish, after first creating a bean of type FilterRepo inside a Configuration class.

**Simple Usage Example:**
```
@Bean
public FilterRepo filterRepo(){
    return new FilterRepoJpaImpl();
}
```
Service Layer:

```
// class declaration , fields , etc ...
@Autowired
protected final FilterRepo filterRepo;
// other methods

 @Override
 public List<UserDto> findAll(FilterWrap filterWrap, Class<UserEntity> clazz) {
    return filterRepo.filter(filterWrap,clazz).stream().map(baseConverter::toDto).collect(Collectors.toList());
 }

```

## Postman example
```
{
"filters": [
{
"field":"name" ,
"value":"Algebra",
"type": 0  ,
"operator":0
}
] ,
"collection": "test"
}
```

(c) Joan Janku 2022



