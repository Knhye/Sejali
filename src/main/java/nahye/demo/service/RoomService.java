package nahye.demo.service;

import lombok.RequiredArgsConstructor;
import nahye.demo.dto.room.RoomGetResponse;
import nahye.demo.dto.room.RoomRequest;
import nahye.demo.dto.room.RoomResponse;
import nahye.demo.entity.Room;
import nahye.demo.entity.User;
import nahye.demo.repository.RoomRepository;
import nahye.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public List<RoomGetResponse> getAllRooms() {
        List<Room> rooms = roomRepository.findAll();

        return rooms.stream()
                .map(room -> new RoomGetResponse(
                        room.getRoomName(),
                        room.getSeats(),
                        room.getRemainingSeats(),
                        room.getRoomImg()
                ))
                .collect(Collectors.toList());
    }

    public RoomResponse createRoom(RoomRequest request, String userId) {

        userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Room isExisting = roomRepository.findByRoomName(request.getRoomName());

        if(isExisting != null){
            throw new IllegalArgumentException("이미 존재하는 실습실입니다.");
        }

        Room room = Room.builder()
                .roomName(request.getRoomName())
                .seats(request.getSeats())
                .remainingSeats(request.getSeats())
                .roomImg(request.getRoomImg())
                .build();

        Room newRoom = roomRepository.save(room);
        return new RoomResponse(newRoom);
    }
}
