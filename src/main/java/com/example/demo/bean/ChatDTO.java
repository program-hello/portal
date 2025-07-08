package com.example.demo.bean;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class ChatDTO {

	@Id
    private Long id;
    @Column(name = "chat_room_id")
    private int chatRoomId;
    @Column(name = "sender_id") // DBは sender_id
    private int senderId;        // Javaでは senderId
    @Column(name = "receive_id") // DBは receive_id
    private int receiveId;
    @Column(name = "message_text")
    private String messageText;
    private LocalDateTime timestamp;
}
