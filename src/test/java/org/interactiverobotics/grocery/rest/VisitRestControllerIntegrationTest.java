/*
 * VisitRestControllerIntegrationTest.java
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

package org.interactiverobotics.grocery.rest;

import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.domain.Visit;
import org.interactiverobotics.grocery.repository.ShopRepository;
import org.interactiverobotics.grocery.repository.VisitRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Visit REST controller integration test.
 * Performs queries to running instance of the application.
 * Requires database access.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VisitRestControllerIntegrationTest {

    private static final String VISIT_ENDPOINT = "/api/v1/visit/";
    private static final String VISIT_SHOP_ENDPOINT = "/api/v1/visit/shop/";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private VisitRepository visitRepository;

    private Shop shop;


    @BeforeEach
    public void setUp() {
        visitRepository.deleteAll();
        shop = shopRepository.save(Shop.builder().name("test-shop").build());
    }

    @AfterEach
    public void tearDown() {
        shopRepository.delete(shop);
    }


    @Test
    public void getVisits_returnsVisits() {
        List<Visit> existingVisits = new ArrayList<>();
        visitRepository.saveAll(List.of(Visit.builder().shop(shop).build(), Visit.builder().shop(shop).build()))
                .forEach(visit -> existingVisits.add(visit));

        ResponseEntity<Visit[]> response = restTemplate.getForEntity(VISIT_ENDPOINT, Visit[].class);

        visitRepository.deleteAll(existingVisits);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingVisits, List.of(response.getBody()));
    }

    @Test
    public void getVisitsPage_returnsPageOfVisits() {
        List<Visit> existingVisits = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingVisits.add(visitRepository.save(Visit.builder().shop(shop).build()));
        }

        ParameterizedTypeReference<PageResponse<Visit>> responseType =
                new ParameterizedTypeReference<>() {};
        ResponseEntity<PageResponse<Visit>> response = restTemplate
                .exchange(VISIT_ENDPOINT + "list?page=1&size=10",
                HttpMethod.GET, null, responseType);

        visitRepository.deleteAll(existingVisits);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingVisits.size(), response.getBody().getTotalElements());
        assertEquals(10, response.getBody().getTotalPages());
        assertEquals(10, response.getBody().getSize());
    }

    @Test
    public void getVisitById_returnsVisit() {
        Visit existingVisit = visitRepository.save(Visit.builder().shop(shop).build());

        ResponseEntity<Visit> response = restTemplate
                .getForEntity(VISIT_ENDPOINT + existingVisit.getId(), Visit.class);

        visitRepository.delete(existingVisit);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingVisit, response.getBody());
    }

    @Test
    public void getVisitById_whenVisitDoesNotExist_returnsError() {
        ResponseEntity<Visit> response = restTemplate
                .getForEntity(VISIT_ENDPOINT + Long.valueOf(999L), Visit.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void getVisitsByShopId_returnsVisits() {
        List<Visit> existingVisits = new ArrayList<>();
        visitRepository.saveAll(List.of(Visit.builder().shop(shop).build(), Visit.builder().shop(shop).build()))
                .forEach(visit -> existingVisits.add(visit));

        ResponseEntity<Visit[]> response = restTemplate
                .getForEntity(VISIT_SHOP_ENDPOINT + shop.getId(), Visit[].class);

        visitRepository.deleteAll(existingVisits);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingVisits, List.of(response.getBody()));
    }

    @Test
    public void getVisitsByShopId_whenShopDoesNotExist_returnsError() {
        ResponseEntity<Object> response = restTemplate
                .getForEntity(VISIT_SHOP_ENDPOINT + Long.valueOf(999L), Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void createVisit_createsAndReturnsVisit() {
        ResponseEntity<Visit> response = restTemplate
                .postForEntity(VISIT_SHOP_ENDPOINT + shop.getId(), null, Visit.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());

        visitRepository.delete(response.getBody());
    }

    @Test
    public void createVisit_whenShopDoesNotExist_returnsError() {
        ResponseEntity<Object> response = restTemplate
                .postForEntity(VISIT_SHOP_ENDPOINT + Long.valueOf(999L), null, Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void startVisit_startsAndReturnsVisit() {
        Visit existingVisit = visitRepository.save(Visit.builder().shop(shop).build());

        ResponseEntity<Visit> response = restTemplate
                .postForEntity(VISIT_ENDPOINT + existingVisit.getId() + "/start", null, Visit.class);

        visitRepository.delete(existingVisit);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingVisit.getShop(), response.getBody().getShop());
        assertNotNull(response.getBody().getStarted());
    }

    @Test
    public void startVisit_whenVisitDoesNotExist_returnsError() {
        ResponseEntity<Object> response = restTemplate
                .postForEntity(VISIT_ENDPOINT + Long.valueOf(999L) + "/start", null, Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void completeVisit_completesAndReturnsVisit() {
        Visit existingVisit = visitRepository.save(Visit.builder().shop(shop).build());

        ResponseEntity<Visit> response = restTemplate
                .postForEntity(VISIT_ENDPOINT + existingVisit.getId() + "/complete", null, Visit.class);

        visitRepository.delete(existingVisit);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(existingVisit.getShop(), response.getBody().getShop());
        assertNotNull(response.getBody().getStarted());
        assertNotNull(response.getBody().getCompleted());
    }

    @Test
    public void completeVisit_whenVisitDoesNotExist_returnsError() {
        ResponseEntity<Object> response = restTemplate
                .postForEntity(VISIT_ENDPOINT + Long.valueOf(999L) + "/complete", null, Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void deleteVisit_deletesVisit() {
        Visit existingVisit = visitRepository.save(Visit.builder().shop(shop).build());

        restTemplate.delete(VISIT_ENDPOINT + existingVisit.getId());

        assertFalse(visitRepository.findById(existingVisit.getId()).isPresent());
    }

    @Test
    public void deleteVisit_whenVisitDoesNotExist_returnsError() {
        ResponseEntity<?> response = restTemplate
                .exchange(VISIT_ENDPOINT + Long.valueOf(999L), HttpMethod.DELETE, null, Object.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
