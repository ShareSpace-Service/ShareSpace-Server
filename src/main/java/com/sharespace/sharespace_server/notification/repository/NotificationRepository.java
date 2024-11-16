package com.sharespace.sharespace_server.notification.repository;

import com.sharespace.sharespace_server.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sharespace.sharespace_server.notification.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUser(User user);
    Page<Notification> findAllByUser(User user, Pageable pageable);
    // @Query("select COUNT(*) "
    //     + " from Notification n"
    //     + " where n.isRead = false"
    //     + " and n.user = :user")
    // List<Notification> findUnreadNotificationsByUser(User user);

    @Query("select COUNT(*) "
        + " from Notification n"
        + " where n.isRead = false"
        + " and n.user = :user")
    int findUnreadNotificationsCountByUser(User user);

    void deleteAllByUser(User user);
}
