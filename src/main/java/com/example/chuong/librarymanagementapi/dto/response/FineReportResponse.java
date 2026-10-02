package com.example.chuong.librarymanagementapi.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FineReportResponse {
    private BigDecimal totalFines;
    private long borrowsWithFine;
    private BigDecimal averageFine;
}
