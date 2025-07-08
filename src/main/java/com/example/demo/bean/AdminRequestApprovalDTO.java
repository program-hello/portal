package com.example.demo.bean;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class AdminRequestApprovalDTO {
	LocalDate today = LocalDate.now();	// 現在の日時(Date)を取得
	
	private Integer year = today.getYear();      // ← 正しく「年（例：2025）」を取得
	private Integer month = today.getMonthValue(); // ← 正しく「月（1〜12）」を取得
	
	@DateTimeFormat(pattern = "yyyy-MM-dd") // ★これが重要
	private LocalDate workDate;
	
	private String uid;
	private int isHolidayCheck;
	private int isAllCheck;
	private String statusName;
}
