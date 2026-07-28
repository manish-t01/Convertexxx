package com.convertexxx.dto;

import com.convertexxx.entity.BillingCycle;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SubscriptionResponse {
    private UUID id;
    private String planName;
    private BigDecimal price;
    private BillingCycle billingCycle;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
}
