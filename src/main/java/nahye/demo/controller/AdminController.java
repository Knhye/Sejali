package nahye.demo.controller;

import lombok.RequiredArgsConstructor;
import nahye.demo.dto.room.RoomRequest;
import nahye.demo.dto.room.RoomResponse;
import nahye.demo.dto.user.AdminUserResponse;
import nahye.demo.entity.Room;
import nahye.demo.entity.User;
import nahye.demo.service.RoomService;
import nahye.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final RoomService roomService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/all-users")
    //ADMIN 인 경우에만 요청 허용
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<AdminUserResponse> getAllUsers(){
        return userService.getAllUsers();
    }

    @PostMapping("/add-room")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> createRoom(@RequestBody RoomRequest request, Authentication authentication) {
        try{
            String userId = authentication.getName();
            RoomResponse response = roomService.createRoom(request, userId);
            return ResponseEntity.status(201).body(response);
        } catch(Exception e){
            logger.error("서버 오류 발생 : ", e);
            return ResponseEntity.status(500).body("서버 오류 발생 : " + e.getMessage());
        }

    }
}
