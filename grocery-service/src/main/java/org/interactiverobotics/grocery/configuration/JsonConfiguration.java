/*
 * JsonConfiguration.java
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

package org.interactiverobotics.grocery.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * JSON configuration.
 * This configuration is required to serialize Entity that contain reference to other Entity
 * with <code>FetchType.LAZY</code>.
 * Add <code>@JsonFilter("jpaFilter")</code> to Entity class declaration.
 */
@Configuration
public class JsonConfiguration {

    /**
     * ObjectMapper bean.
     */
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        final ObjectMapper mapper = builder.build();
        final FilterProvider filters = new SimpleFilterProvider().addFilter("jpaFilter",
                SimpleBeanPropertyFilter.serializeAllExcept("handler", "hibernateLazyInitializer"));
        mapper.setFilterProvider(filters);
        mapper.setConfig(mapper.getSerializationConfig().without(SerializationFeature.FAIL_ON_EMPTY_BEANS));
        return mapper;
    }

}
