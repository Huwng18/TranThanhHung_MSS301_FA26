package fu.de190481.room.repository;

import fu.de190481.room.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    boolean existsByRoomNumber(String roomNumber);

    boolean existsByRoomNumberAndRoomIdNot(String roomNumber, Integer roomId);

    Optional<Room> findByRoomNumber(String roomNumber);

    @Query("SELECT r FROM Room r WHERE " +
           "(:roomType IS NULL OR r.roomType = :roomType) AND " +
           "(:status IS NULL OR r.status = :status)")
    Page<Room> searchRooms(@Param("roomType") String roomType,
                           @Param("status") String status,
                           Pageable pageable);
}
