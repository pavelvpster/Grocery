/*
 * ShoppingListItemWebController.java
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
import org.interactiverobotics.grocery.domain.ShoppingListItem;
import org.interactiverobotics.grocery.form.ShoppingListItemCreateForm;
import org.interactiverobotics.grocery.form.ShoppingListItemUpdateForm;
import org.interactiverobotics.grocery.service.ShoppingListItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * ShoppingListItem web controller.
 */
@Controller
@RequestMapping("/shopping_list_item")
public class ShoppingListItemWebController {

    private final ShoppingListItemService shoppingListItemService;

    /**
     * Parametrized constructor.
     */
    @Autowired
    public ShoppingListItemWebController(final ShoppingListItemService shoppingListItemService) {
        this.shoppingListItemService = shoppingListItemService;
    }

//    /**
//     * Returns index page.
//     */
//    @RequestMapping({"", "/"})
//    public String index() {
//        return "shopping_list_item_select_shopping_list";
//    }

    /**
     * Returns index page.
     */
    @RequestMapping("/{shoppingListId}")
    public String index(@PathVariable Long shoppingListId, Model model) {
        model.addAttribute("shoppingListId", shoppingListId);
        return "shopping_list_item";
    }

    /**
     * Returns HTML block with list of ShoppingListItems.
     */
    @RequestMapping("/{shoppingListId}/list")
    public String getShoppingListItems(@PathVariable Long shoppingListId,
                                       @RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                                       @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                                       Model model) {

        final PageRequest pageRequest = new PageRequest(pageNumber - 1, pageSize);
        final Page<ShoppingListItem> page = this.shoppingListItemService
                .getShoppingListItems(pageRequest, shoppingListId);

        final List<ShoppingListItem> shoppingListItems = new ArrayList<>();
        page.forEach(shoppingListItem -> shoppingListItems.add(shoppingListItem));

        model.addAttribute("offset", 1 + (pageNumber - 1) * pageSize);
        model.addAttribute("shoppingListItems", shoppingListItems);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("currentPage", pageNumber);

        return "shopping_list_item_list";
    }

    /**
     * Returns create ShoppingListItem form.
     */
    @RequestMapping("/form")
    public String getCreateShoppingListItemForm(@RequestParam(value = "shoppingList") Long shoppingListId,
                                                Model model) {
        model.addAttribute("shoppingListId", shoppingListId);
        final List<Item> items = this.shoppingListItemService.getNotAddedItems(shoppingListId);
        model.addAttribute("items", items);
        return "shopping_list_item_form_create";
    }

    /**
     * Creates ShoppingListItem.
     */
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String createShoppingListItem(@Valid ShoppingListItemCreateForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/shopping_list/";
        }
        final ShoppingListItem shoppingListItem = this.shoppingListItemService.createShoppingListItem(form);
        return "redirect:/shopping_list_item/" + shoppingListItem.getShoppingList().getId();
    }

    /**
     * Returns update ShoppingListItem form (with current field values).
     */
    @RequestMapping("/{id}/form")
    public String getUpdateShoppingListItemForm(@PathVariable Long id, Model model) {
        final ShoppingListItem shoppingListItem = this.shoppingListItemService.getShoppingListItemById(id);
        model.addAttribute("shoppingListItem", shoppingListItem);
        return "shopping_list_item_form_update";
    }

    /**
     * Updates ShoppingListItem.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public String updateShoppingListItem(@PathVariable Long id,
                                         @Valid ShoppingListItemUpdateForm form,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/shopping_list_item/" +
                    this.shoppingListItemService.getShoppingListItemById(id).getShoppingList().getId();
        }
        final ShoppingListItem shoppingListItem = this.shoppingListItemService.updateShoppingListItem(id, form);
        return "redirect:/shopping_list_item/" + shoppingListItem.getShoppingList().getId();
    }

    /**
     * Deletes ShoppingListItem.
     * todo: ensure that there are no redirects from DELETE methods
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteShoppingListItem(@PathVariable Long id) {
        this.shoppingListItemService.deleteShoppingListItem(id);
    }

}
