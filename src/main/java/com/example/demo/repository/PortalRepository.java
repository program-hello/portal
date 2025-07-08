package com.example.demo.repository;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.bean.RegisterEntity;

public interface  PortalRepository extends JpaRepository<RegisterEntity,Integer>{
	// CRUD メソッドが自動で使えるようになる！
	
	// 入力された uid がすでにDBに存在しているか検証
	boolean existsByUid(String uid); // 自動的にクエリが作られる！
	
    // uid を使って削除（Spring Data JPA が自動生成）
	@Transactional
    void deleteByUid(String uid);
}
