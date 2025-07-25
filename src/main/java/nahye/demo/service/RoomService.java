package nahye.demo.service;

import lombok.RequiredArgsConstructor;
import nahye.demo.dto.room.RoomResponse;
import nahye.demo.entity.Room;
import nahye.demo.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    public List<RoomResponse> getAllRooms() {
        List<Room> rooms = roomRepository.findAll();

        return rooms.stream()
                .map(room -> new RoomResponse(
                        room.getRoomName(),
                        room.getSeats(),
                        room.getRemainingSeats(),
                        room.getRoomImg()
                ))
                .collect(Collectors.toList());
    }

}
