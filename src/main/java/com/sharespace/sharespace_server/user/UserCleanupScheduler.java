package com.sharespace.sharespace_server.user;

import com.sharespace.sharespace_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {
    private final UserRepository userRepository;

    // 매일 자정 (00:00)에 실행되도록 설정
    @Scheduled(cron = "0 21 17 * * *")
    @Transactional
    public void deleteUnverifiedUsers() {
        userRepository.deleteByEmailValidatedFalse();
    }
}
