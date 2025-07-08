package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.example.demo.bean.AdminPasswordResetEntity;

import jakarta.transaction.Transactional;

public interface AdminPasswordResetRepository extends JpaRepository<AdminPasswordResetEntity, String>{
	
	// 特定の uid を持つレコードが存在するか検証
	boolean existsByUid(String uid);
	
	// 特定の uid を持つレコードを削除
	@Modifying
	@Transactional
	void deleteByUid(String uid);
}
