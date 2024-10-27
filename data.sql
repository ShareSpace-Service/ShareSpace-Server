CREATE database sharespace;
USE sharespace;

CREATE TABLE user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nick_name VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL,
    image TEXT NULL,
    role ENUM('ROLE_HOST', 'ROLE_GUEST') NOT NULL,
    password VARCHAR(255) NOT NULL,
    lock_time DATETIME NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    location VARCHAR(50) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    email_validated BOOLEAN NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE jwt (
                     id BIGINT NOT NULL AUTO_INCREMENT,
                     user_id BIGINT NOT NULL,
                     refresh_token TEXT NOT NULL,
                     PRIMARY KEY (id),
                     FOREIGN KEY (user_id) REFERENCES user (id)
);

CREATE TABLE place (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(50) NOT NULL,
    category ENUM('LARGE', 'MEDIUM', 'SMALL') NOT NULL,
    period INT NOT NULL,
    description VARCHAR(100) NULL,
    image_url TEXT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user (id)
);

CREATE TABLE product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(50) NOT NULL,
    category ENUM('LARGE', 'MEDIUM', 'SMALL') NOT NULL,
    period INT NOT NULL,
    description VARCHAR(100) NULL,
    image_url TEXT,
    writed_at DATETIME NOT NULL,
    is_placed BOOLEAN NOT NULL COMMENT '장소 미배정 상태 관리',
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user (id)
);

CREATE TABLE note (
    id BIGINT NOT NULL AUTO_INCREMENT,
    id1 BIGINT NOT NULL,
    id2 BIGINT NOT NULL,
    title VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    send_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id1) REFERENCES user (id),
    FOREIGN KEY (id2) REFERENCES user (id)
);

CREATE TABLE notification (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '알림 받는 대상',
    is_read BOOLEAN NOT NULL DEFAULT 0,
    message TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user (id)
);

CREATE TABLE matching (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    place_id BIGINT NULL,
    image TEXT NULL COMMENT '요청을 수락했을 경우 매칭 테이블에 저장됨.',
    status ENUM('UNASSIGNED', 'REQUESTED', 'REJECTED', 'PENDING', 'STORED', 'COMPLETED') NOT NULL,
    host_completed BOOLEAN NOT NULL DEFAULT 0,
    guest_completed BOOLEAN NOT NULL DEFAULT 0,
    distance INT NULL COMMENT 'm 단위로 저장',
    start_date DATETIME NULL,
    expiry_date DATETIME NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (product_id) REFERENCES product (id),
    FOREIGN KEY (place_id) REFERENCES place (id)
);

CREATE TABLE contact (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(50) NOT NULL,
    content VARCHAR(200) NOT NULL,
    PRIMARY KEY (id)
);


INSERT INTO user (nick_name, email, image, role, password, location, latitude, longitude, email_validated)
VALUES
    ('호스트민우갓', 'hostminwoo@example.com', 'temporary image url', 'ROLE_HOST', '$2b$12$qZMgj4TG2KqU2ulwGnMD8.0eYstepNGJRva/GEheQHOYNbdS5hIhi', '서울', 200, 100, 1),
    ('게스트민우갓', 'guestminwoo@example.com', 'temporary image url', 'ROLE_GUEST', '$2b$12$qZMgj4TG2KqU2ulwGnMD8.0eYstepNGJRva/GEheQHOYNbdS5hIhi', '경기', 100, 200, 1);
