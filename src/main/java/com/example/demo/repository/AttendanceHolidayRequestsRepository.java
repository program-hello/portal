package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.bean.AttendanceHolidayRequestsEntity;

import jakarta.transaction.Transactional;

public interface AttendanceHolidayRequestsRepository extends JpaRepository<AttendanceHolidayRequestsEntity,Integer>{
	
	// 全件取得（holidayDateで降順にソート）
	List<AttendanceHolidayRequestsEntity> findAllByOrderByHolidayDateDesc();
	
	// 特定の serial_id で全件取得
	List<AttendanceHolidayRequestsEntity> findBySerialIdOrderByHolidayDateDesc(int serialId);

	// すでに holiday_date でレコードが登録されているか検索
	Optional<AttendanceHolidayRequestsEntity> findByHolidayDate(LocalDate holidayDate);
	
	@Query("SELECT COUNT(a) FROM AttendanceHolidayRequestsEntity a WHERE a.holidayDate = :holidayDate AND a.serialId = :serialId")
	Integer existWorkDate(@Param("holidayDate") LocalDate holidayDate, @Param("serialId") int serialId);
	
	// 特定の serial_id, 年,月 で全件取得
    @Query("SELECT a FROM AttendanceHolidayRequestsEntity a "
        	+ "WHERE a.serialId = :serialId "
        	+ "AND FUNCTION('YEAR', a.holidayDate) = :year "
        	+ "AND FUNCTION('MONTH', a.holidayDate) = :month "
        	+ "ORDER BY a.holidayDate DESC")
	List<AttendanceHolidayRequestsEntity> findAllBySerialId(
			@Param("serialId") int serialId,
			@Param("year") int year,
			@Param("month") int month);
    
	// 特定の serial_id, 年,月 で全件取得
    @Query("SELECT a FROM AttendanceHolidayRequestsEntity a "
        	+ "WHERE FUNCTION('YEAR', a.holidayDate) = :year "
        	+ "AND FUNCTION('MONTH', a.holidayDate) = :month "
        	+ "ORDER BY a.holidayDate DESC")
	List<AttendanceHolidayRequestsEntity> findAllByYearAndMonth(
			@Param("year") int year,
			@Param("month") int month);
    
    // 承認ボタンで status の値を１に変更する
    @Modifying
    @Transactional
    @Query("UPDATE AttendanceHolidayRequestsEntity a SET a.status =1 "
    		+ "WHERE a.serialId = :serialId AND a.holidayDate = :holidayDate")
    void updateStatusByIdAndHolidayDate(
    		@Param("serialId") int SerialId,
    		@Param("holidayDate") LocalDate holidayDate);
}
