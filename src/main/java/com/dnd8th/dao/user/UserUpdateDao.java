package com.dnd8th.dao.user;

import static com.dnd8th.entity.QUser.user;

import com.dnd8th.dto.user.UserGetDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserUpdateDao {

    private final JPAQueryFactory queryFactory;

    public void updateUser(String email, UserGetDto userGetDto) {
        queryFactory.update(user)
                .set(user.imagePath, userGetDto.getImgUrl())
                .set(user.name, userGetDto.getNickname())
                .set(user.introduction, userGetDto.getIntroduction())
                .set(user.userLock, userGetDto.getSecret())
                .where(user.email.eq(email))
                .execute();
    }
}
