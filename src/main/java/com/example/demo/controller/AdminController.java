package com.example.demo.controller;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.example.demo.bean.AdminAddUidDTO;
import com.example.demo.bean.AdminChangeStatusDTO;
import com.example.demo.bean.AdminPasswordResetEntity;
import com.example.demo.bean.AdminRegisterStatusDTO;
import com.example.demo.bean.AdminRequestApprovalDTO;
import com.example.demo.bean.AttendanceHolidayRequestsEntity;
import com.example.demo.bean.AttendanceRequestsEntity;
import com.example.demo.bean.RegisterDTO;
import com.example.demo.bean.RegisterEntity;
import com.example.demo.bean.RegisterEntity2;
import com.example.demo.repository.AdminPasswordResetRepository;
import com.example.demo.repository.AttendanceHolidayRequestsRepository;
import com.example.demo.repository.AttendanceRequestsRepository;
import com.example.demo.repository.SignUpRepository;
import com.example.demo.service.SignUpService;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
@SessionAttributes("adminRequestApprovalDTO")
public class AdminController {
	private final SignUpRepository signUpRepository;
	private final AttendanceRequestsRepository attendanceRequestsRepository;
	private final AttendanceHolidayRequestsRepository attendanceHolidayRequestsRepository;
	private final AdminPasswordResetRepository adminPasswordResetRepository;
	private final SignUpService signUpService;
	private final HttpSession session;
	
	
	// 管理者画面ホーム
	@GetMapping("/admin")
	public String admin() {
		return "admin_home";
	}
	
	@GetMapping("/admin/delete/user")
	public String delete(Model model) {
		model.addAttribute("registerDTO",new RegisterDTO());
		return "admin_delete_user";
	}
	// ユーザー削除
	@PostMapping("/admin/delete/user/post")
	public String deletePost(@Validated @ModelAttribute RegisterDTO registerDTO, BindingResult result, Model model) {
		// バリデーションチェック
		if(result.hasErrors()) {
			model.addAttribute("registerDTO",registerDTO);
			return "admin_delete_user";
		}
		
		if(signUpService.deleteUser(registerDTO.getUid())) {
			// 削除成功
			return "admin_delete_result";
		} else {
			// 削除失敗
			result.rejectValue("uid", null, "※DBに登録されていません");
	  		model.addAttribute("registerDTO",registerDTO);
			return "admin_delete_user";
		}
	}
	
	@GetMapping("/admin/register/user")
	public String register(Model model) {
		model.addAttribute("registerDTO", new RegisterEntity());
		return "admin_register_user";
	}
	@PostMapping("/admin/register/user/post")
	public String registerPost(@Validated @ModelAttribute RegisterDTO registerDTO, BindingResult result, Model model) {
		
		// バリデーションチェック
		if(result.hasErrors()) {
			model.addAttribute("registerDTO",registerDTO);
			return "admin_register_user";
		}
		
		// DBにフォームからの入力でuid（主キー）がDBに登録されていなければDBに登録する
		if(signUpService.saveUser(registerDTO.getUid(), registerDTO.getPasswd(), registerDTO.getRole())) {
			return "admin_register_result";
		} else {
			// 入力された uid（主キー）がすでに登録されていたらエラー情報を追加してフォーム画面に戻す
			result.rejectValue("uid", null, "※すでに登録されています");	// ここ調べる！！
	  		model.addAttribute("registerDTO",registerDTO);
			return "admin_register_user";
		}
	}
	
	// 各種申請ボタンからの遷移
	@GetMapping("/admin/request/approval")
	public String admin_request_approval(Model model) {
		
		// DB(user)から uid のリストを取得
		List<RegisterEntity2> uidList = signUpRepository.findAllByOrderByUidAsc();
		
		// 必要なデータを html に渡す
		model.addAttribute("adminRequestApprovalDTO", new AdminRequestApprovalDTO());
		model.addAttribute("uidList", uidList);
    	model.addAttribute("currentYear", LocalDate.now().getYear());
		
		return "admin_request_approval";
	}
	
