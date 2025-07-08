package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.example.demo.bean.AttendanceRequestsEntity;
import com.example.demo.repository.AttendanceRecordsRepository;
import com.example.demo.repository.AttendanceRequestsRepository;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AttendanceRecordsService {
	
	private final AttendanceRecordsRepository attendanceRecordsRepository;
	private final AttendanceRequestsRepository attendanceRequestsRepository;
	private final HttpSession session;
	
	
	// 出勤、退勤の状態を検証して session に状態を保存
	public void verifyStartEnd() {
		// 出勤、退勤ボタンを無効化するために、当日の勤怠管理DBのデータを検証する
		// clock_in_time が非NULL = 出勤ボタンを無効化
		LocalDateTime verifyClockInTime = attendanceRecordsRepository.findByTodayClockInTime(
				(int)session.getAttribute("mySerial_id"), LocalDate.now());
		session.setAttribute("verifyClockInTime", verifyClockInTime);
		
		// clock_out_time が非NULL = 退勤ボタンの無効化
		LocalDateTime verifyClockOutTime = attendanceRecordsRepository.findByTodayClockOutTime(
				(int)session.getAttribute("mySerial_id"), LocalDate.now());
		session.setAttribute("verifyClockOutTime", verifyClockOutTime);
		
		// 当日の LocalDateをsessionに保存
		LocalDate todayLocalDate = LocalDate.now();
		session.setAttribute("todayLocalDate", todayLocalDate);
		// 当日の曜日を session に保存
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E", Locale.JAPANESE);	// 曜日を日本語に変換
		String dayOfWeek = "(" + todayLocalDate.format(formatter) + ")";
		session.setAttribute("dayOfWeek", dayOfWeek);
		
		// 勤怠管理ホーム画面のために hh:mm に整形した LocalDateTime を session に保存する

	      // 日付と時刻のフォーマットを指定（例: yyyy-MM-dd HH:mm:ss:SS）
	      formatter = DateTimeFormatter.ofPattern("HH時mm分");
	      if(verifyClockInTime != null) {
		      String formattedDateClockInTime = verifyClockInTime.format(formatter);	      
		      session.setAttribute("clockInTime", formattedDateClockInTime);      
	      }
	      if(verifyClockOutTime != null) {
	    	  String formattedDateClockOutTime = verifyClockOutTime.format(formatter);
	    	  session.setAttribute("clockOutTime", formattedDateClockOutTime);
	      }


	}
	
	// 当日の実働時間をDBに保存（退勤ボタンの処理の中からの遷移）
	public void actualworkMinutes() {
		// 出勤、退勤時間の取得
		LocalDateTime clockInTime = (LocalDateTime)session.getAttribute("verifyClockInTime");
		LocalDateTime clockOutTime = (LocalDateTime)session.getAttribute("verifyClockOutTime");
		
		int status = 0;
		int startTimeHour = clockInTime.getHour();
		int startTimeMinute = clockInTime.getMinute();
		int endTimeHour = clockOutTime.getHour();
		int endTimeMinute = clockOutTime.getMinute();
		int workMinutes = 0;
		
		// 9時～１８時勤務を想定
		// 9時前に出勤登録した場合、出勤時間を９時に合わせ、status を 0:通常出勤 に変更
		// 9時以降に出勤登録した場合、出勤時間はそのままで、status を 1:遅刻 に変更
		if(startTimeHour < 9) {	// 通常出勤
			startTimeHour = 9;
			startTimeMinute = 0;	// 9時ちょうどに設定
		}
		if(startTimeHour >= 9 && startTimeMinute > 0) status = 1;	// 遅刻
		if(endTimeHour < 18) status = 2;	// 早退
		
		// 実働時間を分単位で計算
		if(startTimeHour == endTimeHour) workMinutes = endTimeMinute - startTimeMinute;
		else {
			workMinutes += (endTimeHour - startTimeHour) * 60;			// 時間を分に変換
			workMinutes += (60 - startTimeMinute) + endTimeMinute;		// 分に変換
		}
		
		// DBのログイン者の当日の実働時間を更新
		attendanceRecordsRepository.updateWorkMinutesById(
				(int)session.getAttribute("mySerial_id"), LocalDate.now(),
				workMinutes);
		
		// DBのログイン者の当日の status を新規作成
		AttendanceRequestsEntity attendanceRequestsEntity = new AttendanceRequestsEntity();
		attendanceRequestsEntity.setSerialId((int)session.getAttribute("mySerial_id"));
		attendanceRequestsEntity.setWorkDate(LocalDate.now());
		attendanceRequestsEntity.setStatus(status);
		attendanceRequestsRepository.save(attendanceRequestsEntity);
		
	}
}
