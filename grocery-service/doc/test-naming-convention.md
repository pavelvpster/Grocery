# Test naming convention

## Getter without arguments
```java
public class Foo {
    public List<Object> getObjects() {}
}

public class FooTest {
    @Test
    public void getObjects_returnsObjects() {}
}
```

## Getter with arguments
```java
public class Foo {
    public Object getObjectById(long id) {}
}

public class FooTest {
    @Test
    public void getObjectsById_returnsObject() {}

    @Test
    public void getObjectsById_whenObjectDoesNotExist_throwsException() {}
}
```

```java
public class Foo {
    public Object getObject(long id) {}
    
    public Object getObject(String name) {}
}

public class FooTest {
    @Test
    public void getObjects_givenId_returnsObject() {}

    @Test
    public void getObjects_givenId_whenObjectDoesNotExist_returnsObject() {}

    @Test
    public void getObjects_givenName_throwsException() {}

    @Test
    public void getObjects_givenName_whenObjectDoesNotExist_throwsException() {}
}
```

## Create/Update/Delete
```java
public class Foo {
    public Object createObject(long value) {}
    
    public Object updateObject(long id, long value) {}
    
    public void deleteObject(long id) {}
}

public class FooTest {
    @Test
    public void createObject_createsAndReturnsObject() {}

    @Test
    public void createObject_whenValueIsWrong_throwsException() {}

    @Test
    public void updateObject_updatesAndReturnsObject() {}

    @Test
    public void updateObject_whenObjectDoesNotExist_throwsException() {}

    @Test
    public void updateObject_whenValueIsWrong_throwsException() {}

    @Test
    public void deleteObject_deletesObject() {}

    @Test
    public void deleteObject_whenObjectDoesNotExist_throwsException() {}
}
```
