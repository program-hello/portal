package com.example.demo.bean;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="password_reset")
@Data
public class AdminPasswordResetEntity {
	@Id
	String uid;
	@Column(name="password_reset_flag")
	Integer passwordResetFlag;
}