	// 検索ボタンからの遷移
	@PostMapping("/admin/request/approval/search")
	public String admin_request_approval_search(@ModelAttribute AdminRequestApprovalDTO adminRequestApprovalDTO,  Model model) {
		List<AttendanceRequestsEntity> resultRecordsList = null;		// DB(attendance_requests)からデータを取得したリスト
		List<AdminChangeStatusDTO> resultRecordsChangeStatusList = new ArrayList<>();	// AttendanceRequestsEntity の status を文字列に変更したリスト
		List<AdminAddUidDTO> resultRecordsAdminAddUidList = new ArrayList<>();				// AttendanceRequestsEntityにuidを追加したリスト
		List<AttendanceHolidayRequestsEntity> resultRecordsHolidayList = new ArrayList<>();	// DB(attendance_holiday_requests)からデータを取得したリスト
		// 入力された uid で serial_id を取得
		Integer serialId = signUpRepository.findSeirilIdByUid(adminRequestApprovalDTO.getUid());	
		session.setAttribute("isAllCheck", adminRequestApprovalDTO.getIsAllCheck());
		
		// 全てにチェックがついていない
		// 休暇申請チェックがついていない → 遅刻早退などのリストを表示
		if(adminRequestApprovalDTO.getIsHolidayCheck() == 0 && adminRequestApprovalDTO.getIsAllCheck() == 0) {
			// 検索窓でユーザーIDが ALL が選択されているかの判定
			if(adminRequestApprovalDTO.getUid().equals("all")) {
				resultRecordsList = attendanceRequestsRepository.findAllByYearAndMonthOrderByWorkDateDesc(
						adminRequestApprovalDTO.getYear(),
						adminRequestApprovalDTO.getMonth());
			} else {
				//DB(attendance_records)から serial_id, year, month で取得
				resultRecordsList = attendanceRequestsRepository.findAllBySerialIdAndYearAndMonthOrderByWorkDateDesc(
						serialId,
						adminRequestApprovalDTO.getYear(),
						adminRequestApprovalDTO.getMonth());
			}
			
			// status を文字列に変換するために、AttendanceRequestsEntity から AdminChangeStatusDTO に換装する
			for(AttendanceRequestsEntity a : resultRecordsList) {
				AdminChangeStatusDTO acs = new AdminChangeStatusDTO();
				acs.setSerialId(a.getSerialId());
				acs.setUid(signUpRepository.findUidBySerialId(acs.getSerialId()));
				acs.setWorkDate(a.getWorkDate());
				acs.setRemarks(a.getRemarks());
				acs.setStatus(a.getStatus());
				acs.changeStatus();		// status を文字列に変換するメソッド
				acs.checkRemarks();		// remarks が入力済みか判定する
				resultRecordsChangeStatusList.add(acs);
			}
		} else if(adminRequestApprovalDTO.getIsHolidayCheck() == 1 && adminRequestApprovalDTO.getIsAllCheck() == 0){		// 休暇申請チェックがついている → 休暇申請リストを表示
			// 検索窓でユーザーIDが ALL が選択されているかの判定
			if(adminRequestApprovalDTO.getUid().equals("all")) {
				resultRecordsHolidayList = attendanceHolidayRequestsRepository.findAllByYearAndMonth(
						adminRequestApprovalDTO.getYear(),
						adminRequestApprovalDTO.getMonth());
			} else {
				// 入力された uid, year, month でDB(attendance_holiday_requests)からレコードを取得
				resultRecordsHolidayList = attendanceHolidayRequestsRepository.findAllBySerialId(
						serialId,
						adminRequestApprovalDTO.getYear(),
						adminRequestApprovalDTO.getMonth());
			}
			// uidをセットするために、AttendanceHolidayRequestsEntity から AdminAddUidDTO に換装する
			for(AttendanceHolidayRequestsEntity a : resultRecordsHolidayList) {
				AdminAddUidDTO acs = new AdminAddUidDTO();
				acs.setSerialId(a.getSerialId());
				acs.setUid(signUpRepository.findUidBySerialId(acs.getSerialId()));
				acs.setHolidayDate(a.getHolidayDate());
				acs.setRemarks(a.getRemarks());
				acs.setStatus(a.getStatus());
				resultRecordsAdminAddUidList.add(acs);
			}
		}
		
		// [全て] にチェックがついている
		// 休暇申請にチェックがついている
		if(adminRequestApprovalDTO.getIsHolidayCheck() == 0 && adminRequestApprovalDTO.getIsAllCheck() == 1) {
			
			// 検索窓でユーザーIDが ALL が選択されているかの判定
			if(adminRequestApprovalDTO.getUid().equals("all")) {
				resultRecordsList = attendanceRequestsRepository.findAllByOrderByWorkDateDesc();
			} else {
				//DB(attendance_records)から serial_id で全件取得
				resultRecordsList = attendanceRequestsRepository.findBySerialIdOrderByWorkDateDesc(serialId);
			}
//			resultRecordsList = attendanceRequestsRepository.findBySerialIdOrderByWorkDateDesc(serialId);				
			
			// status を文字列に変換するために、AttendanceRequestsEntity から AdminChangeStatusDTO に換装する
			for(AttendanceRequestsEntity a : resultRecordsList) {
				AdminChangeStatusDTO acs = new AdminChangeStatusDTO();
				acs.setSerialId(a.getSerialId());
				acs.setUid(signUpRepository.findUidBySerialId(acs.getSerialId()));
				acs.setWorkDate(a.getWorkDate());
				acs.setRemarks(a.getRemarks());
				acs.setStatus(a.getStatus());
				acs.changeStatus();		// status を文字列に変換するメソッド
				acs.checkRemarks();		// remarks が入力済みか判定する
				resultRecordsChangeStatusList.add(acs);
			}
		} else if(adminRequestApprovalDTO.getIsHolidayCheck() == 1 && adminRequestApprovalDTO.getIsAllCheck() == 1){		// 休暇申請チェックがついている → 休暇申請リストを表示
			// 検索窓でユーザーIDが ALL が選択されているかの判定
			if(adminRequestApprovalDTO.getUid().equals("all")) {
				// DB(attendance_holiday_requests)から全件取得 holidayDate で降順ソート
				resultRecordsHolidayList = attendanceHolidayRequestsRepository.findAllByOrderByHolidayDateDesc();
			} else {
				// 入力された serialId でDB(attendance_holiday_requests)からレコードを取得
				resultRecordsHolidayList = attendanceHolidayRequestsRepository.findBySerialIdOrderByHolidayDateDesc(serialId);
			}
			// uidをセットするために、AttendanceHolidayRequestsEntity から AdminAddUidDTO に換装する
			for(AttendanceHolidayRequestsEntity a : resultRecordsHolidayList) {
				AdminAddUidDTO acs = new AdminAddUidDTO();
				acs.setSerialId(a.getSerialId());
				acs.setUid(signUpRepository.findUidBySerialId(acs.getSerialId()));
				acs.setHolidayDate(a.getHolidayDate());
				acs.setRemarks(a.getRemarks());
				acs.setStatus(a.getStatus());
				resultRecordsAdminAddUidList.add(acs);
			}
		}
				
		// DB(user)から uid のリストを取得
		List<RegisterEntity2> uidList = signUpRepository.findAllByOrderByUidAsc();
		// 必要なデータを html に渡す
		model.addAttribute("uidList", uidList);		// ユーザー一覧
    	model.addAttribute("currentYear", LocalDate.now().getYear());
    	// @PostMapping("/admin/request/approval/post") で使うためにセッションに保存して Model に渡す
    	// th:object で ${session.~~} の記述が許されていないため
		session.setAttribute("adminRequestApprovalDTO", adminRequestApprovalDTO);
		session.setAttribute("resultRecordsChangeStatusList", resultRecordsChangeStatusList);		// AttendanceRequestsEntity の status を文字列に変更したリスト
		session.setAttribute("resultRecordsAdminAddUidList", resultRecordsAdminAddUidList);		// AttendanceRequestsEntityにuidを追加したリスト
		model.addAttribute("adminRequestApprovalDTO", adminRequestApprovalDTO);
		model.addAttribute("resultRecordsChangeStatusList", resultRecordsChangeStatusList);		// AttendanceRequestsEntity の status を文字列に変更したリスト
		model.addAttribute("resultRecordsAdminAddUidList", resultRecordsAdminAddUidList);		// AttendanceRequestsEntityにuidを追加したリスト
		model.addAttribute("resultRecordsHolidayList", resultRecordsHolidayList);		// 休暇申請リスト
		
		return "admin_request_approval";
	}
	
