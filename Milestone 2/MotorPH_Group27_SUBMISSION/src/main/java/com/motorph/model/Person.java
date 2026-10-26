package com.motorph.model;

/** Abstract base class (Abstraction + Inheritance). */
public abstract class Person {
    private final String firstName;
    private final String lastName;

    protected Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }

    /** Each subclass describes its own role (Polymorphism). */
    public abstract String describe();
}
