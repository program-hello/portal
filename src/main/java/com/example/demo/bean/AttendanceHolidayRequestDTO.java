package com.example.demo.bean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceHolidayRequestDTO {
	LocalDate date = LocalDate.now();
	private Integer year = date.getYear();      // ← 正しく「年（例：2025）」を取得
	private Integer month = date.getMonthValue(); // ← 正しく「月（1〜12）」を取得
	@NotNull(message="日付を選択してください")
	private Integer day = date.getDayOfMonth();	// 日付は複数選択できるので配列で受け取る
	private LocalDate holidayDate;
	@NotBlank(message = "※内容を入力してください")
	private String remarks;
	
    private List<LocalDate> holidayDates; // 日付リスト
    // 送信された年、月、日をリストとして受け取る
    private List<Integer> yearData;
    private List<Integer> monthData;
    private List<Integer> dayData;
    
    // 年、月、日から LocalDate に変換
    public void convertToHolidayDates() {
        holidayDates = new ArrayList<>();
        for (int i = 0; i < yearData.size(); i++) {
            LocalDate date = LocalDate.of(yearData.get(i), monthData.get(i), dayData.get(i));
            holidayDates.add(date);
        }
    }
}
