/*
 * PurchaseService.java
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

import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.domain.Purchase;
import org.interactiverobotics.grocery.domain.Visit;
import org.interactiverobotics.grocery.exception.ItemNotFoundException;
import org.interactiverobotics.grocery.exception.PurchaseNotFoundException;
import org.interactiverobotics.grocery.exception.VisitNotFoundException;
import org.interactiverobotics.grocery.repository.ItemRepository;
import org.interactiverobotics.grocery.repository.PurchaseRepository;
import org.interactiverobotics.grocery.repository.VisitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Purchase service.
 */
@Service
public class PurchaseService {

    private static final Logger LOG = LoggerFactory.getLogger(PurchaseService.class);

    private final VisitRepository visitRepository;

    private final ItemRepository itemRepository;

    private final PurchaseRepository purchaseRepository;

    /**
     * Parametrized constructor.
     */
    @Autowired
    public PurchaseService(final VisitRepository visitRepository,
                           final ItemRepository itemRepository,
                           final PurchaseRepository purchaseRepository) {

        this.visitRepository = visitRepository;
        this.itemRepository = itemRepository;
        this.purchaseRepository = purchaseRepository;
    }

    /**
     * Returns Purchase(s).
     */
    public List<Purchase> getPurchases(final Long visitId) {
        final Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException(visitId));
        return getPurchases(visit);
    }

    /**
     * Returns Purchase(s).
     */
    public List<Purchase> getPurchases(final Visit visit) {
        final List<Purchase> purchases = purchaseRepository.findAllByVisit(visit);
        LOG.debug("{} Purchase(s) found for Visit {}", purchases.size(), visit);
        return purchases;
    }

    /**
     * Returns page of Purchase(s).
     */
    public Page<Purchase> getPurchases(Pageable pageable, final Long visitId) {
        final Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException(visitId));
        return getPurchases(pageable, visit);
    }

    /**
     * Returns page of Purchase(s).
     */
    public Page<Purchase> getPurchases(Pageable pageable, final Visit visit) {
        final Page<Purchase> purchases = purchaseRepository.findAllByVisit(pageable, visit);
        LOG.debug("{} Purchase(s) found for Visit {} and {}", purchases.getNumberOfElements(), visit, pageable);
        return purchases;
    }

    /**
     * Returns Item(s) not existing in Visit's Purchase(s).
     */
    public List<Item> getNotPurchasedItems(final Long visitId) {
        final Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException(visitId));
        return getNotPurchasedItems(visit);
    }

    /**
     * Returns Item(s) not existing in Visit's Purchase(s).
     */
    public List<Item> getNotPurchasedItems(final Visit visit) {
        final List<Item> items = StreamSupport.stream(itemRepository.findAll().spliterator(), false)
                .filter(item -> purchaseRepository.findOneByVisitAndItem(visit, item) == null)
                .collect(Collectors.toList());
        LOG.debug("{} not purchased Item(s) found for Visit {}", items.size(), visit);
        return items;
    }

    /**
     * Buy item by VisitId, ItemId.
     */
    public Purchase buyItem(final Long visitId, final Long itemId, final Long quantity, final BigDecimal price) {
        final Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException(visitId));
        final Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        return buyItem(visit, item, quantity, price);
    }

    /**
     * Buy item by Visit, Item.
     */
    public Purchase buyItem(final Visit visit, final Item item, final Long quantity, final BigDecimal price) {
        return buyItem(Optional.ofNullable(purchaseRepository.findOneByVisitAndItem(visit, item))
                .orElseGet(() -> new Purchase(visit, item)), quantity, price);
    }

    /**
     * Buy item.
     */
    public Purchase buyItem(final Purchase purchase, final Long quantity, final BigDecimal price) {

        // Quantity must be > 0
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0!");
        }
        // Price must be > 0 if not null
        if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be > 0!");
        }

        // Update Quantity
        final Long prevQuantity = purchase.getQuantity();
        purchase.setQuantity(prevQuantity + quantity);

        // Update Price as average
        if (price != null) {
            if (purchase.getPrice() == null) {
                purchase.setPrice(price);
            } else {
                // Price = average = (prevPrice * prevQuantity + price * quantity) / (prevQuantity + quantity)
                final BigDecimal prevPrice = purchase.getPrice();
                purchase.setPrice(
                        prevPrice.multiply(BigDecimal.valueOf(prevQuantity))
                                .add(price.multiply(BigDecimal.valueOf(quantity)))
                                .divide(BigDecimal.valueOf(prevQuantity + quantity)));
            }
        }

        final Purchase updatedPurchase = purchaseRepository.save(purchase);
        LOG.info("Purchase updated: {}", updatedPurchase);
        return updatedPurchase;
    }

    /**
     * Return item by VisitId, ItemId.
     */
    public Purchase returnItem(final Long visitId, final Long itemId, final Long quantity) {
        final Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException(visitId));
        final Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        return returnItem(visit, item, quantity);
    }

    /**
     * Return item by Visit, Item.
     */
    public Purchase returnItem(final Visit visit, final Item item, final Long quantity) {
        return returnItem(Optional.ofNullable(purchaseRepository.findOneByVisitAndItem(visit, item))
                .orElseThrow(() -> new PurchaseNotFoundException("Purchase not found!")), quantity);
    }

    /**
     * Return item.
     */
    public Purchase returnItem(final Purchase purchase, final Long quantity) {

        // Quantity must be > 0 and < Purchase.Quantity
        if (quantity == null || quantity <= 0 || quantity > purchase.getQuantity()) {
            throw new IllegalArgumentException("Quantity must be > 0 and < available!");
        }

        final Long newQuantity = purchase.getQuantity() - quantity;
        purchase.setQuantity(newQuantity);

        if (newQuantity > 0) {
            final Purchase updatedPurchase = purchaseRepository.save(purchase);
            LOG.info("Purchase updated: {}", updatedPurchase);
            return updatedPurchase;
        } else {
            // Delete empty purchase
            purchaseRepository.delete(purchase);
            LOG.info("Purchase deleted: {}", purchase);
            return null;
        }
    }

    /**
     * Updates Price by VisitId, ItemId.
     */
    public Purchase updatePrice(final Long visitId, final Long itemId, final BigDecimal price) {
        final Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException(visitId));
        final Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        return updatePrice(visit, item, price);
    }

    /**
     * Updates Price by Visit, Item.
     */
    public Purchase updatePrice(final Visit visit, final Item item, final BigDecimal price) {
        return updatePrice(Optional.ofNullable(purchaseRepository.findOneByVisitAndItem(visit, item))
                .orElseThrow(() -> new PurchaseNotFoundException("Purchase not found!")), price);
    }

    /**
     * Updates price.
     */
    public Purchase updatePrice(final Purchase purchase, final BigDecimal price) {

        // Price must be > 0 if not null
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be > 0!");
        }

        purchase.setPrice(price);

        final Purchase updatedPurchase = purchaseRepository.save(purchase);
        LOG.info("Purchase updated: {}", updatedPurchase);
        return updatedPurchase;
    }
}
