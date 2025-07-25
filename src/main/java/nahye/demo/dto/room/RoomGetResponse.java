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
public class RoomGetResponse {

    private String roomName;
    private int seats;
    private int remainingSeats;
    private String roomImg;

}
