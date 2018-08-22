/*
 * VisitWebController.java
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

package org.interactiverobotics.grocery.web;

import org.interactiverobotics.grocery.domain.Visit;
import org.interactiverobotics.grocery.service.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Visit web controller.
 */
@Controller
@RequestMapping("/visit")
public class VisitWebController {

    private final VisitService visitService;

    /**
     * Parametrized constructor.
     */
    @Autowired
    public VisitWebController(final VisitService visitService) {
        this.visitService = visitService;
    }

    @RequestMapping("/")
    public String index() {
        return "visit";
    }

    /**
     * Returns HTML block with list of Visits.
     */
    @RequestMapping("/list")
    public String getVisits(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                            @RequestParam(value = "size", defaultValue = "10") Integer pageSize, Model model) {

        final Page<Visit> page = this.visitService.getVisits(PageRequest.of(pageNumber - 1, pageSize));

        final List<Visit> visits = new ArrayList<>();
        page.forEach(visit -> visits.add(visit));

        model.addAttribute("offset", 1 + (pageNumber - 1) * pageSize);
        model.addAttribute("visits", visits);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("currentPage", pageNumber);

        return "visit_list";
    }
}
