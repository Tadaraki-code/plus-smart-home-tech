package ru.yandex.practicum.commerce.shoppingcart.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Builder
@Table(name = "cart", schema = "cart_schema")
@AllArgsConstructor
@NoArgsConstructor
public class Cart {

    @Id
    private UUID Id;

    @Column(name = "user_name")
    private String userName;

    @ElementCollection
    @CollectionTable(
            name = "cart_item",
            schema = "cart_schema",
            joinColumns = @JoinColumn(name = "cart_id")
    )
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Integer> products = new HashMap<>();
}
