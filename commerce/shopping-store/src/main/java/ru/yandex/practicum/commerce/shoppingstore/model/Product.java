package ru.yandex.practicum.commerce.shoppingstore.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.commerce.interactionapi.shop.ProductCategory;
import ru.yandex.practicum.commerce.interactionapi.shop.ProductState;
import ru.yandex.practicum.commerce.interactionapi.shop.QuantityState;

import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product", schema = "product_schema")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {
    @Id
    UUID id;

    @Column(nullable = false)
    String productName;

    @Column(nullable = false)
    String description;

    @Column(name = "image_src")
    String imageSrc;

    @Enumerated(EnumType.STRING)
    @Column(name = "quantity_state", nullable = false)
    QuantityState quantityState;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_state", nullable = false)
    ProductState productState;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_category")
    ProductCategory productCategory;

    @Column(nullable = false)
    Double price;
}
