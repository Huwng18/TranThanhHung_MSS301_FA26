CREATE TABLE booking (
    booking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    booking_date DATETIME NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    booking_status VARCHAR(20) NOT NULL
);

CREATE TABLE booking_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    showtime_id VARCHAR(24) NOT NULL,
    movie_id VARCHAR(24) NOT NULL,
    movie_title VARCHAR(255),
    room_name VARCHAR(255),
    seat_code VARCHAR(10) NOT NULL,
    ticket_price DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_booking FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE CASCADE
);
