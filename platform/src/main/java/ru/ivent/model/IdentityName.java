package ru.ivent.model;

import lombok.Value;

/**
 * @author Laughina
 */
@Value
public class IdentityName {

    String firstName;
    String lastName;

    @Override
    public String toString() {
        return lastName != null
                ? firstName + " " + lastName
                : firstName;
    }
}
