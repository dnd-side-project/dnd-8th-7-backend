package com.dnd8th.api;

import com.dnd8th.dto.user.UserGetResponse;
import com.dnd8th.entity.User;
import com.dnd8th.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApi {

    private final UserService userService;

    @DeleteMapping("")
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();

        userService.deleteUser(email);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
    }

    @GetMapping("")
    public ResponseEntity<UserGetResponse> getUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();

        User user = userService.getUser(email);
        UserGetResponse userGetResponse = new UserGetResponse(
                user.getImagePath(), user.getName(), user.getIntroduction(), user.getUserLock());
        return ResponseEntity.ok(userGetResponse);
    }
}
