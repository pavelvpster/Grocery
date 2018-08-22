/*
 * ShopService.java
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

package org.interactiverobotics.grocery.service;

import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.exception.ShopNotFoundException;
import org.interactiverobotics.grocery.form.ShopForm;
import org.interactiverobotics.grocery.repository.ShopRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(ShopService.class);

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
        shopRepository.findAll().forEach(shop -> shops.add(shop));
        LOG.debug("{} Shop(s) found", shops.size());
        return shops;
    }

    /**
     * Returns page of Shop(s).
     */
    public Page<Shop> getShops(Pageable pageable) {
        final Page<Shop> shops = shopRepository.findAll(pageable);
        LOG.debug("{} Shop(s) found for {}", shops.getNumberOfElements(), pageable);
        return shops;
    }

    /**
     * Returns Shop by Id.
     */
    public Shop getShopById(final Long shopId) {
        final Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopNotFoundException(shopId));
        LOG.debug("Shop found by Id #{}", shopId);
        return shop;
    }

    /**
     * Returns Shop by Name.
     */
    public Shop getShopByName(final String name) {
        final Shop shop = Optional.ofNullable(shopRepository.findOneByName(name))
                .orElseThrow(() -> new ShopNotFoundException(-1L));
        LOG.debug("Shop found by Name '{}'", name);
        return shop;
    }

    /**
     * Creates Shop.
     */
    public Shop createShop(final ShopForm form) {
        final Shop shop = shopRepository.save(new Shop(form.getName()));
        LOG.info("Shop created: {}", shop);
        return shop;
    }

    /**
     * Updates Shop.
     */
    public Shop updateShop(final Long shopId, final ShopForm form) {
        final Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopNotFoundException(shopId));
        shop.setName(form.getName());
        final Shop updatedShop = shopRepository.save(shop);
        LOG.info("Shop updated: {}", updatedShop);
        return updatedShop;
    }

    /**
     * Deletes Shop.
     */
    public void deleteShop(final Long shopId) {
        final Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopNotFoundException(shopId));
        this.shopRepository.delete(shop);
        LOG.info("Shop deleted: {}", shop);
    }
}
