package nahye.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    private String token;

    @Column(nullable = false, name = "user_id")
    private String userId;

    @Column(name ="expiry_date", nullable = false)
    private LocalDateTime expiryDate;
}
