package com.example.demo.bean;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "attendance_records")
@Data
public class AttendanceRecordsEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // IDは自動生成
	private Integer id;
	@Column(name="serial_id")
	private int serialId;
	@Column(name="work_date")
	private LocalDate workDate;
	@Column(name="clock_in_time")
	private LocalDateTime clockInTime;
	@Column(name="clock_out_time")
	private LocalDateTime clockOutTime;
	@Column(name="break_minutes")
	private int breakMinutes;
	@Column(name="work_minutes")
	private int workMinutes;
}
