package com.radman.shop.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagedResponse extends ResponseService {

    private Integer page;

    private Integer size;

    private Long totalElements;

    private Integer totalPages;
}