	@PostMapping("/admin/request/approval/post")
	public String admin_request_approval_post(
			@RequestParam("serialId") int serialId,
			@RequestParam("holidayDate") LocalDate holidayDate,
			Model model){
		
		List<AttendanceHolidayRequestsEntity> resultRecordsHolidayList = new ArrayList<>();
		List<AdminAddUidDTO> resultRecordsAdminAddUidList = new ArrayList<>();				// AttendanceRequestsEntityにuidを追加したリスト
		
		
		// 承認ボタンで status の値を１に変更する
		attendanceHolidayRequestsRepository.updateStatusByIdAndHolidayDate(
				serialId, holidayDate);
		
		// ユーザーIDが ALL である
		if(((AdminRequestApprovalDTO)session.getAttribute("adminRequestApprovalDTO")).getUid().equals("all")) {
			// 更新されたDB(attendance_holiday_requests)のデータを取得する
			if((int)session.getAttribute("isAllCheck") == 1) {	// 全てにチェックが入っている
				resultRecordsHolidayList = attendanceHolidayRequestsRepository.findAllByOrderByHolidayDateDesc();
			} else {
				resultRecordsHolidayList = attendanceHolidayRequestsRepository.findAllByYearAndMonth(
						((AdminRequestApprovalDTO)session.getAttribute("adminRequestApprovalDTO")).getYear(),
						((AdminRequestApprovalDTO)session.getAttribute("adminRequestApprovalDTO")).getMonth());
			}
		} else {		// ユーザーIDが ALL ではない
			// 更新されたDB(attendance_holiday_requests)のデータを取得する
			if((int)session.getAttribute("isAllCheck") == 1) {	// 全てにチェックが入っている
				resultRecordsHolidayList = attendanceHolidayRequestsRepository.findBySerialIdOrderByHolidayDateDesc(serialId);
			} else {
				// 入力された uid, year, month でDB(attendance_holiday_requests)からレコードを取得
				resultRecordsHolidayList = attendanceHolidayRequestsRepository.findAllBySerialId(
						serialId,
						((AdminRequestApprovalDTO)session.getAttribute("adminRequestApprovalDTO")).getYear(),
						((AdminRequestApprovalDTO)session.getAttribute("adminRequestApprovalDTO")).getMonth());
			}
		}

		// uidをセットするために、AttendanceHolidayRequestsEntity から AdminAddUidDTO に換装する
		for(AttendanceHolidayRequestsEntity a : resultRecordsHolidayList) {
			AdminAddUidDTO acs = new AdminAddUidDTO();
			acs.setSerialId(a.getSerialId());
			acs.setUid(signUpRepository.findUidBySerialId(acs.getSerialId()));
			acs.setHolidayDate(a.getHolidayDate());
			acs.setRemarks(a.getRemarks());
			acs.setStatus(a.getStatus());
			resultRecordsAdminAddUidList.add(acs);
		}
		
		// DB(user)から uid のリストを取得
		List<RegisterEntity2> uidList = signUpRepository.findAllByOrderByUidAsc();
		// 必要なデータを html に渡す
		model.addAttribute("uidList", uidList);		// ユーザー一覧
    	model.addAttribute("currentYear", LocalDate.now().getYear());
		model.addAttribute("adminRequestApprovalDTO", session.getAttribute("adminRequestApprovalDTO"));
		model.addAttribute("resultRecordsChangeStatusList", session.getAttribute("resultRecordsChangeStatusList"));		// AttendanceRequestsEntity の status を文字列に変更したリスト
		model.addAttribute("resultRecordsAdminAddUidList", resultRecordsAdminAddUidList);		// AttendanceRequestsEntityにuidを追加したリスト
		session.setAttribute("resultRecordsAdminAddUidList", resultRecordsAdminAddUidList);		// AttendanceRequestsEntityにuidを追加したリスト
		model.addAttribute("resultRecordsHolidayList", resultRecordsHolidayList);		// 休暇申請リスト
		return "admin_request_approval";
	}
	
