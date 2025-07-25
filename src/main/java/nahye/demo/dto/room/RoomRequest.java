package nahye.demo.dto.room;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RoomRequest {
    private String roomName;
    private int seats;
    private String roomImg;
}
