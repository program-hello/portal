package com.example.demo.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.demo.service.UserDetailsServiceImpl;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class SecurityConfig {
	
	private final UserDetailsServiceImpl userDetailsServiceImpl;
	
	
	// パスワードエンコーダー
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    // ユーザを手動で設定
//    // 自作の UserDetailsService と競合するので両方を同時に有効にはできない
//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails user = User.withUsername("user")
//                .password(new BCryptPasswordEncoder().encode("password"))
//                .roles("USER")
//                .build();
//        return new InMemoryUserDetailsManager(user);
//    }
	    
	    // SecurityFilterChain を使用した認証・認可の設定
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            //.csrf().disable()
            .authorizeHttpRequests((authz) -> authz
                .requestMatchers("/sample").permitAll()	// 指定したURLパターンはログインを求めない
                .requestMatchers("/admin/**").hasRole("ADMIN")	// 指定したURLパターンはADMIN（管理者）だけに許可する
                .anyRequest().authenticated()		// 上記以外のすべてのリクエストを認証必須に
            )
            
            .formLogin(form -> form
                //.loginPage("/login")	// カスタムログインページ（必要なければ削除可能）
            	.defaultSuccessUrl("/home", true) // ← ログイン成功後は毎回 /home に遷移
                .permitAll()
            )
            .logout(logout -> logout
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                    .accessDeniedPage("/access-denied") // 権限がない場合にアクセス拒否ページを表示
                );
        return http.build();
    }
	    
	    // 自作したuserDetailsServiceImplクラスを用いてAuthenticationManagerを設定する
	    @Bean
	    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
	        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
	        auth.userDetailsService(userDetailsServiceImpl).passwordEncoder(passwordEncoder());
	        return auth.build(); // AuthenticationManagerを返す
	    }

	}
