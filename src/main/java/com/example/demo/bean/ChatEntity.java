package com.example.demo.bean;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "messages")
@Data
public class ChatEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // IDは自動生成
    private int id; // 自動生成されるID
    @Column(name = "chat_room_id") // ← DBの列名とマッピング
    private int chatRoomId;        // ← Javaのフィールド名はキャメルケース
    @Column(name = "sender_id")
    private int senderId;
    @Column(name = "receiver_id")
    private int receiverId;
    @Column(name = "message_text")
    private String messageText;
	private LocalDateTime timestamp;
	private boolean flag = false;
	
	//@PrePersist はエンティティが新しく保存（persist）される前に呼ばれる JPA のコールバックです。
    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}
