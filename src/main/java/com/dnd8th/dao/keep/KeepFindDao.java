package com.dnd8th.dao.keep;

import static com.dnd8th.entity.QKeep.keep;

import com.dnd8th.entity.Keep;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class KeepFindDao {

    private final JPAQueryFactory queryFactory;

    public List<Keep> findByEmail(String email) {
        return queryFactory.selectFrom(keep)
                .where(keep.user.email.eq(email))
                .orderBy(keep.createdAt.asc())
                .fetch();
    }
}
