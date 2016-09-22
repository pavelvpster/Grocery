/*
 * ShopService.java
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

package org.interactiverobotics.grocery.service;

import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.exception.ShopNotFoundException;
import org.interactiverobotics.grocery.form.ShopForm;
import org.interactiverobotics.grocery.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Shop service.
 */
@Service
public class ShopService {

    private final ShopRepository shopRepository;

    @Autowired
    public ShopService(final ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    /**
     * Returns Shop(s).
     */
    public List<Shop> getShops() {
        final List<Shop> shops = new ArrayList<>();
        this.shopRepository.findAll().forEach(shop -> shops.add(shop));
        return shops;
    }

    /**
     * Returns page of Shop(s).
     */
    public Page<Shop> getShops(Pageable pageable) {
        return this.shopRepository.findAll(pageable);
    }

    /**
     * Returns Shop by Id.
     */
    public Shop getShopById(final Long shopId) {
        return Optional.ofNullable(this.shopRepository.findOne(shopId))
                .orElseThrow(() -> new ShopNotFoundException(shopId));
    }

    /**
     * Returns Shop by Name.
     */
    public Shop getShopByName(final String name) {
        return Optional.ofNullable(this.shopRepository.findOneByName(name))
                .orElseThrow(() -> new ShopNotFoundException(-1L));
    }

    /**
     * Creates Shop.
     */
    public Shop createShop(final ShopForm form) {
        return this.shopRepository.save(new Shop(form.getName()));
    }

    /**
     * Updates Shop.
     */
    public Shop updateShop(final Long shopId, final ShopForm form) {
        final Shop shop = Optional.ofNullable(this.shopRepository.findOne(shopId))
                .orElseThrow(() -> new ShopNotFoundException(shopId));
        shop.setName(form.getName());
        return this.shopRepository.save(shop);
    }

    /**
     * Deletes Shop.
     */
    public void deleteShop(final Long shopId) {
        final Shop shop = Optional.ofNullable(this.shopRepository.findOne(shopId))
                .orElseThrow(() -> new ShopNotFoundException(shopId));
        this.shopRepository.delete(shop);
    }

}
