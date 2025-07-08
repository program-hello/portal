package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.example.demo.bean.RegisterEntity2;

@Service
public interface SignUpRepository extends JpaRepository<RegisterEntity2,Integer>{
	

	
	// 入力された uid と passwd がDBに存在しているか検証（ログイン用）
	Optional<RegisterEntity2> findByUid(String uid);
	
	List<RegisterEntity2> findAllByOrderByUidAsc(); // 昇順
	
	// uid で serial_id を取得
	@Query("SELECT r.serialId FROM RegisterEntity2 r WHERE r.uid = :uid")
	Integer findSeirilIdByUid(@Param("uid") String uid);
	
	// serial_id で uid を取得
	@Query("SELECT r.uid FROM RegisterEntity2 r WHERE r.serialId = :serialId")
	String findUidBySerialId(@Param("serialId") int serialId);
	
	// パスワードリセットで使用
    @Modifying
    @Transactional
	@Query("UPDATE RegisterEntity2 "
			+ "SET passwd = :passwd "
			+ "WHERE uid = :uid")
	int updateByUid(@Param("passwd") String passwd, @Param("uid") String uid);
}
