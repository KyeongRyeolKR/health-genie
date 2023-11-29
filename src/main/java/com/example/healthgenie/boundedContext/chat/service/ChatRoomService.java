package com.example.healthgenie.boundedContext.chat.service;

import com.example.healthgenie.base.exception.ChatException;
import com.example.healthgenie.base.exception.CommonException;
import com.example.healthgenie.base.utils.SecurityUtils;
import com.example.healthgenie.boundedContext.chat.dto.RoomRequest;
import com.example.healthgenie.boundedContext.chat.dto.RoomResponse;
import com.example.healthgenie.boundedContext.chat.entity.ChatRoom;
import com.example.healthgenie.boundedContext.chat.repository.ChatRoomRepository;
import com.example.healthgenie.boundedContext.user.entity.User;
import com.example.healthgenie.boundedContext.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.example.healthgenie.base.exception.ChatErrorResult.*;
import static com.example.healthgenie.base.exception.CommonErrorResult.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long joinChatRoom(RoomRequest request) {
        User sender = SecurityUtils.getCurrentUser();

        User receiver = userRepository.findByEmail(request.getReceiverEmail())
                .orElseThrow(() -> new CommonException(USER_NOT_FOUND));

        if(Objects.equals(sender.getId(), receiver.getId())) {
            throw new ChatException(SELF_CHAT);
        }

        Optional<ChatRoom> opChatRoom = chatRoomRepository.findBySenderIdAndReceiverId(sender.getId(), receiver.getId());
        if(opChatRoom.isPresent()) {
            return opChatRoom.get().getId();
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .sender(sender)
                .receiver(receiver)
                .build();

        return chatRoomRepository.save(chatRoom).getId();
    }

    public List<RoomResponse> getRoomList() {
        User currentUser = SecurityUtils.getCurrentUser();

        return chatRoomRepository.findAllBySenderIdOrReceiverId(currentUser.getId(), currentUser.getId()).stream()
                .map(RoomResponse::of)
                .toList();
    }

    public RoomResponse getRoomDetail(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ROOM_NOT_FOUND));

        return RoomResponse.of(chatRoom);
    }

    @Transactional
    public String deleteRoom(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ROOM_NOT_FOUND));

        User currentUser = SecurityUtils.getCurrentUser();

        if (!chatRoom.getSender().getId().equals(currentUser.getId())) {
            throw new ChatException(NO_PERMISSION);
        }

        // TODO : 한명만 채팅방을 삭제 했을 경우엔 DB에서 삭제가 안되야 함 / 둘 다 삭제 했을 경우에 DB에서 아예 삭제 되어야 함
        // 지금은 한쪽만 삭제해도 채팅방이 사라짐

        chatRoomRepository.delete(chatRoom);

        return "채팅방이 삭제 되었습니다.";
    }
}