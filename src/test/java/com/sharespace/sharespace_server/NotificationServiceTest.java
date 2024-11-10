package com.sharespace.sharespace_server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.sharespace.sharespace_server.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.sharespace.sharespace_server.user.entity.User;
import com.sharespace.sharespace_server.user.repository.UserRepository;
import com.sharespace.sharespace_server.notification.repository.NotificationRepository;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.global.enums.Role;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private BaseResponseService baseResponseService;

    private List<User> testUsers;
    private ExecutorService executorService;
    private static final int USER_COUNT = 100;
    private static final int CONCURRENT_USERS = 50;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 100명 생성 및 Mock 설정
        testUsers = new ArrayList<>();
        for (int i = 0; i < USER_COUNT; i++) {
            User user = User.builder()
                    .id((long) i)
                    .email("test" + i + "@test.com")
                    .nickName("User" + i)
                    .role(Role.ROLE_GUEST)
                    .password("password")
                    .location("Seoul")
                    .latitude(37.5665)
                    .longitude(126.9780)
                    .build();
            testUsers.add(user);
            when(userRepository.findById((long) i)).thenReturn(java.util.Optional.of(user));
        }

        // 테스트용 스레드 풀 초기화
        executorService = Executors.newFixedThreadPool(100);
    }

    @Test

    @DisplayName("다중 사용자 동시 접속 테스트")
    void testMultipleUserSubscriptions() throws InterruptedException {
        // Given
        CountDownLatch connectionLatch = new CountDownLatch(CONCURRENT_USERS);
        CountDownLatch completionLatch = new CountDownLatch(CONCURRENT_USERS);
        List<SseEmitter> emitters = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>(); // 테스트 중 발생하는 예외를 저장

        // When
        // 50명의 사용자가 동시에 접속하는 상황 시뮬레이션
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userIndex = i;
            executorService.submit(() -> {
                try {
                    // 모든 스레드가 준비될 때까지 대기
                    connectionLatch.countDown();
                    connectionLatch.await();

                    // SSE 연결 생성 및 알림 전송 테스트
                    System.out.printf("[사용자 %d] SSE 연결 시도\n", userIndex);
                    SseEmitter emitter = notificationService.subscribe(testUsers.get(userIndex).getId());
                    emitters.add(emitter);

                    System.out.printf("[사용자 %d] 알림 전송 시도\n", userIndex);
                    notificationService.sendNotification(
                            testUsers.get(userIndex).getId(),
                            "Test notification for user " + userIndex
                    );
                    System.out.printf("[사용자 %d] 알림 전송 완료\n", userIndex);
                } catch (Exception e) {
                    System.err.printf("[사용자 %d] 오류 발생: %s\n", userIndex, e.getMessage());
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                } finally {
                    completionLatch.countDown();
                }
            });
        }
        System.out.println("=== 다중 사용자 동시 접속 테스트 종료 ===\n");

        // Then
        // 1. 모든 테스트가 30초 이내에 완료되는지 확인
        boolean completed = completionLatch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "테스트가 시간 내에 완료되지 않았습니다.");

        // 2. 예외가 발생하지 않았는지 확인
        assertTrue(exceptions.isEmpty(),
                "테스트 중 예외 발생: " + (exceptions.isEmpty() ? "" : exceptions.get(0).getMessage()));

        // 3. 모든 사용자에 대해 알림이 정상적으로 처리되었는지 확인
        verify(userRepository, times(CONCURRENT_USERS)).findById(any());
        verify(notificationRepository, times(CONCURRENT_USERS)).save(any());

        // 4. 리소스 정리
        emitters.forEach(SseEmitter::complete);
    }

    @Test
    @DisplayName("대규모 알림 브로드캐스트 테스트")
    void testBroadcastNotification() throws InterruptedException {
        System.out.println("\n=== 대규모 알림 브로드캐스트 테스트 시작 ===");
        // Given
        List<SseEmitter> emitters = new ArrayList<>();
        CountDownLatch broadcastLatch = new CountDownLatch(USER_COUNT);
        String broadcastMessage = "Broadcast test message";
        List<Exception> exceptions = new ArrayList<>();

        // When
        // 1. 모든 사용자를 구독 상태로 만듦
        for (int i = 0; i < USER_COUNT; i++) {
            System.out.printf("[사용자 %d] SSE 구독 시작\n", i);
            emitters.add(notificationService.subscribe(testUsers.get(i).getId()));
        }

        // 2. 브로드캐스트 메시지 전송
        for (User user : testUsers) {
            executorService.submit(() -> {
                try {
                    System.out.printf("[사용자 %d] 브로드캐스트 메시지 전송 시도\n", user.getId());
                    notificationService.sendNotification(user.getId(), broadcastMessage);
                    System.out.printf("[사용자 %d] 브로드캐스트 메시지 전송 완료\n", user.getId());
                } catch (Exception e) {
                    synchronized (exceptions) {
                        System.err.printf("[사용자 %d] 오류 발생: %s\n", user.getId(), e.getMessage());
                        exceptions.add(e);
                    }
                } finally {
                    broadcastLatch.countDown();
                }
            });
        }
        System.out.println("=== 대규모 알림 브로드캐스트 테스트 종료 ===\n");

        // Then
        // 1. 브로드캐스트가 30초 이내에 완료되는지 확인
        boolean completed = broadcastLatch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "브로드캐스트가 시간 내에 완료되지 않았습니다.");

        // 2. 예외가 발생하지 않았는지 확인
        assertTrue(exceptions.isEmpty(),
                "브로드캐스트 중 예외 발생: " + (exceptions.isEmpty() ? "" : exceptions.get(0).getMessage()));

        // 3. 모든 사용자가 알림을 받았는지 확인
        verify(notificationRepository, times(USER_COUNT)).save(any());

        // 4. 리소스 정리
        emitters.forEach(SseEmitter::complete);
    }

    @Test
    @DisplayName("장시간 연결 안정성 테스트")
    void testConnectionStability() throws InterruptedException {
        // Given
        int testDurationSeconds = 60;
        CountDownLatch stabilityLatch = new CountDownLatch(USER_COUNT);
        List<SseEmitter> emitters = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();

        // When
        // 100명의 사용자에 대해 60초 동안 연결 유지 및 주기적 알림 전송 테스트
        for (int i = 0; i < USER_COUNT; i++) {
            final int userIndex = i;
            executorService.submit(() -> {
                try {
                    // 1. SSE 연결 생성
                    SseEmitter emitter = notificationService.subscribe(testUsers.get(userIndex).getId());
                    emitters.add(emitter);

                    // 2. 10초마다 알림 전송
                    for (int j = 0; j < testDurationSeconds / 10; j++) {
                        Thread.sleep(10000);
                        notificationService.sendNotification(
                                testUsers.get(userIndex).getId(),
                                "Stability test message " + j
                        );
                    }
                } catch (Exception e) {
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                } finally {
                    stabilityLatch.countDown();
                }
            });
        }

        // Then
        // 1. 테스트가 정해진 시간 내에 완료되는지 확인
        boolean completed = stabilityLatch.await(testDurationSeconds + 10, TimeUnit.SECONDS);
        assertTrue(completed, "안정성 테스트가 시간 내에 완료되지 않았습니다.");

        // 2. 예외가 발생하지 않았는지 확인
        assertTrue(exceptions.isEmpty(),
                "안정성 테스트 중 예외 발생: " + (exceptions.isEmpty() ? "" : exceptions.get(0).getMessage()));

        // 3. 모든 알림이 정상적으로 전송되었는지 확인
        verify(notificationRepository, atLeast(USER_COUNT)).save(any());

        // 4. 리소스 정리
        emitters.forEach(SseEmitter::complete);
    }
}