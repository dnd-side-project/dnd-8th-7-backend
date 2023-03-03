package com.dnd8th.api;

import com.dnd8th.dao.user.UserNameFindDuplicatedDao;
import com.dnd8th.dto.user.UserGetDto;
import com.dnd8th.dto.user.UserGetResponse;
import com.dnd8th.dto.user.UserNameDuplicatedResponse;
import com.dnd8th.entity.User;
import com.dnd8th.service.AwsS3Service;
import com.dnd8th.service.UserService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApi {

    private final UserService userService;
    private final AwsS3Service awsS3Service;
    private final UserNameFindDuplicatedDao userNameFindDuplicatedDao;

    @DeleteMapping("")
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();

        userService.deleteUser(email);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
    }

    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<UserNameDuplicatedResponse> getNameDuplicated(
            @PathVariable String nickname) {
        boolean nameDuplicated = userNameFindDuplicatedDao.isNameDuplicated(nickname);

        UserNameDuplicatedResponse userNameDuplicatedResponse = UserNameDuplicatedResponse.builder()
                .duplicated(nameDuplicated)
                .build();

        return ResponseEntity.ok(userNameDuplicatedResponse);
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uploadProfileImage(
            @RequestPart("image")
            MultipartFile multipartFile,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        String imagePath = awsS3Service.uploadImageToS3(multipartFile, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(imagePath);
    }

    @GetMapping("")
    public ResponseEntity<UserGetResponse> getUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();

        User user = userService.getUser(email);
        UserGetResponse userGetResponse = UserGetResponse.builder()
                .imgUrl(user.getImagePath())
                .nickname(user.getName())
                .introduction(user.getIntroduction())
                .secret(user.getUserLock()).build();
        return ResponseEntity.ok(userGetResponse);
    }

    @PatchMapping("")
    public ResponseEntity<String> patchUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid UserGetResponse userGetResponse) {
        String email = userDetails.getUsername();

        UserGetDto userGetDto = UserGetDto.builder()
                .imgUrl(userGetResponse.getImgUrl())
                .nickname(userGetResponse.getNickname())
                .introduction(userGetResponse.getIntroduction())
                .secret(userGetResponse.getSecret()).build();
        userService.updateUser(email, userGetDto);
        return ResponseEntity.ok("");
    }
}
