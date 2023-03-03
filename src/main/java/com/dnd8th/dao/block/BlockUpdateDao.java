package com.dnd8th.dao.block;

import static com.dnd8th.entity.QBlock.block;

import com.dnd8th.dto.block.BlockUpdateRequest;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class BlockUpdateDao {

    private final JPAQueryFactory queryFactory;

    public void updateBlock(Long blockId, BlockUpdateRequest blockUpdateRequest) {
        queryFactory.update(block)
                .set(block.title, blockUpdateRequest.getTitle())
                .set(block.emotion, blockUpdateRequest.getEmoji())
                .set(block.blockColor, blockUpdateRequest.getBackgroundColor())
                .set(block.blockLock, blockUpdateRequest.getIsSecret())
                .where(block.id.eq(blockId))
                .execute();
    }
}
