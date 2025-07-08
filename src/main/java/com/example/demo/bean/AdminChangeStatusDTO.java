package com.example.demo.bean;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AdminChangeStatusDTO {
	private int serialId;
	private String uid;
	private LocalDate workDate;
	private int status;
	private String remarks;
	private String statusName;
	private boolean isCheckRemarks = false;
	
	public void changeStatus() {
		switch(this.status) {
		case 0:
			this.statusName = "通常出勤";
			break;
		case 1:
			this.statusName = "遅刻";
			break;
		case 2:
			this.statusName = "早退";
			break;
		case 3:
			this.statusName = "出張";
			break;
		case 4:
			this.statusName = "欠席";
			break;
		}
	}
	public void checkRemarks() {
		if(this.remarks != null) {
			this.isCheckRemarks = true;
		}
	}
}
