package ru.yandex.practicum.commerce.warehouse.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.commerce.warehouse.model.Reservation;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByShoppingCartId(UUID shoppingCartId);

    Boolean existsByShoppingCartIdAndUsernameAndProductId(UUID shoppingCartId,
                                                                      String username, UUID productId);

    List<Reservation> findAllByShoppingCartIdAndUsernameAndProductIdIn(UUID shoppingCartId,
                                                                       String username, Set<UUID> productIds);
    List<Reservation> findByUsernameAndProductIdIn(String username, Set<UUID> productIds);
}
