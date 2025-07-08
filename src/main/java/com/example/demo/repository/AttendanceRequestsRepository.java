package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.bean.AttendanceRequestsEntity;

import jakarta.transaction.Transactional;

public interface AttendanceRequestsRepository extends JpaRepository<AttendanceRequestsEntity,Integer>{
	
	// 特定の serial_id で全件取得
	List<AttendanceRequestsEntity> findBySerialIdOrderByWorkDateDesc(int serialId);
	
	// 全件取得して uid, workDateで降順にソート
	List<AttendanceRequestsEntity> findAllByOrderByWorkDateDesc();

    // ログイン者の特定の勤務日の status を取得
    @Query("SELECT status FROM AttendanceRequestsEntity WHERE serialId = :serialId AND workDate = :workDate")
    Integer[] findStatusBySerialIdAndWorkDate(@Param("serialId") int serialId, @Param("workDate") LocalDate workDate);
    
    // ログイン者の特定の勤務日の workDate, status を取得
    @Query("SELECT workDate, status FROM AttendanceRequestsEntity WHERE serialId = :serialId")
    List<Object[]> findAllStatusBySerialIdAndWorkDate(@Param("serialId") int serialId);
    
    
    // ログイン者の status別 の remarks を更新（attendance_request.html の送信ボタンで使う）
    @Modifying
    @Transactional
    @Query("UPDATE AttendanceRequestsEntity a SET a.remarks = :remarks WHERE a.serialId = :serialId AND a.workDate = :workDate AND a.status = :status")
    void updateRemarksByIdANDWorkDate(
    		@Param("serialId") int SerialId,
    		@Param("workDate") LocalDate workDate, 
    		@Param("status") int status,
    		@Param("remarks") String remarks);
    

    // ログイン者の複数の status を取得する
	// DB(attendance_requests)からwork_dateごとの status を取得
	// 同一日に複数県 status が登録されていたら , 区切りで結合
    @Query(
    		  value = "SELECT a.serial_id, a.work_date, GROUP_CONCAT(a.status ORDER BY a.status SEPARATOR ',') AS statuses " +
    		          "FROM attendance_requests a " +
    				  "WHERE a.serial_id = :serialId "+
    		          "GROUP BY a.serial_id, a.work_date " +
    				  "ORDER BY a.work_date DESC ",
    		  nativeQuery = true
    		)
    		List<Object[]> findStatusGroupBySerialId(@Param("serialId") int serialId);
    		
    		
    // 特定のユーザーのレコードを全件取得
    @Query("SELECT a FROM AttendanceRequestsEntity a "
    	+ "WHERE a.serialId = :serialId "
    	+ "AND FUNCTION('YEAR', a.workDate) = :year "
    	+ "AND FUNCTION('MONTH', a.workDate) = :month "
    	+ "ORDER BY a.workDate DESC")
    List<AttendanceRequestsEntity> findAllBySerialIdAndYearAndMonthOrderByWorkDateDesc(
    		@Param("serialId") int serialId,
    		@Param("year") int year,
    		@Param("month") int month);
    
    // 全ユーザーのレコードを全件取得
    @Query("SELECT a FROM AttendanceRequestsEntity a "
    	+ "WHERE FUNCTION('YEAR', a.workDate) = :year "
    	+ "AND FUNCTION('MONTH', a.workDate) = :month "
    	+ "ORDER BY workDate DESC")
    List<AttendanceRequestsEntity> findAllByYearAndMonthOrderByWorkDateDesc(
    		@Param("year") int year,
    		@Param("month") int month);
}
