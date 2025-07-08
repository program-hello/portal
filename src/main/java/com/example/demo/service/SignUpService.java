package com.example.demo.service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.bean.RegisterEntity;
import com.example.demo.bean.RegisterEntity2;
import com.example.demo.repository.PortalRepository;
import com.example.demo.repository.SignUpRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SignUpService {
	
	private final PortalRepository portalRepository;
	private final SignUpRepository signUpRepository;
	

    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // DBに保存
    public boolean saveUser(String uid, String passwd, String role) {
        // 入力された uid がすでに登録されているかを確認する
        if (portalRepository.existsByUid(uid)) {
            return false;  // すでに存在する → 登録を拒否
        }

        RegisterEntity user = new RegisterEntity();
        user.setUid(uid);
        user.setPasswd(passwordEncoder().encode(passwd)); // パスワードをハッシュ化
        user.setRole(role);
        portalRepository.save(user);	// DBに登録
        return true;
    }
    
    // DBからユーザーを削除
    public boolean deleteUser(String uid) {
        // 入力された uid がすでに登録されているかを確認する
        if (!portalRepository.existsByUid(uid)) {
            return false;  // DBに存在しない → 削除を拒否
        }
        
        portalRepository.deleteByUid(uid);  // IDでユーザーを削除
        return true;
    }
    
    // DBからユーザーを全件取得
    public List<RegisterEntity2> getAllUsers() {
        return signUpRepository.findAllByOrderByUidAsc(); 
    }
    
}
