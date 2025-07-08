package com.example.demo.service;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.bean.RegisterEntity2;
import com.example.demo.repository.SignUpRepository;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
// ユーザ情報を取得するクラス
public class UserDetailsServiceImpl implements UserDetailsService{
	private final HttpSession session;
	
	
	private final SignUpRepository signUpRepository;

	//loadUserByUsernameは実装が必要
	// 引数の String uid はログインページで用意された name="username" の値
    @Override
    public UserDetails loadUserByUsername(String uid) throws UsernameNotFoundException {
    	// DBから
        RegisterEntity2 user = signUpRepository.findByUid(uid)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // sessionにログインユーザの情報を登録
        session.setAttribute("mySerial_id", user.getSerialId());
        session.setAttribute("myUid", user.getUid());
        session.setAttribute("myRole", user.getRole());

        // ユーザーネーム、パスワード、ロールを設定する
        return new User(user.getUid(), user.getPasswd(),
                AuthorityUtils.createAuthorityList(user.getRole().split(",")));
        
        // uid はDBに登録した変数名　
        // UserEntity は自作のEntityクラス
    }
}
