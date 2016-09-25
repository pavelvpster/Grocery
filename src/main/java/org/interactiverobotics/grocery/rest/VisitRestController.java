/*
 * VisitRestController.java
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

package org.interactiverobotics.grocery.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.interactiverobotics.grocery.domain.Visit;
import org.interactiverobotics.grocery.service.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Visit REST controller.
 */
@Api(value = "Visit", description = "Visit management endpoint")
@RestController
@RequestMapping(value = "/api/v1/visit")
public class VisitRestController {

    private final VisitService visitService;

    @Autowired
    public VisitRestController(final VisitService visitService) {
        this.visitService = visitService;
    }

    @ApiOperation(value = "Get all Visit(s)", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Visit> getVisits() {
        return this.visitService.getVisits();
    }

    @ApiOperation(value = "Get page of Visits", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Page<Visit> getVisitsPage(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                                     @RequestParam(value = "size", defaultValue = "10") Integer pageSize) {

        final PageRequest pageRequest = new PageRequest(pageNumber - 1, pageSize);
        return this.visitService.getVisits(pageRequest);
    }

    @ApiOperation(value = "Get Visit by Id", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Visit getVisitById(@PathVariable Long id) {
        return this.visitService.getVisitById(id);
    }

    @ApiOperation(value = "Get Visit(s) by Shop Id", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/shop/{shopId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Visit> getVisitsByShopId(@PathVariable Long shopId) {
        return this.visitService.getVisitsByShopId(shopId);
    }

    @ApiOperation(value = "Create Visit", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/shop/{shopId}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Visit createVisit(@PathVariable Long shopId) {
        return this.visitService.createVisit(shopId);
    }

    @ApiOperation(value = "Start Visit", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/{id}/start", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Visit startVisit(@PathVariable Long id) {
        return this.visitService.startVisit(id);
    }

    @ApiOperation(value = "Complete Visit", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/{id}/complete", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Visit completeVisit(@PathVariable Long id) {
        return this.visitService.completeVisit(id);
    }

    @ApiOperation(value = "Delete Visit", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void deleteVisit(@PathVariable Long id) {
        this.visitService.deleteVisit(id);
    }

}
