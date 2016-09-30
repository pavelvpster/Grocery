package org.interactiverobotics.grocery.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.List;

/**
 * ShoppingList domain class.
 */
@Entity
@Table(schema = "grocery", name = "shopping_lists")
public class ShoppingList {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shopping_list_id_seq")
    @SequenceGenerator(name = "shopping_list_id_seq", sequenceName = "shopping_list_id_seq", allocationSize = 1)
    private Long id;

    @Column
    private String name;

    @Transient
    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Item> items;

    public ShoppingList() {
    }

    public ShoppingList(final String name) {
        this.name = name;
    }

    public ShoppingList(final Long id, final String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
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
