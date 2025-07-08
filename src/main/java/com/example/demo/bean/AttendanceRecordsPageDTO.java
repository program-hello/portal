package com.example.demo.bean;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttendanceRecordsPageDTO {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // IDは自動生成
	private Integer id;
	private int serialId;
	private LocalDate workDate;
	private LocalDateTime clockInTime;
	private LocalDateTime clockOutTime;
	private int breakMinutes;
	private int workMinutes;
	private String statuses;
}