	// 管理者画面のパスワードリセットボタンからの遷移
	@GetMapping("/admin/reset_password")
	public String admin_reset_password(Model model) {
		model.addAttribute("registerDTO",new RegisterDTO());
		return "admin_reset_password";
	}
	@PostMapping("/admin/reset/password/post")
	public String asmin_reset_password_post(@Validated @ModelAttribute RegisterDTO registerDTO, BindingResult result, Model model) {
		// バリデーションチェック
		if(result.hasErrors()) {
			model.addAttribute("registerDTO",registerDTO);
			return "admin_delete_user";
		}
		
		// 仮のパスワードを乱数で発行
		Random random = new Random();
		Integer ranNum = 100000 + random.nextInt(999999);
		String ranNumStr = Integer.toString(ranNum);	// 数値を文字列に変換
		
		if(signUpRepository.updateByUid(signUpService.passwordEncoder().encode(ranNumStr), registerDTO.getUid()) == 1) {
			// パスワード更新成功
			model.addAttribute("ranNumStr", ranNumStr);
			// 該当ユーザーの uid で DB(password_reset)にフラグを立てて保存する
			AdminPasswordResetEntity adminPasswordResetEntity = new AdminPasswordResetEntity();
			adminPasswordResetEntity.setUid(registerDTO.getUid());
			adminPasswordResetEntity.setPasswordResetFlag(1);
			adminPasswordResetRepository.save(adminPasswordResetEntity);					
			return "admin_reset_password_result";
		} else {
			// パスワード更新失敗
			result.rejectValue("uid", null, "※DBに登録されていません");
			model.addAttribute("registerDTO",registerDTO);
			return "admin_reset_password";
		}
	}
	
