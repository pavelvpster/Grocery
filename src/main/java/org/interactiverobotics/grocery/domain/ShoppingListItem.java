/*
 * ShoppingListItem.java
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

package org.interactiverobotics.grocery.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * ShoppingListItem domain class.
 */
@Entity
@Table(schema = "grocery", name = "shopping_list_items")
public class ShoppingListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shopping_list_item_id_seq")
    @SequenceGenerator(name = "shopping_list_item_id_seq", sequenceName = "shopping_list_item_id_seq",
            allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_list_id")
    private ShoppingList shoppingList;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column
    private Long quantity;

    public ShoppingListItem() {
    }

    /**
     * Parametrized constructor.
     */
    public ShoppingListItem(final ShoppingList shoppingList, final Item item, final Long quantity) {
        this.shoppingList = shoppingList;
        this.item = item;
        this.quantity = quantity;
    }

    /**
     * Parametrized constructor.
     */
    public ShoppingListItem(final Long id, final ShoppingList shoppingList, final Item item, final Long quantity) {
        this.id = id;
        this.shoppingList = shoppingList;
        this.item = item;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ShoppingList getShoppingList() {
        return shoppingList;
    }

    public void setShoppingList(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object object) {
        return EqualsBuilder.reflectionEquals(this, object);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
