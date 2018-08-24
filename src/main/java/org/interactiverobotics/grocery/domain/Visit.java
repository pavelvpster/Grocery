/*
 * Visit.java
 *
 * Copyright (C) 2016-2018 Pavel Prokhorov (pavelvpster@gmail.com)
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

import com.fasterxml.jackson.annotation.JsonFilter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

/**
 * Visit domain class.
 */
@Entity
@Table(schema = "grocery", name = "visits")
@JsonFilter("jpaFilter")
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "visit_id_seq")
    @SequenceGenerator(name = "visit_id_seq", sequenceName = "visit_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date started;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date completed;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_list_id")
    private ShoppingList shoppingList;

    @Transient
    @OneToMany(mappedBy = "visit", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Purchase> purchases;

    public Visit() {
    }

    public Visit(final Shop shop) {
        this.shop = shop;
    }

    public Visit(final Long id, final Shop shop) {
        this.id = id;
        this.shop = shop;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Date getCompleted() {
        return completed;
    }

    public void setCompleted(Date completed) {
        this.completed = completed;
    }

    public ShoppingList getShoppingList() {
        return shoppingList;
    }

    public void setShoppingList(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }

    public void setPurchases(List<Purchase> purchases) {
        this.purchases = purchases;
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
