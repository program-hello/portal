package com.example.demo.controller;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.bean.AttendanceHolidayRequestDTO;
import com.example.demo.bean.AttendanceHolidayRequestsEntity;
import com.example.demo.bean.AttendanceRecordsDTO;
import com.example.demo.bean.AttendanceRecordsEntity;
import com.example.demo.bean.AttendanceRequestsEntity;
import com.example.demo.repository.AttendanceHolidayRequestsRepository;
import com.example.demo.repository.AttendanceRecordsRepository;
import com.example.demo.repository.AttendanceRequestsRepository;
import com.example.demo.service.AttendanceRecordsService;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class AttendanceController {
	
	private final AttendanceRecordsRepository attendanceRecordsRepository;
	private final AttendanceRequestsRepository attendanceRequestsRepository;
	private final AttendanceHolidayRequestsRepository attendanceHolidayRequestsRepository;
	private final AttendanceRecordsService attendanceRecordsService;
	private final HttpSession session;
	
	

	
	@GetMapping("/attendance/redirect")
	public String attendance_redirect() {
		return "attendance_home";
	}
	// 勤怠管理ホームへの遷移
	@GetMapping("/attendance")
	public String attendance_home() {
		session.setAttribute("greeting","お疲れ様です！！<br>各種操作を選んでください");	// 簡単な挨拶メッセージ
		session.setAttribute("clockOutTime", null);
		session.setAttribute("clockOutTime", null);
		
		// 出勤、退勤の状態を検証して session に状態を保存
		attendanceRecordsService.verifyStartEnd();

		return "redirect:/attendance/redirect";
	}	
	// 出勤ボタンからの遷移
	@GetMapping("/attendance/start")
	public String attendance_start() {
		// ログイン者の serial_id を取得
		int serial_id = (int)session.getAttribute("mySerial_id");
		// 現在時刻の取得
		LocalDate nowDate = LocalDate.now();	// 現在の日時(Date)を取得（勤務日）
	     LocalDateTime nowDateTime = LocalDateTime.now();  // 現在の日時(DateTime)を取得（出勤時間）
	     // 日付と時刻のフォーマットを指定（例: yyyy-MM-dd HH:mm:ss:SS）
	     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	     String formattedDate = nowDateTime.format(formatter);
	      
	     // DB(attendace_records)にデータを登録
	      AttendanceRecordsEntity attendanceEntity = new AttendanceRecordsEntity();
	      attendanceEntity.setSerialId(serial_id);
	      attendanceEntity.setWorkDate(nowDate);
	      attendanceEntity.setClockInTime(nowDateTime);
	      attendanceRecordsRepository.save(attendanceEntity);
	      
	      // DB(attendance_requests)にデータを登録
	      AttendanceRequestsEntity attendanceRequestsEntity = new AttendanceRequestsEntity();
	      attendanceRequestsEntity.setSerialId(serial_id);
	      attendanceRequestsEntity.setWorkDate(nowDate);
	      if(nowDateTime.getHour() >= 9 && nowDateTime.getMinute() > 0) {	// 遅刻を status に反映（9時以降に出社で遅刻）
	    	  attendanceRequestsEntity.setStatus(1);
	      }
	      attendanceRequestsRepository.save(attendanceRequestsEntity);
	      
	      session.setAttribute("greeting","出勤ご苦労様です<br>今日も頑張っていきましょう！！");	// 簡単な挨拶メッセージ
	      
			// 出勤、退勤の状態を検証して session に状態を保存
			attendanceRecordsService.verifyStartEnd();
			
	      return "redirect:/attendance/redirect";
	}
	// 退勤ボタンからの遷移
	@GetMapping("/attendance/end")
	public String attendance_end() {
		
		// 現在時刻の取得
		LocalDate nowDate = LocalDate.now();	// 現在の日時(Date)を取得（勤務日）
	     LocalDateTime nowDateTime = LocalDateTime.now();  // 現在の日時(DateTime)を取得（退勤時間）
		
		// DBに保存したデータの退勤時刻を変更する
		attendanceRecordsRepository.updateClockOutTimeById(
				(int)session.getAttribute("mySerial_id"), nowDateTime, nowDate);
		
		session.setAttribute("greeting","お仕事お疲れさまでした！！<br>お気を付けてお帰りください");	// 簡単な挨拶メッセージ
		
		// ログイン者の出勤、退勤の状態を検証して session に状態を保存
		attendanceRecordsService.verifyStartEnd();
		// DBのログイン者の当日のデータの当日の実働時間を更新
		attendanceRecordsService.actualworkMinutes();
		
		return "redirect:/attendance/redirect";
	}
	
	// 申請ボタンからの遷移
	@GetMapping("/attendance/request")
	public String attendance_request(Model model) {
		model.addAttribute("attendanceRecordsDTO", new AttendanceRecordsDTO());
		model.addAttribute("currentYear", LocalDate.now().getYear());
		
		return "attendance_request";
	}
	// 申請フォームからの遷移
	@PostMapping("/attendance/request/post")
	public String attendance_request(@Validated @ModelAttribute AttendanceRecordsDTO attendanceRecordsDTO, BindingResult result, Model model) {
		
		// 存在しない日付（2月31日など）が入力されていたらエラー処理を行う
        try {
        	attendanceRecordsDTO.setWorkDate(LocalDate.of(attendanceRecordsDTO.getYear(), attendanceRecordsDTO.getMonth(), attendanceRecordsDTO.getDay()));
        } catch (DateTimeException e) {
            // フィールドにエラーを追加する
            FieldError fieldError = new FieldError("attendanceRecordsDTO", "workDate", "※不正な日付です");
            result.addError(fieldError);
        }
        // DBに登録されていない status が選択されていたらエラーフィールドを追加して html に戻す
        int flag = 0;
        Integer[] statusArr = attendanceRequestsRepository.findStatusBySerialIdAndWorkDate(
        		(int)session.getAttribute("mySerial_id"),
        		attendanceRecordsDTO.getWorkDate());
        for(Integer n : statusArr) {
        	if(n == attendanceRecordsDTO.getStatus()) flag = 1;
        }
        if(flag == 0) {	// DBから取得した status の配列にフォームで選択した項目が含まれていない
            // フィールドにエラーを追加する
            FieldError fieldError = new FieldError("attendanceRecordsDTO", "status", "※正しい項目を選択してください");
            result.addError(fieldError);
        }
        
		// バリデーションチェック
        if(result.hasErrors()) {
        	// 必要なデータを html に渡す
        	model.addAttribute("attendanceRecordsDTO", attendanceRecordsDTO);
        	model.addAttribute("currentYear", LocalDate.now().getYear());
        	return "attendance_request";
        }
        
        // 申請内容で attendance_requests テーブルのデータを更新する
        attendanceRequestsRepository.updateRemarksByIdANDWorkDate(
        		(int)session.getAttribute("mySerial_id"),
        		attendanceRecordsDTO.getWorkDate(),
        		attendanceRecordsDTO.getStatus(),
        		attendanceRecordsDTO.getRemarks()
        		);
        
        // 必要なデータを html に渡す
		model.addAttribute("attendanceRecordsDTO", new AttendanceRecordsDTO());
		model.addAttribute("currentYear", LocalDate.now().getYear());
        
		return "attendance_request";
	}
	
	// 入力した日付の status をDBから検索する
	@GetMapping("/attendance/request/serch")
	public String attendance_request_serch(
			@Validated 
			@ModelAttribute AttendanceRecordsDTO attendanceRecordsDTO, 
			BindingResult result,
		    @RequestParam("year") int year,
		    @RequestParam("month") int month,
		    @RequestParam("day") int day,
		    Model model) {
				

		LocalDate workDate;
		// 存在しない日付（2月31日など）が入力されていたらエラー処理を行う
        try {
    		// 入力内容で LocalDate を組み立てる		
    		workDate = LocalDate.of(year, month, day);
        	} catch (DateTimeException e) {
            // フィールドにエラーを追加する
            FieldError fieldError = new FieldError("attendanceRecordsDTO", "workDate", "※不正な日付です");
            result.addError(fieldError);
    		model.addAttribute("attendanceRecordsDTO", attendanceRecordsDTO);
    		model.addAttribute("currentYear", LocalDate.now().getYear());
            return "attendance_request";
        }	
		
		
		// 入力した年月日の status をDBから検索して、それに対する状態を html に渡す
		String statusStr = "";
		Integer[] statusArr = attendanceRequestsRepository.findStatusBySerialIdAndWorkDate((int)session.getAttribute("mySerial_id"),workDate);
		// status が登録されていない日付を入力した場合 "null" を文字列として html に返す 
		if(statusArr == null || statusArr.length == 0) statusStr = "null";
		// 複数件登録されている status を一つの文字列にまとめる
		for(Integer n: statusArr) {
			switch(n) {
			case -1:
				statusStr = "null";
				break;
			case 0:
				statusStr += "通常出勤";
				break;
			case 1:
				statusStr += "遅刻　";
				break;
			case 2:
				statusStr += "早退　";
				break;
			case 3:
				statusStr += "出張　";
				break;
			case 4:
				statusStr += "欠席　";
				break;
			case 5:
				statusStr += "有給　";
				break;
			}
		}

		// 入力内容を attendanceRecordsDTO にセットして html に渡す
		// その他必要なデータも渡す
		AttendanceRecordsDTO attendanceRecordsDTO2 = new AttendanceRecordsDTO();
		attendanceRecordsDTO2.setYear(year);
		attendanceRecordsDTO2.setMonth(month);
		attendanceRecordsDTO2.setDay(day);
		model.addAttribute("statusStr",statusStr);
		model.addAttribute("attendanceRecordsDTO", attendanceRecordsDTO2);
		model.addAttribute("currentYear", LocalDate.now().getYear());
		
		return "attendance_request";
	}
	
	// 休暇申請ボタンからの遷移
	@GetMapping("/attendance/holiday/request")
		public String attendance_holiday_request(Model model) {
		// 必要な情報を html に渡す
		model.addAttribute("attendanceHolidayRequestDTO", new AttendanceHolidayRequestDTO());
		model.addAttribute("currentYear", LocalDate.now().getYear());
			return "attendance_holiday_request";
		}
	// 休暇申請内の送信ボタンからの遷移
		@PostMapping("/attendance/holiday/request/post")
		public String attendance_holiday_request_post(
				@Validated 
				@ModelAttribute AttendanceHolidayRequestDTO attendanceHolidayRequestDTO, 
				BindingResult result,
			    Model model) {

			// 存在しない日付（2月31日など）が入力されていたらエラー処理を行う
	        try {
	            // 入力されたデータを LocalDate に変換
	            attendanceHolidayRequestDTO.convertToHolidayDates();
	            
	            // 日付が選択されていなければバリデーションエラーを追加
	            if(attendanceHolidayRequestDTO.getHolidayDates().size() == 0) {
	            	System.out.println("koko");
                    FieldError fieldError = new FieldError(
	                        "attendanceHolidayRequestDTO",
	                        "holidayDate",
	                        "※日付を選択してください");
	                    result.addError(fieldError);
	            }
	            
	            // 入力された日付がすでにDB(attendance_holiday_requests)に登録されているか検索して
	            // 登録されている場合、DBへの登録は拒否する
	            for(LocalDate date : attendanceHolidayRequestDTO.getHolidayDates()) {
		            Integer existing =  attendanceHolidayRequestsRepository.existWorkDate(date, (int)session.getAttribute("mySerial_id"));
		            if (existing > 0) {
	                    FieldError fieldError = new FieldError(
		                        "attendanceHolidayRequestDTO",
		                        "holidayDate",
		                        "※申請済みの日付が含まれています");
		                    result.addError(fieldError);
		            }
	            }
	            
	            // 未来日チェック　未来日が選択されていた場合、バリデーションエラーを追加
	            LocalDate today = LocalDate.now();
	            for (LocalDate date : attendanceHolidayRequestDTO.getHolidayDates()) {
	                if (date.isBefore(today)) {
	                    FieldError fieldError = new FieldError(
	                        "attendanceHolidayRequestDTO",
	                        "holidayDate",
	                        "※過去の日付が含まれています");
	                    result.addError(fieldError);
	                    model.addAttribute("attendanceHolidayRequestDTO", attendanceHolidayRequestDTO);
	                    model.addAttribute("currentYear", LocalDate.now().getYear());
	                    return "attendance_holiday_request";
	                }
	            }
	        	} catch (DateTimeException e) {
	            // フィールドにエラーを追加する
	            FieldError fieldError = new FieldError("attendanceHolidayRequestDTO", "holidayDate", "※不正な日付が含まれています");
	            result.addError(fieldError);
	    		model.addAttribute("attendanceHolidayRequestDTO", attendanceHolidayRequestDTO);
	    		model.addAttribute("currentYear", LocalDate.now().getYear());
	            return "attendance_holiday_request";
	        }	
			
			// バリデーションチェック
	        if(result.hasErrors()) {
	        	// 必要なデータを html に渡す
	        	model.addAttribute("attendanceHolidayRequestDTO", attendanceHolidayRequestDTO);
	        	model.addAttribute("currentYear", LocalDate.now().getYear());
	        	return "attendance_holiday_request";
	        }
	        
	        // 入力した日付の分だけ、DB(attendance_holiday_requests)に保存
	        for(int i=0; i<attendanceHolidayRequestDTO.getHolidayDates().size(); i++) {
	        	AttendanceHolidayRequestsEntity attendanceHolidayRequestEntity = new AttendanceHolidayRequestsEntity();
	        	attendanceHolidayRequestEntity.setSerialId((int)session.getAttribute("mySerial_id"));
	        	attendanceHolidayRequestEntity.setHolidayDate(attendanceHolidayRequestDTO.getHolidayDates().get(i));
	        	attendanceHolidayRequestEntity.setStatus(0);
	        	attendanceHolidayRequestEntity.setRemarks(attendanceHolidayRequestDTO.getRemarks());
	        	attendanceHolidayRequestsRepository.save(attendanceHolidayRequestEntity);
	        }
			
        	// 必要なデータを html に渡す
        	model.addAttribute("attendanceHolidayRequestDTO", attendanceHolidayRequestDTO);
        	model.addAttribute("currentYear", LocalDate.now().getYear());
			return "attendance_holiday_request";
		}
		
		// 履歴ボタンからの遷移
		@GetMapping("/attendance/history")
		public String attendance_history(@PageableDefault(page = 0, size = 10, sort = "workDate", direction = Sort.Direction.DESC) Pageable pageable, Model model) {
			// page : 最初に表示するページ数（０始まり）　 size : １ページに表示するデータの数
			// sort : ソートの際に基準とするEntityクラスのプロパティ名

			// DB(attendance_records テーブル)からログイン者の serial_id で Pageable で全件取得する
			Page<AttendanceRecordsEntity> attendanceHistoryPage = attendanceRecordsRepository.findBySerialId((int)session.getAttribute("mySerial_id"),pageable);					
			model.addAttribute("pageAll", attendanceHistoryPage);
			
			return "attendance_history";
		}
}
