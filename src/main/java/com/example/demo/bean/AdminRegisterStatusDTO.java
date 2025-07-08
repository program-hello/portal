package com.example.demo.bean;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AdminRegisterStatusDTO {
	LocalDate today = LocalDate.now();	// 現在の日時(Date)を取得
	
	private Integer year = today.getYear();      // ← 正しく「年（例：2025）」を取得
	private Integer month = today.getMonthValue(); // ← 正しく「月（1〜12）」を取得
	private Integer day = today.getDayOfMonth();   // ← 正しく「日（1〜31）」を取得
	
	// @DateTimeFormat(pattern = "yyyy-MM-dd") をつけることで、
	// Spring がフォームから渡された文字列 "2025-05-16" を LocalDate に変換してくれる
	@DateTimeFormat(pattern = "yyyy-MM-dd") // ★これが重要
	private LocalDate workDate;
	private String remarks;
	@Min(value = 0, message="※申請項目を選択してください")
	private int status;
	private String uid;
}
