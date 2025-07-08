package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.bean.ChatUnreadEntity;

public interface ChatUnreadRepository extends JpaRepository<ChatUnreadEntity,Integer>{
	
    // userテーブルのserial_idを全件取得
    @Query("SELECT r.serialId FROM RegisterEntity2 r")
    List<Integer> findAllSerialIds();
    
    // groupChatUnreadテーブルのcntUnreadカラムを取得
    @Query("SELECT c.cntUnread FROM ChatUnreadEntity c WHERE c.serialId = :serialId")
    Integer findCntUnread(@Param("serialId") int serial_id);
    
    // ログイン者に個別、全体関係なく未読メッセージの件数を取得
    // ホーム画面で *新着メッセージ有 と表示させるため。 １以上であれば表示
    // findCntUnreadOneHome で個別チャットの未読メッセージを検索し
    // findCntUnreadGroupHome で全体チャットの未読メッセージを検索する
    @Query("SELECT COUNT(m) FROM ChatEntity m WHERE m.flag = false AND m.chatRoomId = 0 AND m.receiverId = :serialId")
    Integer findCntUnreadOneHome(@Param("serialId") int serialId);
    @Query("SELECT m.cntUnread FROM ChatUnreadEntity m WHERE serialId = :serialId")
    Integer findCntUnreadGroupHome(@Param("serialId") int serialId);

}