	// ステータス登録ボタンからの遷移
	@GetMapping("/admin/register/status")
	public String admin_register_status(Model model) {
		// DB(user)から uid のリストを取得
		List<RegisterEntity2> uidList = signUpRepository.findAllByOrderByUidAsc();
		// 必要なデータを html に渡す
		model.addAttribute("uidList", uidList);		// ユーザー一覧
		model.addAttribute("currentYear", LocalDate.now().getYear());	// 現在の年
		model.addAttribute("adminRegisterStatusDTO", new AdminRegisterStatusDTO());	// 現在の年
		return "admin_register_status";
	}
	
	// ステータス登録の送信からの遷移
	@PostMapping("/admin/register/status/post")
	public String admin_register_status_post(@Validated @ModelAttribute AdminRegisterStatusDTO adminRegisterStatusDTO, BindingResult result, Model model) {
		// 存在しない日付（2月31日など）が入力されていたらエラー処理を行う
        try {
        	adminRegisterStatusDTO.setWorkDate(LocalDate.of(adminRegisterStatusDTO.getYear(), adminRegisterStatusDTO.getMonth(), adminRegisterStatusDTO.getDay()));
        } catch (DateTimeException e) {
            // フィールドにエラーを追加する
            FieldError fieldError = new FieldError("attendanceRecordsDTO", "workDate", "※不正な日付です");
            result.addError(fieldError);
        }
		// バリデーションチェック
        if(result.hasErrors()) {
        	// 必要なデータを html に渡す
        	// DB(user)から uid のリストを取得
    		List<RegisterEntity2> uidList = signUpRepository.findAllByOrderByUidAsc();
    		// 必要なデータを html に渡す
    		model.addAttribute("uidList", uidList);		// ユーザー一覧
        	model.addAttribute("attendanceRecordsDTO", adminRegisterStatusDTO);
        	model.addAttribute("currentYear", LocalDate.now().getYear());
        	return "admin_register_status";
        }
        // 入力された uid で serial_id を取得
     	Integer serialId = signUpRepository.findSeirilIdByUid(adminRegisterStatusDTO.getUid());	
        // Entity に値をセットしてDB(attendance_requests)に保存
        AttendanceRequestsEntity attendanceRequestsEntity = new AttendanceRequestsEntity();
        attendanceRequestsEntity.setStatus(adminRegisterStatusDTO.getStatus());
        attendanceRequestsEntity.setWorkDate(adminRegisterStatusDTO.getWorkDate());
        attendanceRequestsEntity.setSerialId(serialId);
        attendanceRequestsEntity.setRemarks(null);
        attendanceRequestsRepository.save(attendanceRequestsEntity);
        
		return "admin_register_status_result";
	}
}
