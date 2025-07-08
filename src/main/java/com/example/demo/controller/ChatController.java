package com.example.demo.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.bean.ChatEntity;
import com.example.demo.bean.ChatUnreadDTO;
import com.example.demo.bean.RegisterEntity2;
import com.example.demo.repository.ChatRepository;
import com.example.demo.service.ChatMessageSetService;
import com.example.demo.service.ChatService;
import com.example.demo.service.SignUpService;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class ChatController {
	
	private final ChatService chatService;
	private final SignUpService signUpService;
	private final ChatMessageSetService chatMessageGet;
	private final ChatRepository chatRepository;
	private SimpMessagingTemplate messagingTemplate;	// web-socketで使う
	private final HttpSession session;
	
	// ホームからチャットをクリックで遷移
	@GetMapping("/chat")
	public String chat(Model model) {
		// ユーザーリストをDBから全件取得してhtmlに渡す
		List<RegisterEntity2> userList = signUpService.getAllUsers();
		model.addAttribute("userList",userList);
		
		// ユーザー別未読メッセージの数を session に登録
		List<ChatUnreadDTO> chatUnreadList = new ArrayList<>();
		for(RegisterEntity2 r: userList) {
			// ユーザー別未読メッセージの数をDBから取得
			// ログイン者のIDと送信者のIDで検索をかける
			int countFlag = chatRepository.countMessagesWithFlagFalseAndSenderReceiver
					((int)session.getAttribute("mySerial_id"), r.getSerialId());
			
			// ユーザーリストと未読メッセージ件数を ChatUnreadDTO クラスに移植して リスト に追加する
			ChatUnreadDTO cuDTO = new ChatUnreadDTO(r,countFlag);
			chatUnreadList.add(cuDTO);
		}
		session.setAttribute("chatUnreadList", chatUnreadList);
		
		// グループチャットの未読件数をDBから取得してhtmlに渡す
		Integer cntGroupUnread = chatService.findCntUnreadGroup();
		cntGroupUnread = (cntGroupUnread != null) ? cntGroupUnread : 0;	// null の場合 0を代入
		model.addAttribute("cntGroupUnread",cntGroupUnread);
		
		return "chat_home";
	}
	
	// チャットホームで名前のクリックしたときの遷移
	@GetMapping("/chat_room_one")
	public String chat_room(@RequestParam("chat_room_id") int chat_room_id,  
			@RequestParam("receiver_id") int receiver_id, 
			@RequestParam("receiver_uid") String receiver_uid, 
			Model model) {
		
		// チャット相手の id, uid をセッションに保存
		session.setAttribute("receiver_id",receiver_id);	
		session.setAttribute("receiver_uid",receiver_uid);
		
		// クリックしたユーザーとの未読件数を取得して html に渡す
		// ログイン者のIDと送信者のIDで検索をかける
		// highlightCount : 未読メッセージ件数
		Integer highlightCount = chatRepository.countMessagesWithFlagFalseAndSenderReceiver
				((int)session.getAttribute("mySerial_id"), receiver_id);
		highlightCount = highlightCount == null ? 0 : highlightCount;	// nullを0に変換
		model.addAttribute("highlightCount",highlightCount);
		
		// チャット内容（二人）をDBから取り出して timestamp順に並び変えて session に保存
		chatMessageGet.chatMessageSet();
		// DBに保存されている個別チャット未読メッセージのカウントをリセットする
		chatRepository.updateFlagToTrueById((int)session.getAttribute("mySerial_id"), receiver_id);
		
		// 個別チャット内に留まっている間は web-socket のリロードが行われたとき、未読メッセージの数(highlightCount)をリセットする
		// ログインして初めて個別チャットに入ったときは 
		// session.getAttribute("indiviUnreadFlag")が null なので if文で判定を行う
		// それ以降は @GetMapping("/reChat") に遷移するときに indiviUnreadFlag = 0 となる（個別チャットに留まっているかの判定用フラグを下す）
		if(session.getAttribute("indiviUnreadFlag") != null) {
			if((int)session.getAttribute("indiviUnreadFlag") == 1) {
				// highlightCount : 未読メッセージ件数を０にする
				model.addAttribute("highlightCount",0);
			}
		}		
		// indiviUnreadFlag : 個別チャットに留まっているかの判定用フラグ
		session.setAttribute("indiviUnreadFlag", 1);
	
		return "chat_room_one";

	}
	
	// 個別チャットの入力をDBに保存
	@PostMapping("/chat_room_one/message_post")
	public String messageOne_post(@ModelAttribute ChatEntity ChatEntity, Model model) {
		
		// チャットで入力したメッセージをDBに保存
		chatService.saveMessage(ChatEntity.getChatRoomId(),ChatEntity.getSenderId()
				,ChatEntity.getReceiverId(),ChatEntity.getMessageText());
		
        // 個別チャット相手にリロード命令をWebSocketで送信
		// ログイン者のIDとチャット相手のIDを結合した文字列を認証コードに設定する
		String authCode = String.valueOf(session.getAttribute("mySerial_id"));	
		authCode += String.valueOf(session.getAttribute("receiver_id"));
		messagingTemplate.convertAndSend("/topic/reload/" + authCode, "reload");
		
		// チャットホーム画面にいるチャット相手にメッセージを送る
		authCode = String.valueOf(session.getAttribute("receiver_id"));
		messagingTemplate.convertAndSend("/topic/reload/" + authCode, "reloadChatHomeOne");
		
        return "redirect:/chat_room_one/message_get";
	}
	
	// chat_room_one.html を表示するだけの @GetMapping
	// リロードして何回も同じメッセージが保存されるのを防ぐため
	@GetMapping("/chat_room_one/message_get")
	public String message_get(Model model) {
		// チャット内容（二人）をDBから取り出して timestamp順に並び変えて session に保存
		chatMessageGet.chatMessageSet();
		model.addAttribute("highlightCount",0);
		
		return "chat_room_one";
	}
	// 個別チャット画面の戻るボタンからの遷移
	@GetMapping("/reChatOne")
	public String reChat(Model model) {
		// DBに保存されている個別チャット相手との未読メッセージのカウントをリセットする
		chatRepository.updateFlagToTrueById((int)session.getAttribute("mySerial_id"), (int)session.getAttribute("receiver_id"));
		
		// 個別チャットを離れるため、個別チャットに留まっているかの判定用フラグを下す
		session.setAttribute("indiviUnreadFlag", 0);
		return "redirect:/chat";
	}
	
	
	@GetMapping("/chat_room_all")
	public String chat_room_all(Model model) {

		// service で全体チャットのメッセージ取得 ＆ sender_id を uid に置き換える & timestamp の整形
		List<Object[]> chatMessageAll = chatService.getGroupChatMessage();
		
		// timestamp の T と秒を削除する
		for(Object[] list : chatMessageAll) {
			LocalDateTime dateTime = (LocalDateTime) list[4];
			String time = dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd'<br>'HH:mm"));
			list[4] = time;
		}
		session.setAttribute("chatMessageAll", chatMessageAll);
		
		// グループチャットの未読件数を html に渡す
		Integer highlightCount = chatService.findCntUnreadGroup();
		highlightCount = highlightCount == null ? 0 : highlightCount;	// nullを0に変換
		model.addAttribute("highlightCount",highlightCount);
		
		// グループチャットの未読件数をリセットする
		chatService.resetUnread();
	

		// 個別チャット内で web-socket のリロードが行われたとき、未読メッセージの数(highlightCount)をリセットする
		// ログインして初めて個別チャットに入ったときは 
		// session.getAttribute("indiviUnreadFlag")が null なので if文で判定を行う
		// それ以降は @GetMapping("/reChat") に遷移するときに groupUnreadFlag = 0 となる（全体チャットに留まっているかの判定用フラグを下す）
		if(session.getAttribute("groupUnreadFlag") != null) {
			if((int)session.getAttribute("groupUnreadFlag") == 1) {
				// highlightCount : 未読メッセージ件数を０にする
				model.addAttribute("highlightCount",0);
			}
		}		
		// indiviUnreadFlag : 全体チャットに留まっているかの判定用フラグ
		session.setAttribute("groupUnreadFlag", 1);
		
		return "chat_room_all";
	}
	// 全体チャットのメッセージをDBに保存
	@PostMapping("/chat_room_all/message_post")
	public String messageAll_post(@ModelAttribute ChatEntity ChatEntity) {
		
		// 全体チャットで入力したメッセージをDBに保存
		chatService.saveMessage(ChatEntity.getChatRoomId(),ChatEntity.getSenderId()
				,ChatEntity.getReceiverId(),ChatEntity.getMessageText());
		
		// ログイン者以外のユーザーの group_chat_unreadテーブルのcntUnreadカラムの値を+1して、
		//group_chat_unreadテーブルに保存
		chatService.unreadIncrement();
		
		// グループチャット内にいるクライアント全員にメッセージを送信（リロード要求のため）
		messagingTemplate.convertAndSend("/topic/public", "reloadGroupChat");
		// チャットホームにいるクライアント全員にリロード命令をWebSocketで送信
		messagingTemplate.convertAndSend("/topic/public", "reloadChatHomeGroup");
		
		return "redirect:/chat_room_all";
	}
	@GetMapping("/reChatGroup")
	public String reChatGroup() {
		// 全体チャットを離れるため、個別チャットに留まっているかの判定用フラグを下す
		session.setAttribute("groupUnreadFlag", 0);
		return "redirect:/chat";
	}
}
