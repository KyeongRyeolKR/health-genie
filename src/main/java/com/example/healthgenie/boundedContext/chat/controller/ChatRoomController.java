package com.example.healthgenie.boundedContext.chat.controller;

import com.example.healthgenie.base.response.Result;
import com.example.healthgenie.boundedContext.chat.dto.RoomRequest;
import com.example.healthgenie.boundedContext.chat.dto.RoomResponse;
import com.example.healthgenie.boundedContext.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<Result> JoinChatRoom(@RequestBody RoomRequest request) {
        Long roomId = chatRoomService.joinChatRoom(request);

        URI chatRoomUri = UriComponentsBuilder.newInstance()
                .path("/chat/rooms/{roomId}")
                .buildAndExpand(roomId)
                .toUri();

        return ResponseEntity.ok(Result.of(chatRoomUri));
    }

    @GetMapping
    public ResponseEntity<Result> getChatRooms() {
        List<RoomResponse> response = chatRoomService.getRoomList();

        return ResponseEntity.ok(Result.of(response));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<Result> getChatRoom(@PathVariable Long roomId) {
        RoomResponse response = chatRoomService.getRoomDetail(roomId);

        return ResponseEntity.ok(Result.of(response));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Result> removeChatRoom(@PathVariable Long roomId) {
        String response = chatRoomService.deleteRoom(roomId);

        return ResponseEntity.ok(Result.of(response));
    }
}
