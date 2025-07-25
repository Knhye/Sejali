package nahye.demo.controller;

import lombok.RequiredArgsConstructor;
import nahye.demo.dto.room.RoomResponse;
import nahye.demo.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping
    public ResponseEntity<?> getAllRooms(){
        try{
            List<RoomResponse> response = roomService.getAllRooms();
            return ResponseEntity.ok(response);
        } catch (Exception e){
            logger.error("오류 : ",e);
            return new ResponseEntity<>("서버 내부 오류", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
