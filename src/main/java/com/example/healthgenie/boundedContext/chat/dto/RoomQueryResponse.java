package com.example.healthgenie.boundedContext.chat.dto;

import com.example.healthgenie.boundedContext.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomQueryResponse {

    private Long roomId;
    private String nickname;
    private Role role;
    private String profilePhoto;
}
