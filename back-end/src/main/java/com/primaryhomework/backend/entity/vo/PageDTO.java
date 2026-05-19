package com.primaryhomework.backend.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO<T> {

    private List<T> list;
    private Long total;
    private Integer pageNo;
    private Integer pageSize;

    public static <T> PageDTO<T> of(List<T> list, long total, Integer pageNo, Integer pageSize) {
        return new PageDTO<>(list == null ? Collections.emptyList() : list, total, pageNo, pageSize);
    }
}
