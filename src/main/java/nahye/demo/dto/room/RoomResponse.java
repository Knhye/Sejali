package nahye.demo.dto.room;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import nahye.demo.entity.Reservation;
import nahye.demo.entity.Room;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RoomResponse {
    private Long id;
    private String roomName;
    private int seats;
    private int remainingSeats;
    private String roomImg;
    private List<Reservation> reservations;

    public RoomResponse(Room room){
        this.id = room.getId();
        this.roomName = room.getRoomName();
        this.seats = room.getSeats();
        this.remainingSeats = room.getRemainingSeats();
        this.roomImg = room.getRoomImg() ;
        this.reservations = room.getReservations();
    }

    public RoomResponse(String roomName, int seats, int remainingSeats, String roomImg){
        this.roomName = roomName;
        this.seats = seats;
        this.remainingSeats = remainingSeats;
        this.roomImg = roomImg;
    }
}
