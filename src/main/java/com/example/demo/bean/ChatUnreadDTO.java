package com.example.demo.bean;

import lombok.Data;

@Data
public class ChatUnreadDTO {
	private RegisterEntity2 registerEntity2;
	private int countFlag;
	
	public ChatUnreadDTO(RegisterEntity2 registerEntity2, int countFlag){
		this.registerEntity2 = registerEntity2;
		this.countFlag = countFlag;
	}
}
