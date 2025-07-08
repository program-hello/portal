package com.example.demo.bean;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDTO {
	@Id
	@NotBlank(message = "※ユーザー名は必須です")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "※ユーザー名は半角英数字のみで入力してください")
	private String uid;
	@Size(min = 5, message = "※パスワードは5文字以上で入力してください")
	@NotBlank(message = "※パスワードは必須です")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "※パスワードは半角英数字のみで入力してください")
	private String passwd;
	private String role;
}
