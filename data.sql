CREATE database sharespace;
USE sharespace;

DROP TABLE IF EXISTS user;
CREATE TABLE user (
    id BIGINT NOT NULL,
    nick_name VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL,
    image TEXT NULL,
    role ENUM('Host', 'Guest') NOT NULL,
    password VARCHAR(50) NOT NULL,
    lock_time DATETIME NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    location VARCHAR(50) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    email_validated BOOLEAN NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS place;
CREATE TABLE place (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    title VARCHAR(50) NOT NULL,
    category ENUM('Large', 'Medium', 'Small') NOT NULL,
    period INT NOT NULL,
    description VARCHAR(100) NULL,
    image_url TEXT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user (id)
);

DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    title VARCHAR(50) NOT NULL,
    category ENUM('Large', 'Medium', 'Small') NOT NULL,
    period INT NOT NULL,
    description VARCHAR(100) NULL,
    image_url TEXT NOT NULL,
    writed_at DATETIME NOT NULL,
    is_placed BOOLEAN NOT NULL COMMENT '장소 미배정 상태 관리',
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user (id)
);

DROP TABLE IF EXISTS note;
CREATE TABLE note (
    id BIGINT NOT NULL,
    id1 BIGINT NOT NULL,
    id2 BIGINT NOT NULL,
    title VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    send_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id1) REFERENCES user (id),
    FOREIGN KEY (id2) REFERENCES user (id)
);

DROP TABLE IF EXISTS notification;
CREATE TABLE notification (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL COMMENT '알림 받는 대상',
    is_read BOOLEAN NOT NULL DEFAULT 0,
    message TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user (id)
);

DROP TABLE IF EXISTS matching;
CREATE TABLE matching (
    id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    place_id BIGINT NOT NULL,
    image TEXT NULL COMMENT '요청을 수락했을 경우 매칭 테이블에 저장됨.',
    status ENUM('Unassigned', 'Requested', 'Rejected', 'Pending', 'Stored', 'Completed') NOT NULL,
    host_completed BOOLEAN NOT NULL DEFAULT 0,
    guest_completed BOOLEAN NOT NULL DEFAULT 0,
    distance INT NOT NULL COMMENT 'm 단위로 저장',
    PRIMARY KEY (id),
    FOREIGN KEY (product_id) REFERENCES product (id),
    FOREIGN KEY (place_id) REFERENCES place (id)
);

DROP TABLE IF EXISTS contact;
CREATE TABLE contact (
    id BIGINT NOT NULL,
    title VARCHAR(50) NOT NULL,
    content VARCHAR(200) NOT NULL,
    PRIMARY KEY (id)
);