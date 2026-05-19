package com.primaryhomework.backend.entity.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageDto<T> {

    private List<T> list = new ArrayList<>();
    private Long total = 0L;
    private Integer pageNo = 1;
    private Integer pageSize = 20;
}
