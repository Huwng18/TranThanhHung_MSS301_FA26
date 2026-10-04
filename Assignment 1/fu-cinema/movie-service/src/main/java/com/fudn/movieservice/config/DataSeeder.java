package com.fudn.movieservice.config;

import com.fudn.movieservice.model.CinemaRoom;
import com.fudn.movieservice.model.Genre;
import com.fudn.movieservice.model.RoomStatus;
import com.fudn.movieservice.model.RoomType;
import com.fudn.movieservice.repository.CinemaRoomRepository;
import com.fudn.movieservice.repository.GenreRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {
    private final GenreRepository genreRepository;
    private final CinemaRoomRepository roomRepository;

    public DataSeeder(GenreRepository genreRepository, CinemaRoomRepository roomRepository) {
        this.genreRepository = genreRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (genreRepository.count() == 0) {
            genreRepository.saveAll(List.of(
                    Genre.builder().id("66f100000000000000000001").genreName("Action").description("Action movies").build(),
                    Genre.builder().id("66f100000000000000000002").genreName("Comedy").description("Comedy movies").build()
            ));
            System.out.println("Seeded Genres");
        }

        if (roomRepository.count() == 0) {
            roomRepository.saveAll(List.of(
                    CinemaRoom.builder().id("66f200000000000000000001").roomName("Room 1").roomType(RoomType.STANDARD).seatRows(10).seatsPerRow(10).totalSeats(100).status(RoomStatus.ACTIVE).build(),
                    CinemaRoom.builder().id("66f200000000000000000002").roomName("Room IMAX").roomType(RoomType.IMAX).seatRows(15).seatsPerRow(20).totalSeats(300).status(RoomStatus.ACTIVE).build()
            ));
            System.out.println("Seeded Cinema Rooms");
        }
    }
}
