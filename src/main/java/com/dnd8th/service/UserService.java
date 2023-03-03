package com.dnd8th.service;

import com.dnd8th.auth.jwt.JwtProviderService;
import com.dnd8th.dao.user.UserFindDao;
import com.dnd8th.dao.user.UserUpdateDao;
import com.dnd8th.dto.auth.UserLoginRequest;
import com.dnd8th.dto.auth.UserSignUpResponse;
import com.dnd8th.dto.user.UserGetDto;
import com.dnd8th.entity.User;
import com.dnd8th.error.exception.auth.EmailDuplicateException;
import com.dnd8th.error.exception.user.UserNotFoundException;
import com.dnd8th.repository.UserRepository;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtProviderService jwtProviderService;
    private final UserFindDao userFindDao;
    private final UserUpdateDao userUpdateDao;


    public User getUserByAccessToken(HttpServletRequest request) {
        String email = getEmailFromAuthentication(request);

        User user = findUserByEmail(email);

        return user;
    }

    public boolean existsByEmail(String email) {
        boolean existsByEmail = userRepository.existsByEmail(email);

        return existsByEmail;
    }

    private User findUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);

        return user;
    }

    private String getEmailFromAuthentication(HttpServletRequest request) {
        String email = jwtProviderService.getEmailFromHeaderAccessToken(request);

        return email;
    }

    public UserSignUpResponse signUp(final UserLoginRequest userLoginRequest) {

        if (userRepository.existsByEmail(userLoginRequest.getEmail())) {
            throw new EmailDuplicateException();
        }

        User user = userLoginRequest.toEntity();

        User savedUser = userRepository.save(user);

        return UserSignUpResponse.builder()
                .user(savedUser)
                .build();
    }

    public void deleteUser(final String email) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);

        userRepository.delete(user);
    }

    public User getUser(String email) {
        User user = userFindDao.findUserByEmail(email);
        if (user == null) {
            throw new UserNotFoundException();
        }
        return userFindDao.findUserByEmail(email);
    }

    public void updateUser(String email, UserGetDto userGetDto) {
        userUpdateDao.updateUser(email, userGetDto);
    }
}
