package org.interactiverobotics.grocery.form;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Form to create/update ShoppingList.
 */
public class ShoppingListForm {

    @NotNull
    @Size(min = 1, max = 255)
    private String name;

    public ShoppingListForm() {
    }

    public ShoppingListForm(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
