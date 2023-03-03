package com.dnd8th.dao.user;

import static com.dnd8th.entity.QUser.user;

import com.dnd8th.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserNameFindDuplicatedDao {

    private final JPAQueryFactory queryFactory;

    public boolean isNameDuplicated(String nickname) {
        User findUser = queryFactory.selectFrom(user)
                .where(user.name.eq(nickname))
                .fetchOne();

        return (findUser != null);
    }
}
