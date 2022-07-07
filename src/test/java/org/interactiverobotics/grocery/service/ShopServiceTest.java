/*
 * ShopServiceTest.java
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

import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.exception.ShopNotFoundException;
import org.interactiverobotics.grocery.form.ShopForm;
import org.interactiverobotics.grocery.repository.ShopRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Shop service test.
 */
@ExtendWith(MockitoExtension.class)
public class ShopServiceTest {

    private static final String TEST_SHOP_NAME = "test-shop";

    @Mock
    private ShopRepository shopRepository;

    @InjectMocks
    private ShopService shopService;


    @Test
    public void getShops_returnsShops() {
        List<Shop> existingShops = List.of(new Shop(1L, "test-shop-1"), new Shop(2L, "test-shop-2"));
        when(shopRepository.findAll()).thenReturn(existingShops);

        List<Shop> shops = shopService.getShops();

        assertEquals(existingShops, shops);
    }

    @Test
    public void getShops_givenPageRequest_returnsPageOfShops() {
        List<Shop> existingShops = new ArrayList<>();
        for (long i = 0; i < 100; i ++) {
            existingShops.add(new Shop(i, "test-shop-" + i));
        }

        when(shopRepository.findAll(any(Pageable.class))).thenAnswer(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            Pageable pageable = invocation.getArgument(0);
            return new PageImpl<>(existingShops, pageable, existingShops.size());
        });

        Page<Shop> shops = shopService.getShops(PageRequest.of(0, 10));

        assertEquals(existingShops.size(), shops.getTotalElements());
        assertEquals(10, shops.getTotalPages());
    }

    @Test
    public void getShopById_returnsShop() {
        Shop existingShop = new Shop(1L, TEST_SHOP_NAME);
        when(shopRepository.findById(existingShop.getId())).thenReturn(Optional.of(existingShop));

        Shop shop = shopService.getShopById(existingShop.getId());

        assertEquals(existingShop, shop);
    }

    @Test
    public void getShopById_whenShopDoesNotExist_throwsException() {
        assertThrows(ShopNotFoundException.class, () -> {
            when(shopRepository.findById(any())).thenReturn(Optional.empty());

            shopService.getShopById(1L);
        });
    }

    @Test
    public void getShopByName_returnsShop() {
        Shop existingShop = new Shop(1L, TEST_SHOP_NAME);
        when(shopRepository.findOneByName(existingShop.getName())).thenReturn(existingShop);

        Shop shop = shopService.getShopByName(TEST_SHOP_NAME);

        assertEquals(existingShop, shop);
    }

    @Test
    public void getShopByName_whenShopDoesNotExist_throwsException() {
        assertThrows(ShopNotFoundException.class, () -> {
            when(shopRepository.findOneByName(anyString())).thenReturn(null);

            shopService.getShopByName("test-name");
        });
    }

    @Test
    public void createShop_createsAndReturnsShop() {
        when(shopRepository.save(any(Shop.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        ShopForm form = new ShopForm(TEST_SHOP_NAME);

        Shop shop = shopService.createShop(form);

        ArgumentCaptor<Shop> captor = ArgumentCaptor.forClass(Shop.class);
        verify(shopRepository).save(captor.capture());
        Shop savedShop = captor.getValue();

        assertEquals(savedShop, shop);
        assertEquals(form.getName(), shop.getName());
    }

    @Test
    public void updateShop_updatesAndReturnsShop() {
        Shop existingShop = new Shop(1L, TEST_SHOP_NAME);
        when(shopRepository.findById(existingShop.getId())).thenReturn(Optional.of(existingShop));

        when(shopRepository.save(any(Shop.class))).then(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0);
        });

        ShopForm form = new ShopForm("updated-test-shop");

        Shop shop = shopService.updateShop(existingShop.getId(), form);

        ArgumentCaptor<Shop> captor = ArgumentCaptor.forClass(Shop.class);
        verify(shopRepository).save(captor.capture());
        Shop savedShop = captor.getValue();

        assertEquals(savedShop, shop);
        assertEquals(form.getName(), shop.getName());
    }

    @Test
    public void updateShop_whenShopDoesNotExists_throwsException() {
        assertThrows(ShopNotFoundException.class, () -> {
            when(shopRepository.findById(any())).thenReturn(Optional.empty());

            ShopForm form = new ShopForm("updated-test-shop");

            shopService.updateShop(999L, form);
        });
    }

    @Test
    public void deleteShop_deletesShop() {
        Shop existingShop = new Shop(1L, TEST_SHOP_NAME);
        when(shopRepository.findById(existingShop.getId())).thenReturn(Optional.of(existingShop));

        shopService.deleteShop(existingShop.getId());

        verify(shopRepository).delete(eq(existingShop));
    }

    @Test
    public void deleteShop_whenShopDoesNotExist_throwsException() {
        assertThrows(ShopNotFoundException.class, () -> {
            when(shopRepository.findById(any())).thenReturn(Optional.empty());

            shopService.deleteShop(999L);
        });
    }
}
