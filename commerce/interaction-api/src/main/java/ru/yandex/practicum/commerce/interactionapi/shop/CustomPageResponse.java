package ru.yandex.practicum.commerce.interactionapi.shop;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CustomPageResponse<T> {

    private final List<T> content;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;
    private final List<SortOrder> sort;

    public CustomPageResponse(Page<T> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.sort = page.getSort().stream()
                .map(order -> new SortOrder(order.getProperty(), order.getDirection().name()))
                .collect(Collectors.toList());
    }

}

