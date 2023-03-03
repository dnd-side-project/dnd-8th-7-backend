package com.dnd8th.dao.review;

import static com.dnd8th.entity.QReview.review;

import com.dnd8th.dto.review.ReviewUpdateRequest;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ReviewUpdateDao {

    private final JPAQueryFactory queryFactory;

    public void updateReview(Long reviewId, ReviewUpdateRequest reviewUpdateRequest) {
        queryFactory.update(review)
                .set(review.retrospection, reviewUpdateRequest.getReview())
                .set(review.retrospectionLock, reviewUpdateRequest.isSecret())
                .set(review.emotion, reviewUpdateRequest.getEmoji())
                .where(review.id.eq(reviewId))
                .execute();
    }
}
