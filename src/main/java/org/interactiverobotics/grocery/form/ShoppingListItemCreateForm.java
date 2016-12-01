/*
 * ShoppingListItemCreateForm.java
 *
 * Copyright (C) 2016 Pavel Prokhorov (pavelvpster@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.interactiverobotics.grocery.form;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Form to create ShoppingListItem.
 */
public class ShoppingListItemCreateForm {

    @NotNull
    private Long shoppingList;
    @NotNull
    private Long item;
    @NotNull
    @Min(1)
    private Long quantity;

    public ShoppingListItemCreateForm() {
    }

    /**
     * Parametrized constructor.
     */
    public ShoppingListItemCreateForm(final Long shoppingList, final Long item, final Long quantity) {
        this.shoppingList = shoppingList;
        this.item = item;
        this.quantity = quantity;
    }

    public Long getShoppingList() {
        return shoppingList;
    }

    public void setShoppingList(final Long shoppingList) {
        this.shoppingList = shoppingList;
    }

    public Long getItem() {
        return item;
    }

    public void setItem(final Long item) {
        this.item = item;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(final Long quantity) {
        this.quantity = quantity;
    }
}
