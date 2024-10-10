package com.sharespace.sharespace_server.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sharespace.sharespace_server.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
