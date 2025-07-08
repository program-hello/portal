package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.repository.ChatRepository;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ChatMessageSetService {
	private final ChatRepository chatRepository;
	private final HttpSession session;
	
	
	public void chatMessageSet() {
		
		// DBから自分が送ったメッセージをDBから取り出す
		List<Object[]> myMessage = chatRepository.findChatRoomMessages((int)session.getAttribute("mySerial_id"), (int)session.getAttribute("receiver_id"));
		// DBから相手が送ったメッセージをDBから取り出す
		List<Object[]> youMessage = chatRepository.findChatRoomMessages((int)session.getAttribute("receiver_id"), (int)session.getAttribute("mySerial_id"));
		
		// 自分と相手のメッセージを一つのリスト(allList)にして timestamp で昇順にソートして session に保存
		List<Object[]>  allList = new ArrayList<>();
		for(Object[] list : myMessage) {
			allList.add(list);
		}
		for(Object[] list : youMessage) {
			allList.add(list);
		}
		// timestamp を基準に昇順にソート
		allList.sort(Comparator.comparing(o -> (LocalDateTime) o[4]));
		
		// timestamp の T と秒を削除する
		for(Object[] list : allList) {
			LocalDateTime dateTime = (LocalDateTime) list[4];
			String time = dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd'<br>'HH:mm"));
			list[4] = time;
		}

		
		session.setAttribute("chatMessageOne", allList);
	}
}
