package com.example.demo.bean;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="group_chat_unread")
@Data
public class ChatUnreadEntity {
	@Id
	@Column(name="serial_id")
	private int serialId;
	@Column(name="cnt_unread")
	private int cntUnread;
}
