package com.sharespace.sharespace_server.notification.repository;

import com.sharespace.sharespace_server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sharespace.sharespace_server.notification.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUser(User user);
}
