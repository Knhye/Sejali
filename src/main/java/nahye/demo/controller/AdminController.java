package nahye.demo.controller;

import lombok.RequiredArgsConstructor;
import nahye.demo.dto.user.AdminUserResponse;
import nahye.demo.entity.User;
import nahye.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @GetMapping("/all-users")
    //ADMIN 인 경우에만 요청 허용
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<AdminUserResponse> getAllUsers(){
        return userService.getAllUsers();
    }
}
