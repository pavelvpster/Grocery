/*
 * ItemWebController.java
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

package org.interactiverobotics.grocery.web;

import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Item web controller.
 */
@Controller
@RequestMapping("/item")
public class ItemWebController {

    private final ItemRepository itemRepository;

    public ItemWebController(final ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @RequestMapping("/")
    public String index() {
        return "item";
    }

    @RequestMapping("/list")
    public String getItemList(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                              @RequestParam(value = "size", defaultValue = "10") Integer pageSize, Model model) {

        final PageRequest pageRequest = new PageRequest(pageNumber - 1, pageSize);
        final Page<Item> page = this.itemRepository.findAll(pageRequest);

        final List<Item> items = new ArrayList<>();
        page.forEach(item -> items.add(item));

        model.addAttribute("offset", 1 + (pageNumber - 1) * pageSize);
        model.addAttribute("items", items);
        model.addAttribute("page_count", page.getTotalPages());
        model.addAttribute("current_page", pageNumber);

        return "item_list";
    }

}
