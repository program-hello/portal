package com.example.demo.bean;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Data;
@Entity
@Table(name="user")
@Data
public class RegisterEntity {
    @Id
	@NotBlank(message = "※名前は必須です")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "※ユーザー名は半角英数字のみで入力してください")
	private String uid;
	@NotBlank(message = "※パスワードは必須です")
	//@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "※パスワードは半角英数字のみで入力してください")
	private String passwd;
	private String role = "ROLE_USER";
}
