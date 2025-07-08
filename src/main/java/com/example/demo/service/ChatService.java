package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.bean.ChatEntity;
import com.example.demo.bean.ChatUnreadEntity;
import com.example.demo.repository.ChatUnreadRepository;
import com.example.demo.repository.ChatRepository;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ChatService {
	private final ChatRepository chatRepository;
	private final ChatUnreadRepository chatGroupChatUnreadRepository;
	private final HttpSession session;
	
	// メッセージをDBに保存
    public void saveMessage(int chat_room_id, int sender_id, int receiver_id, String message_text) {
        ChatEntity message = new ChatEntity();
        message.setChatRoomId(chat_room_id);
        message.setSenderId(sender_id);
        message.setReceiverId(receiver_id);
        message.setMessageText(message_text);
        // message.setTimestamp(LocalDateTime.now());

        chatRepository.save(message); // ✅ DBに保存
    }
    
    // @GetMapping("/chat_room_all")で使う
    // グループチャットのメッセージを取り出す
    public List<Object[]> getGroupChatMessage(){
    	return chatRepository.findMessagesWithUid();
    }
    
    // ログイン者以外のユーザーの groupChatUnreadテーブルcntUnreadカラムの値を+1する
    public void unreadIncrement(){
    	// 登録されているユーザーのserial_idを全件取得
    	List<Integer> userSerial_id = chatGroupChatUnreadRepository.findAllSerialIds();
    	// ログイン者のserial_idをリストから削除
    	userSerial_id.remove((Integer)session.getAttribute("mySerial_id"));
    	
    	for(Integer serial_id : userSerial_id) {
    		// グループチャットの未読件数を取得してその値を＋１してDBに保存
    		Integer cntUnread = chatGroupChatUnreadRepository.findCntUnread(serial_id);
    		// cntUnreadがNULLのとき０を代入
    		int count = (cntUnread != null) ? cntUnread : 0;
    		count++;		// 未読メッセージカウントを＋１
    		
    		// DBに保存
    		ChatUnreadEntity chatUnreadEntity = new ChatUnreadEntity();
    		chatUnreadEntity.setSerialId(serial_id);
    		chatUnreadEntity.setCntUnread(count);
    		chatGroupChatUnreadRepository.save(chatUnreadEntity);
    	}    	
    }
    
    // グループチャットの未読を０にする
	public void resetUnread() {
		ChatUnreadEntity chatUnreadEntity = new ChatUnreadEntity();
		chatUnreadEntity.setSerialId((int)session.getAttribute("mySerial_id"));
		chatUnreadEntity.setCntUnread(0);
		chatGroupChatUnreadRepository.save(chatUnreadEntity);
	}
    
	// DBからログイン者のグループチャットの未読件数を取得
    public Integer findCntUnreadGroup() {
    	return chatGroupChatUnreadRepository.findCntUnread((int)session.getAttribute("mySerial_id"));
    }

}
