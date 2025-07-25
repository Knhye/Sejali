package nahye.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "room_name")
    private String roomName;

    @Column(nullable = false)
    private int seats;

    @Column(nullable = false, name="remaining_seats")
    private int remainingSeats;

    @Column(nullable = false, name = "room_img")
    private String roomImg;

    @OneToMany(mappedBy = "rooms")
    private List<Reservation> reservations = new ArrayList<>();
}
