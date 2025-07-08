package com.example.demo.bean;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AdminAddUidDTO {
	private Integer id;
	private int serialId;
	private String uid;
	private LocalDate holidayDate;
	private int status;
	private String remarks;
}
