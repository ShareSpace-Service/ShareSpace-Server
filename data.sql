CREATE database sharespace;
USE sharespace;

DROP TABLE IF EXISTS user;
CREATE TABLE user (
    id BIGINT NOT NULL,
    nick_name VARCHAR(50) NULL,
    email VARCHAR(50) NULL,
    image TEXT NULL,
    role ENUM('Host', 'Guest') NULL,
    password VARCHAR(50) NULL,
    lock_time DATETIME NULL,
    failed_attempts INT NULL,
    location VARCHAR(50) NULL,
    latitude DOUBLE NULL,
    longitude DOUBLE NULL,
    email_validated BOOLEAN NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS place;
CREATE TABLE place (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    title VARCHAR(50) NULL,
    category ENUM('대', '중', '소') NULL,
    period INT NULL,
    description VARCHAR(100) NULL,
    image_url TEXT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user (id)
);

DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    title VARCHAR(50) NULL,
    category ENUM('대', '중', '소') NULL,
    period INT NULL,
    description VARCHAR(100) NULL,
    image_url TEXT NULL,
    writed_at DATETIME NULL,
    is_placed BOOLEAN NULL COMMENT '장소 미배정 상태 관리',
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user (id)
);

DROP TABLE IF EXISTS note;
CREATE TABLE note (
    id BIGINT NOT NULL,
    id1 BIGINT NOT NULL,
    id2 BIGINT NOT NULL,
    title VARCHAR(50) NULL,
    content TEXT NULL,
    send_at DATETIME NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id1) REFERENCES user (id),
    FOREIGN KEY (id2) REFERENCES user (id)
);

DROP TABLE IF EXISTS notification;
CREATE TABLE notification (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL COMMENT '알림 받는 대상',
    is_read BOOLEAN NULL,
    message TEXT NULL,
    created_at DATETIME NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user (id)
);

DROP TABLE IF EXISTS matching;
CREATE TABLE matching (
    id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    place_id BIGINT NOT NULL,
    image TEXT NULL COMMENT '요청을 수락했을 경우 매칭 테이블에 저장됨.',
    status ENUM('미배정', '요청됨', '반려됨', '대기중', '보관됨', '완료됨') NULL,
    host_completed BOOLEAN NULL,
    guest_completed BOOLEAN NULL,
    distance INT NULL COMMENT 'm 단위로 저장',
    PRIMARY KEY (id),
    FOREIGN KEY (product_id) REFERENCES product (id),
    FOREIGN KEY (place_id) REFERENCES place (id)
);

DROP TABLE IF EXISTS contact;
CREATE TABLE contact (
    id BIGINT NOT NULL,
    title VARCHAR(50) NULL,
    content VARCHAR(200) NULL,
    PRIMARY KEY (id)
);