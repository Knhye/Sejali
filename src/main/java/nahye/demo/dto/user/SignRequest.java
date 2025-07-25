package nahye.demo.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import nahye.demo.enums.AuthLevel;

@Getter
@Setter
@AllArgsConstructor
public class SignRequest {
    private int studentNum;
    private String username;
    private String userId;
    private String password;
    private AuthLevel authLevel;
}
