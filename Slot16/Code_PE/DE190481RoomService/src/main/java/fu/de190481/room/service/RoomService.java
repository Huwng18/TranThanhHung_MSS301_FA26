package fu.de190481.room.service;

import fu.de190481.room.dto.ApiResponse;
import fu.de190481.room.dto.PageDTO;
import fu.de190481.room.dto.RoomDTO;

public interface RoomService {

    ApiResponse<RoomDTO> createRoom(RoomDTO roomDTO);

    ApiResponse<RoomDTO> updateRoom(Integer roomId, RoomDTO roomDTO);

    ApiResponse<RoomDTO> getRoomById(Integer roomId);

    ApiResponse<Void> deleteRoom(Integer roomId);

    ApiResponse<PageDTO<RoomDTO>> getRooms(int page, int size, String roomType, String status);
}
