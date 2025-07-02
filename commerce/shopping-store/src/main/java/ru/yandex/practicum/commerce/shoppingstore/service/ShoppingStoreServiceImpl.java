package ru.yandex.practicum.commerce.shoppingstore.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.interactionapi.shop.*;
import ru.yandex.practicum.commerce.interactionapi.exceptions.ProductNotFoundException;
import ru.yandex.practicum.commerce.shoppingstore.mapper.ProductMapper;
import ru.yandex.practicum.commerce.shoppingstore.model.Product;
import ru.yandex.practicum.commerce.shoppingstore.storage.ProductRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingStoreServiceImpl implements ShoppingStoreService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public Page<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        log.info("Получение продуктов по категории: {}, пагинация: {}", category, pageable);
        Page<Product> productPage = productRepository.findByProductCategory(category, pageable);
        log.info("Найдено {} продуктов", productPage.getTotalElements());
        return productPage.map(productMapper::toDto);
    }

    @Override
    public ProductDto createNewProduct(ProductDto productDto) {
        log.info("Создание нового продукта: {}", productDto);
        Product product = productMapper.fromDto(productDto);

        if (product.getId() == null) {
            product.setId(UUID.randomUUID());
            log.info("Сгенерирован новый UUID для продукта: {}", product.getId());
        }

        if (checkExistsById(product.getId())) {
            log.info("Продукт с ID {} уже существует", product.getId());
            throw new IllegalArgumentException("Товар с ID " + product.getId() + " уже существует");
        }

        Product savedProduct = productRepository.save(product);
        log.info("Продукт успешно создан: {}", savedProduct);
        return productMapper.toDto(savedProduct);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        log.info("Обновление продукта: {}", productDto);

        if (productDto.getProductId() == null) {
            log.info("Обновление продукта без ID");
            throw new IllegalArgumentException("Идентификатор товара обязателен");
        }

        if (checkExistsById(productDto.getProductId())) {
            Product product = productRepository.getReferenceById(productDto.getProductId());

            if (!productDto.getProductName().equals(product.getProductName())) {
                log.info("Обновление имени продукта: {} -> {}", product.getProductName(), productDto.getProductName());
                product.setProductName(productDto.getProductName());
            }
            if (!productDto.getDescription().equals(product.getDescription())) {
                log.info("Обновление описания продукта");
                product.setDescription(productDto.getDescription());
            }
            if (productDto.getImageSrc() != null && !productDto.getImageSrc().equals(product.getImageSrc())) {
                log.info("Обновление изображения продукта");
                product.setImageSrc(productDto.getImageSrc());
            }
            if (!productDto.getQuantityState().equals(product.getQuantityState())) {
                log.info("Обновление состояния количества");
                product.setQuantityState(productDto.getQuantityState());
            }
            if (!productDto.getProductState().equals(product.getProductState())) {
                log.info("Обновление состояния продукта");
                product.setProductState(productDto.getProductState());
            }
            if (productDto.getProductCategory() != null &&
                    !productDto.getProductCategory().equals(product.getProductCategory())) {
                log.info("Обновление категории продукта");
                product.setProductCategory(productDto.getProductCategory());
            }
            if (!productDto.getPrice().equals(product.getPrice())) {
                log.info("Обновление цены: {} -> {}", product.getPrice(), productDto.getPrice());
                product.setPrice(productDto.getPrice());
            }

            Product updatedProduct = productRepository.save(product);
            log.info("Продукт успешно обновлён: {}", updatedProduct);
            return productMapper.toDto(updatedProduct);
        }

        log.info("Продукт с ID {} не найден для обновления", productDto.getProductId());
        throw new ProductNotFoundException("Товар с ID " + productDto.getProductId() + " не найден",
                "Товар с ID " + productDto.getProductId() + " не найден");
    }

    @Override
    public Boolean deleteProduct(UUID productId) {
        log.info("Удаление из каталога продукта с ID: {}", productId);

        if (checkExistsById(productId)) {
            Product product = productRepository.getReferenceById(productId);
            product.setProductState(ProductState.DEACTIVATE);
            productRepository.save(product);
            log.info("Продукт с ID {} успешно удалён из каталога", productId);
            return true;
        }

        log.info("Попытка удалить несуществующий продукт с ID {}", productId);
        return false;
    }

    @Override
    public Boolean updateQuantityState(UUID productId, QuantityState state) {
        log.info("Обновление количества для продукта c ID {}, новое значение : {}", productId, state);

        if (checkExistsById(productId)) {
            Product product = productRepository.getReferenceById(productId);
            product.setQuantityState(state);
            productRepository.save(product);
            log.info("Количество успешно обновлено для продукта с ID {}", state);
            return true;
        }

        log.info("Продукт с ID {} не найден для обновления количества", state);
        return false;
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        log.info("Запрос продукта по ID: {}", productId);

        if (checkExistsById(productId)) {
            Product product = productRepository.getReferenceById(productId);
            log.info("Продукт найден: {}", product);
            return productMapper.toDto(product);
        }

        log.info("Продукт с ID {} не найден", productId);
        throw new ProductNotFoundException("Товар с ID " + productId + " не найден",
                "Товар с ID " + productId + " не найден");
    }

    private boolean checkExistsById(UUID productId) {
        return productRepository.existsById(productId);
    }
}

