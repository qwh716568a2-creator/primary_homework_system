package com.primaryhomework.backend.entity.vo.mobile;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReviewVo {

    private String id;
    private String status;
    private BigDecimal score;
    private String level;
    private String comment;
    private String reviewedAt;
    private List<String> images = new ArrayList<>();
}
