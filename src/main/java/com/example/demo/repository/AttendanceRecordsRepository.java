package com.example.demo.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.bean.AttendanceRecordsEntity;

import jakarta.transaction.Transactional;

public interface AttendanceRecordsRepository extends JpaRepository<AttendanceRecordsEntity,Integer>{
	
	// ログイン者の退勤時刻を更新する
    @Modifying
    @Transactional
    @Query("UPDATE AttendanceRecordsEntity a SET a.clockOutTime = :clockOutTime WHERE a.serialId = :serialId AND a.workDate = :workDate")
    int updateClockOutTimeById(
    		@Param("serialId") int SerialId,
    		@Param("clockOutTime") LocalDateTime clockOutTime,
    		@Param("workDate") LocalDate workDate);
    
    // ログイン者の実働時間と status を更新
    @Modifying
    @Transactional
    @Query("UPDATE AttendanceRecordsEntity a SET a.workMinutes = :workMinutes WHERE a.serialId = :serialId AND a.workDate = :workDate")
    void updateWorkMinutesById(
    		@Param("serialId") int SerialId,
    		@Param("workDate") LocalDate workDate, 
    		@Param("workMinutes") int workMinutes);
   
    // ログイン者の出勤時刻を取得
    @Query("SELECT clockInTime FROM AttendanceRecordsEntity a WHERE a.serialId = :serialId AND workDate = :workDate")
    LocalDateTime findByTodayClockInTime(@Param("serialId") int serialId, @Param("workDate") LocalDate workDate);
    
    // ログイン者の退勤時刻を取得
    @Query("SELECT clockOutTime FROM AttendanceRecordsEntity a WHERE a.serialId = :serialId AND workDate = :workDate")
    LocalDateTime findByTodayClockOutTime(@Param("serialId") int serialId, @Param("workDate") LocalDate workDate);
    
    
    // serialId でDB(attendance_records)を検索し、ページング可能なメソッド
    Page<AttendanceRecordsEntity> findBySerialId(int serialId, Pageable pageable);
    
}
