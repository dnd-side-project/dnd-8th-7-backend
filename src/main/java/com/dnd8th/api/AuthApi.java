package com.dnd8th.api;

import com.dnd8th.auth.jwt.JwtProviderService;
import com.dnd8th.dto.auth.GoogleOAuthTokenResponse;
import com.dnd8th.dto.auth.GoogleOAuthUserInfoResponse;
import com.dnd8th.dto.auth.UserLoginRequest;
import com.dnd8th.dto.auth.UserLoginResponse;
import com.dnd8th.error.exception.auth.AccessTokenNotFoundException;
import com.dnd8th.error.exception.auth.ExternalApiFailException;
import com.dnd8th.service.UserService;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthApi {

    private final JwtProviderService jwtProviderService;
    private final UserService userService;

    @Value("${app.auth.redirect-uri}")
    private String redirectUri;

    @Value("${app.auth.client-id}")
    private String googleClientId;

    @Value("${app.auth.client-secret}")
    private String googleClientSecret;

    @GetMapping("/")
    public ResponseEntity<String> getHealthCheck() {
        return ResponseEntity.ok("");
    }

    @GetMapping("/api/login/google")
    public ResponseEntity<String> googleLogin(HttpServletRequest request) {
        // google login URL ??????
        String redirectUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "response_type=code" +
                "&client_id=" + googleClientId +
                "&scope=email%20profile" +
                "&redirect_uri=" + redirectUri;

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();
    }

    @PostMapping("/api/login/callback/google")
    public ResponseEntity<UserLoginResponse> googleCallback(@RequestParam String code) {
        // code??? access_token ????????????
        ResponseEntity<GoogleOAuthTokenResponse> responseEntity = getTokenResponse(code);
        String accessToken = Optional.ofNullable(responseEntity.getBody())
                .orElseThrow(AccessTokenNotFoundException::new)
                .getAccessToken();

        // access_token?????? ????????? ?????? ????????????
        ResponseEntity<GoogleOAuthUserInfoResponse> userInfo = getUserInfo(accessToken);
        String email = Optional.ofNullable(userInfo.getBody())
                .orElseThrow(ExternalApiFailException::new)
                .getEmail();

        String randomUserName = "user@" + UUID.randomUUID().toString();

        Boolean isNewUser = false;

        //email??? ?????? ?????? ?????? ????????? ?????? ???, ??????????????? ????????????
        if (!userService.existsByEmail(email)) {
            userService.signUp(UserLoginRequest.builder()
                    .email(email)
                    .nickname(randomUserName)
                    .imageUrl(
                            "https://harublock-image-bucket.s3.ap-northeast-2.amazonaws.com/onboarding/default_profile_image.png")
                    .build());
            isNewUser = true;
        }

        // JWT token ??????
        String jwtToken = jwtProviderService.generateToken(email,
                accessToken);

        UserLoginResponse userLoginResponse = UserLoginResponse.builder()
                .token(jwtToken)
                .isNewUser(isNewUser)
                .build();

        // ?????????????????? JWT token ??????
        return ResponseEntity.ok()
                .body(userLoginResponse);
    }

    @PostMapping("/api/auth/token/{token}")
    public ResponseEntity<String> refreshToken(@PathVariable String token) {
        String newToken = jwtProviderService.refreshToken(token);
        return ResponseEntity.status(HttpStatus.OK).body(newToken);
    }

    private ResponseEntity<GoogleOAuthTokenResponse> getTokenResponse(String code) {
        // ?????? API??? ???????????? access token ?????? ????????????
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        try {
            return restTemplate.postForEntity(url,
                    params, GoogleOAuthTokenResponse.class);
        } catch (HttpClientErrorException exception) {
            throw new ExternalApiFailException();
        }

    }

    private ResponseEntity<GoogleOAuthUserInfoResponse> getUserInfo(String accessToken) {
        // ?????? API??? ???????????? ????????? ?????? ????????????
        RestTemplate restTemplate = new RestTemplate();

        String url = "https://www.googleapis.com/oauth2/v2/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>("", headers);

        try {
            return restTemplate.exchange(url,
                    HttpMethod.GET, entity, GoogleOAuthUserInfoResponse.class);
        } catch (HttpClientErrorException exception) {
            throw new ExternalApiFailException();
        }
    }
}
