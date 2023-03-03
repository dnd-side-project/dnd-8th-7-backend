package com.dnd8th.dao.block;

import static com.dnd8th.entity.QBlock.block;
import static com.dnd8th.entity.QTask.task;

import com.dnd8th.entity.Block;
import com.dnd8th.entity.Task;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class BlockFindDao {

    private final JPAQueryFactory queryFactory;

    public List<String> findByEmailAndDate(String email, Date date) {
        return queryFactory.select(block.blockColor)
                .from(block)
                .where(block.user.email.eq(email), block.date.eq(date))
                .orderBy(block.createdAt.asc())
                .fetch();
    }

    public List<Block> getDailyBlock(String email, Date date) {
        return queryFactory
                .selectFrom(block)
                .where(block.user.email.eq(email), block.date.eq(date))
                .orderBy(block.createdAt.asc())
                .fetch();
    }

    public List<Task> getDailyTask(long bockId) {
        return queryFactory
                .selectFrom(task)
                .where(task.block.id.eq(bockId))
                .orderBy(task.createdAt.asc())
                .fetch();
    }
}
