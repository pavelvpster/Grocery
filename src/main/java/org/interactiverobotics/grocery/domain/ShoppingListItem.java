package org.interactiverobotics.grocery.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

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

    public ShoppingListItem(final ShoppingList shoppingList, final Long quantity) {
        this.shoppingList = shoppingList;
        this.quantity = quantity;
    }

    public ShoppingListItem(final Long id, final ShoppingList shoppingList, final Long quantity) {
        this.id = id;
        this.shoppingList = shoppingList;
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
