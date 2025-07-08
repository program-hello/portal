package com.example.demo.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    @MessageMapping("/chat/send")
    @SendTo("/topic/public")
    public String sendMessage(String message,@AuthenticationPrincipal UserDetails userDetails) {
    	System.out.println("受信したメッセージ: " + message);
    	
        return message;  // 単純な文字列をそのまま返す
    }
}
