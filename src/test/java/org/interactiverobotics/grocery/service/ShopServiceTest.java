/*
 * ShopServiceTest.java
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Shop service test.
 */
@RunWith(SpringRunner.class)
public class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    private ShopService shopService;


    @Before
    public void setUp() throws Exception {
        shopService = new ShopService(shopRepository);
    }


    @Test
    public void testGetShops() {

        final List<Shop> existingShops = Arrays.asList(new Shop(1L, "test-shop-1"), new Shop(2L, "test-shop-2"));
        when(shopRepository.findAll()).thenReturn(existingShops);

        final List<Shop> shops = shopService.getShops();

        assertEquals(existingShops, shops);
    }

    @Test
    public void testGetShopsPage() {

        final List<Shop> existingShops = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingShops.add(new Shop(i, "test-shop-" + i));
        }

        when(shopRepository.findAll(any(Pageable.class))).thenAnswer(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            final Pageable pageable = invocation.getArgument(0);
            return new PageImpl<>(existingShops, pageable, existingShops.size());
        });

        final Page<Shop> shops = shopService.getShops(PageRequest.of(0, 10));

        assertEquals(existingShops.size(), shops.getTotalElements());
        assertEquals(10, shops.getTotalPages());
    }

    @Test
    public void testGetShopById() {

        Shop existingShop = new Shop(1L, "test-shop");
        when(shopRepository.findById(existingShop.getId())).thenReturn(Optional.of(existingShop));

        final Shop shop = shopService.getShopById(existingShop.getId());

        assertEquals(existingShop, shop);
    }

    @Test(expected = ShopNotFoundException.class)
    public void testGetNotExistingShopById() {

        when(shopRepository.findById(any())).thenReturn(Optional.empty());

        shopService.getShopById(1L);
    }

    @Test
    public void testGetShopByName() {

        Shop existingShop = new Shop(1L, "test-shop");
        when(shopRepository.findOneByName(existingShop.getName())).thenReturn(existingShop);

        final Shop shop = shopService.getShopByName("test-shop");

        assertEquals(existingShop, shop);
    }

    @Test(expected = ShopNotFoundException.class)
    public void testGetNotExistingShopByName() {

        when(shopRepository.findById(any())).thenReturn(Optional.empty());

        shopService.getShopByName("test-name");
    }


    public static class SaveAndReturnShopAnswer implements Answer<Shop> {

        private Shop shop;

        public Shop getShop() {
            return shop;
        }

        @Override
        public Shop answer(InvocationOnMock invocation) throws Throwable {
            assertEquals(1, invocation.getArguments().length);
            shop = invocation.getArgument(0);
            if (shop.getId() == null) {
                shop.setId(1L);
            }
            return shop;
        }
    }


    @Test
    public void testCreateShop() {

        final SaveAndReturnShopAnswer saveAndReturnShopAnswer = new SaveAndReturnShopAnswer();
        when(shopRepository.save(any(Shop.class))).then(saveAndReturnShopAnswer);

        final ShopForm form = new ShopForm("test-shop");

        final Shop shop = shopService.createShop(form);

        // Check that Service returns what was saved
        final Shop savedShop = saveAndReturnShopAnswer.getShop();
        assertEquals(savedShop, shop);

        // Check response content
        assertEquals(form.getName(), shop.getName());
    }

    @Test
    public void testUpdateShop() {

        Shop existingShop = new Shop(1L, "test-shop");
        when(shopRepository.findById(existingShop.getId())).thenReturn(Optional.of(existingShop));

        final SaveAndReturnShopAnswer saveAndReturnShopAnswer = new SaveAndReturnShopAnswer();
        when(shopRepository.save(any(Shop.class))).then(saveAndReturnShopAnswer);

        final ShopForm form = new ShopForm("updated-test-shop");

        final Shop shop = shopService.updateShop(existingShop.getId(), form);

        // Check that Service returns what was saved
        final Shop savedShop = saveAndReturnShopAnswer.getShop();
        assertEquals(savedShop, shop);

        // Check response content
        assertEquals(form.getName(), shop.getName());
    }

    @Test(expected = ShopNotFoundException.class)
    public void testUpdateNotExistingShop() {

        when(shopRepository.findById(any())).thenReturn(Optional.empty());

        final ShopForm form = new ShopForm("updated-test-shop");

        shopService.updateShop(999L, form);
    }

    @Test
    public void testDeleteShop() {

        Shop existingShop = new Shop(1L, "test-shop");
        when(shopRepository.findById(existingShop.getId())).thenReturn(Optional.of(existingShop));

        shopService.deleteShop(existingShop.getId());

        verify(shopRepository).delete(eq(existingShop));
    }

    @Test(expected = ShopNotFoundException.class)
    public void testDeleteNotExistingShop() {

        when(shopRepository.findById(any())).thenReturn(Optional.empty());

        shopService.deleteShop(999L);
    }
}
