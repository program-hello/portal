package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.bean.ChatEntity;

import jakarta.transaction.Transactional;

public interface ChatRepository extends JpaRepository<ChatEntity,Integer>{
	
//	// sender_id と receiver_id で絞り込んだデータを messagesテーブルから取り出す
//	List<ChatDTO> findBySenderIdAndReceiveId(int senderId, int receiveId);
    @Query("SELECT m.chatRoomId, m.senderId, m.receiverId, m.messageText, m.timestamp " +
            "FROM ChatEntity m " +
            "WHERE m.senderId = :senderId AND m.receiverId = :receiverId")
     List<Object[]> findChatRoomMessages(@Param("senderId") int sender_id, @Param("receiverId") int receiver_id);
	
     // chatRoomId による絞り込み
     // 全体チャットを取り出すのに使う（chat_room_id = 1）
     List<ChatEntity> findByChatRoomId(int chat_room_id);
     
     // sender_id を uid に置き換えて全体チャットをDBから取り出す
     @Query("SELECT m.chatRoomId, u.uid, m.receiverId, m.messageText, m.timestamp " +
    	       "FROM ChatEntity m " +
    	       "JOIN RegisterEntity2 u ON m.senderId = u.serialId " +
    	       "WHERE m.chatRoomId = 1")
      List<Object[]> findMessagesWithUid();
      
      // 未読メッセージの id を取得
      @Query("SELECT m.id FROM ChatEntity m WHERE m.flag = false")
      List<Integer> findFlag();
      
      // 未読メッセージのフラグをおろす
      // UPDATE DELETE には @Modify, @Transactional が必要　これがないと spring は select だと認識してしまう
      @Modifying
      @Transactional
      @Query("UPDATE ChatEntity m SET m.flag = true WHERE m.senderId = :receiverId AND m.receiverId = :senderId")
      void updateFlagToTrueById(@Param("senderId") int senderId, @Param("receiverId") int receiverId);
      
      // sender_id 別未読メッセージをカウントする
      @Query("SELECT COUNT(m) FROM ChatEntity m WHERE m.flag = false AND m.senderId = :receiverId AND m.receiverId = :senderId")
      int countMessagesWithFlagFalseAndSenderReceiver(
              @Param("senderId") int senderId, 
              @Param("receiverId") int receiverId);
      

}
