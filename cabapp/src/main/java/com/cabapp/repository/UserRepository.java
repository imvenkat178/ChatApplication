package com.cabapp.repository;

import com.cabapp.model.User;
import com.cabapp.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // Find user by username (for login)
    Optional<User> findByUsername(String username);

    // Find user by email
    Optional<User> findByEmail(String email);

    // Find user by phone number
    Optional<User> findByPhoneNumber(String phoneNumber);

    // Find users by status
    List<User> findByStatus(UserStatus status);

    // Find active users
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE'")
    List<User> findActiveUsers();

    // Find users who registered recently
    @Query("SELECT u FROM User u WHERE u.createdAt > :sinceDate")
    List<User> findRecentUsers(@Param("sinceDate") LocalDateTime sinceDate);

    // Find users by registration date range
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersByRegistrationDateRange(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    // Check if username exists
    boolean existsByUsername(String username);

    // Check if email exists
    boolean existsByEmail(String email);

    // Check if phone number exists
    boolean existsByPhoneNumber(String phoneNumber);

    // Find users with location permission enabled (for auto-coordinates)
    @Query("SELECT u FROM User u WHERE u.locationPermissionGranted = true")
    List<User> findUsersWithLocationPermission();

    // Find users without location permission (may need manual pickup entry)
    @Query("SELECT u FROM User u WHERE u.locationPermissionGranted = false OR u.locationPermissionGranted IS NULL")
    List<User> findUsersWithoutLocationPermission();

    // Count users by status
    long countByStatus(UserStatus status);

    // Find users who have taken rides (have ride history)
    @Query("SELECT DISTINCT u FROM User u WHERE u.id IN (SELECT r.userId FROM Ride r)")
    List<User> findUsersWithRideHistory();

    // Find users who haven't taken any rides yet
    @Query("SELECT u FROM User u WHERE u.id NOT IN (SELECT r.userId FROM Ride r)")
    List<User> findUsersWithoutRideHistory();

    // Find users with multiple rides (frequent users)
    @Query("SELECT u FROM User u WHERE u.id IN " +
            "(SELECT r.userId FROM Ride r GROUP BY r.userId HAVING COUNT(r) > :minRides)")
    List<User> findFrequentUsers(@Param("minRides") int minRides);

    // Find users by city (from profile or address)
    @Query("SELECT u FROM User u WHERE LOWER(u.city) = LOWER(:city)")
    List<User> findUsersByCity(@Param("city") String city);

    // Find users in Atlanta area (if they have set their city)
    @Query("SELECT u FROM User u WHERE LOWER(u.city) LIKE LOWER('%atlanta%') OR LOWER(u.city) LIKE LOWER('%sandy springs%') " +
            "OR LOWER(u.city) LIKE LOWER('%marietta%') OR LOWER(u.city) LIKE LOWER('%alpharetta%')")
    List<User> findUsersInAtlantaArea();

    // Analytics: Get user statistics
    @Query("SELECT " +
            "COUNT(u) as totalUsers, " +
            "SUM(CASE WHEN u.status = 'ACTIVE' THEN 1 ELSE 0 END) as activeUsers, " +
            "SUM(CASE WHEN u.status = 'INACTIVE' THEN 1 ELSE 0 END) as inactiveUsers, " +
            "SUM(CASE WHEN u.locationPermissionGranted = true THEN 1 ELSE 0 END) as usersWithLocationPermission " +
            "FROM User u")
    Object[] getUserStatistics();

    // Find users who last logged in within a time range
    @Query("SELECT u FROM User u WHERE u.lastLoginAt BETWEEN :startDate AND :endDate")
    List<User> findUsersByLastLoginRange(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // Find users who haven't logged in recently (inactive users)
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :cutoffDate OR u.lastLoginAt IS NULL")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Find users by device type (iOS, Android, Web)
    @Query("SELECT u FROM User u WHERE u.preferredDeviceType = :deviceType")
    List<User> findUsersByDeviceType(@Param("deviceType") String deviceType);

    // Find users who prefer cash payment (relevant for auto-coordinates system)
    @Query("SELECT u FROM User u WHERE u.preferredPaymentMethod = 'CASH'")
    List<User> findCashPaymentUsers();

    // Find users who have enabled notifications
    @Query("SELECT u FROM User u WHERE u.notificationsEnabled = true")
    List<User> findUsersWithNotificationsEnabled();

    // Find users by age range (if you have birthdate field)
    @Query("SELECT u FROM User u WHERE YEAR(CURRENT_DATE) - YEAR(u.birthDate) BETWEEN :minAge AND :maxAge")
    List<User> findUsersByAgeRange(int minAge, int maxAge);

    // Search users by name or username
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsersByNameOrUsername(@Param("searchTerm") String searchTerm);

    // Find users who joined this month
    @Query("SELECT u FROM User u WHERE YEAR(u.createdAt) = YEAR(CURRENT_DATE) " +
            "AND MONTH(u.createdAt) = MONTH(CURRENT_DATE)")
    List<User> findUsersJoinedThisMonth();
}