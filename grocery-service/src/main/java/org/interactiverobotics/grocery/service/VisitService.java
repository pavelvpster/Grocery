/*
 * VisitService.java
 *
 * Copyright (C) 2016-2022 Pavel Prokhorov (pavelvpster@gmail.com)
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

import lombok.AllArgsConstructor;
import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.domain.Visit;
import org.interactiverobotics.grocery.exception.ShopNotFoundException;
import org.interactiverobotics.grocery.exception.VisitNotFoundException;
import org.interactiverobotics.grocery.repository.ShopRepository;
import org.interactiverobotics.grocery.repository.VisitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Visit service.
 */
@AllArgsConstructor
@Service
public class VisitService {

    private static final Logger LOG = LoggerFactory.getLogger(VisitService.class);

    private final VisitRepository visitRepository;

    private final ShopRepository shopRepository;

    /**
     * Return Visit(s).
     */
    public List<Visit> getVisits() {
        List<Visit> visits = new ArrayList<>();
        visitRepository.findAll().forEach(visit -> visits.add(visit));
        LOG.debug("{} Visit(s) found", visits.size());
        return visits;
    }

    /**
     * Returns page of Visit(s).
     */
    public Page<Visit> getVisits(Pageable pageable) {
        Page<Visit> visits = visitRepository.findAll(pageable);
        LOG.debug("{} Visit(s) found for {}", visits.getNumberOfElements(), pageable);
        return visits;
    }

    /**
     * Returns Visit by Id.
     */
    public Visit getVisitById(Long visitId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException(visitId));
        LOG.debug("Visit found by Id #{}", visitId);
        return visit;
    }

    /**
     * Returns Visit(s) by Shop Id.
     */
    public List<Visit> getVisitsByShopId(Long shopId) {
        return getVisitsByShop(shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopNotFoundException(shopId)));
    }

    /**
     * Returns Visit(s) by Shop Name.
     */
    public List<Visit> getVisitsByShopName(String shopName) {
        return getVisitsByShop(Optional.ofNullable(shopRepository.findOneByName(shopName))
                .orElseThrow(() -> new ShopNotFoundException(-1L)));
    }

    /**
     * Returns Visit(s) by Shop.
     */
    public List<Visit> getVisitsByShop(Shop shop) {
        List<Visit> visits = new ArrayList<>();
        visitRepository.findAllByShop(shop).forEach(visit -> visits.add(visit));
        LOG.debug("{} Visit(s) found for Shop {}", visits.size(), shop);
        return visits;
    }

    /**
     * Creates Visit.
     */
    public Visit createVisit(Long shopId) {
        return createVisit(shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopNotFoundException(shopId)));
    }

    /**
     * Creates Visit.
     */
    public Visit createVisit(String shopName) {
        return createVisit(Optional.ofNullable(shopRepository.findOneByName(shopName))
                .orElseThrow(() -> new ShopNotFoundException(-1L)));
    }

    /**
     * Creates Visit.
     */
    public Visit createVisit(Shop shop) {
        Visit visit = visitRepository.save(Visit.builder().shop(shop).build());
        LOG.info("Visit created: {}", visit);
        return visit;
    }

    /**
     * Starts Visit.
     */
    public Visit startVisit(Long visitId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException(visitId));
        visit.setStarted(new Date());
        Visit updatedVisit = visitRepository.save(visit);
        LOG.info("Visit started: {}", updatedVisit);
        return updatedVisit;
    }

    /**
     * Completes Visit.
     */
    public Visit completeVisit(Long visitId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException(visitId));
        Date now = new Date();
        if (visit.getStarted() == null) {
            visit.setStarted(now);
        }
        visit.setCompleted(now);
        Visit updatedVisit = visitRepository.save(visit);
        LOG.info("Visit completed: {}", updatedVisit);
        return updatedVisit;
    }

    /**
     * Deletes Visit.
     */
    public void deleteVisit(Long visitId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException(visitId));
        this.visitRepository.delete(visit);
        LOG.info("Visit deleted: {}", visit);
    }
}
