package com.example.demo.bean;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "attendance_holiday_requests")
@Data
public class AttendanceHolidayRequestsEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // IDは自動生成
	private Integer id;
	@Column(name="serial_id")
	private int serialId;
	@Column(name="holiday_date")
	private LocalDate holidayDate;
	private int status;
	private String remarks;
}
