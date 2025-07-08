package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.bean.RegisterDTO;
import com.example.demo.repository.AdminPasswordResetRepository;
import com.example.demo.repository.ChatUnreadRepository;
import com.example.demo.repository.SignUpRepository;
import com.example.demo.service.SignUpService;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class PortalController {
	
	private final SignUpService signUpService;
	private final SignUpRepository signUpRepository;
	private final ChatUnreadRepository chatUnreadRepository;
	private final AdminPasswordResetRepository adminPasswordResetRepository;
	private final HttpSession session;
	
	@GetMapping("/home")
	public String home(Model model) {
		
		// ホーム画面に *新着メッセージがあります を表示させるための処理
		// セッションからユーザーIDを取得
		int mySerialId = (int) session.getAttribute("mySerial_id");
		// 個人チャット未読数を取得（null対策）
		Integer individualCount = chatUnreadRepository.findCntUnreadOneHome(mySerialId);
		if (individualCount == null) individualCount = 0;
		// グループチャット未読数を取得（null対策）
		Integer groupCount = chatUnreadRepository.findCntUnreadGroupHome(mySerialId);
		if (groupCount == null) groupCount = 0;
		// 合計してモデルに追加
		int cntChatUnread = individualCount + groupCount;
		model.addAttribute("cntChatUnread", cntChatUnread);
			
		// パスワードのリセットが行われたら、ホーム画面に パスワードの更新 ボタンを表示させるための処理
		boolean flag = adminPasswordResetRepository.existsByUid((String)session.getAttribute("myUid"));
		if(flag) model.addAttribute("PasswordResetFlag", true);
		else model.addAttribute("PasswordResetFlag", false);
		
		return "home";
	}
	
	@GetMapping("/password/update")
	String password_update(Model model) {
		model.addAttribute("registerDTO",new RegisterDTO());
		return "password_update";
	}
	// 新規パスワードを更新ボタンからの遷移
	@PostMapping("/password/update/post")
	String password_update_post(@Validated @ModelAttribute RegisterDTO registerDTO, BindingResult result, Model model) {
		// バリデーションチェック
		if(result.hasErrors()) {
			model.addAttribute("registerDTO",registerDTO);
			return "password_update";
		}
		System.out.println(registerDTO.getPasswd());
		
		// 入力された新規パスワードをハッシュ化
		String newPasswordHash = signUpService.passwordEncoder().encode(registerDTO.getPasswd());
		
		// DB(user) のログイン者のパスワードを入力された新規パスワードに更新する
		signUpRepository.updateByUid(newPasswordHash, (String)session.getAttribute("myUid"));
		// DB(password_reset)のログイン者の uid を持つレコードを削除する（パスワード変更のフラグのやつ）
		adminPasswordResetRepository.deleteByUid((String)session.getAttribute("myUid"));
		return "password_update_result";
	}
	
	@GetMapping("/access-denied")
	public String errorAdmin() {
		return "errorAdmin";
	}
	
	@GetMapping("/websocket")
	public String websocket() {
		return "websocket";
	}

}
