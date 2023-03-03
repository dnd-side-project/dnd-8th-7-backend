package com.dnd8th.dao.task;

import static com.dnd8th.entity.QTask.task;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class TaskUpdateDao {

    private final JPAQueryFactory queryFactory;

    public void updateContent(Long taskId, String content) {
        queryFactory.update(task)
                .set(task.contents, content)
                .where(task.id.eq(taskId))
                .execute();
    }

    public void updateStatus(Long taskId) {
        Boolean status = queryFactory.select(task.status)
                .from(task)
                .where(task.id.eq(taskId))
                .fetchOne();
        status = !status;
        queryFactory.update(task)
                .set(task.status, status)
                .where(task.id.eq(taskId))
                .execute();
    }

}
