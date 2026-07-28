package com.convertexxx.dto;

import com.convertexxx.entity.Provider;
import com.convertexxx.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private Role role;
    private Provider provider;
    private boolean isPremium;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
