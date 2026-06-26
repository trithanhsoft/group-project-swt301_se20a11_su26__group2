package com.swp391.coding_platform.repository.user;

import com.swp391.coding_platform.entity.user.UserDailyActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserDailyActivityRepository extends JpaRepository<UserDailyActivityEntity, Integer> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_daily_activities (user_id, activity_date, streak, created_at) " +
            "VALUES (:userId, CURRENT_DATE, " +
            "  COALESCE((SELECT streak FROM user_daily_activities WHERE user_id = :userId AND activity_date = CURRENT_DATE - 1), 0) + 1, " +
            "  CURRENT_TIMESTAMP " +
            ") ON CONFLICT (user_id, activity_date) DO NOTHING",
            nativeQuery = true)
    void trackActivity(@Param("userId") Integer userId);

    @Query("SELECT a.activityDate " +
            "FROM UserDailyActivityEntity a " +
            "WHERE a.user.id = :userId " +
            "AND YEAR(a.activityDate) = :year " +
            "ORDER BY a.activityDate ASC")
    List<LocalDate> findActiveDatesByYear(@Param("userId") Integer userId, @Param("year") int year);

    @Query("SELECT MAX(a.streak) " +
            "FROM UserDailyActivityEntity a " +
            "WHERE a.user.id = :userId")
    Integer getMaxStreak(@Param("userId") Integer userId);

    @Query(value = "SELECT streak " +
            "FROM user_daily_activities " +
            "WHERE user_id = :userId " +
            "AND (activity_date = CURRENT_DATE OR activity_date = CURRENT_DATE - 1) " +
            "ORDER BY activity_date DESC LIMIT 1", nativeQuery = true)
    Number getCurrentValidStreak(@Param("userId") Integer userId);
}
