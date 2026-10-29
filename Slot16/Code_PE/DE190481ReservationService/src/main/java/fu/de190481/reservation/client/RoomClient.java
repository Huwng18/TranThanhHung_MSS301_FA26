package fu.de190481.reservation.client;

import fu.de190481.reservation.dto.ApiResponse;
import fu.de190481.reservation.dto.RoomDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "room-service", url = "${room.service.url:http://localhost:8081}")
public interface RoomClient {

    @GetMapping("/api/rooms/{roomId}")
    ApiResponse<RoomDTO> getRoomById(@PathVariable("roomId") Integer roomId);
}
