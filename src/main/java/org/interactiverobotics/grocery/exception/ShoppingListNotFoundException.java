package org.interactiverobotics.grocery.exception;

/**
 * ShoppingList not found.
 */
public class ShoppingListNotFoundException extends RuntimeException {

    public ShoppingListNotFoundException(final Long id) {
        super("Shopping list #" + id + " not found!");
    }

}
