package com.restaurant.app.order;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.*;
import java.util.*;

@Embeddable
@Data @Builder(toBuilder = true) @NoArgsConstructor @AllArgsConstructor
public class OrderItem {
    @Column(length = 64)
    String id;

    @Column(nullable = false, length = 64)
    String niceName;

    @Column(nullable = false)
    Long price;
    Float discount;

    @Convert(converter = MapConverterJson.class)
    @Column(nullable = false)
    Map<String,Boolean> contents;
}

@Converter
class MapConverterJson implements AttributeConverter<Object, String> {
    private final ObjectMapper objectMapper;
    public MapConverterJson(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String convertToDatabaseColumn(Object meta) {
        try {
            return objectMapper.writeValueAsString(meta);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, Object.class);
        } catch (IOException ex) {
            return null;
        }
    }
}
