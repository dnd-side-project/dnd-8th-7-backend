package com.dnd8th.service;

import com.dnd8th.dao.review.ReviewUpdateDao;
import com.dnd8th.dto.review.ReviewCreateRequest;
import com.dnd8th.dto.review.ReviewGetResponse;
import com.dnd8th.dto.review.ReviewUpdateRequest;
import com.dnd8th.entity.Review;
import com.dnd8th.entity.User;
import com.dnd8th.error.exception.review.ReviewAccessDeniedException;
import com.dnd8th.error.exception.review.ReviewNotFoundException;
import com.dnd8th.error.exception.user.UserNotFoundException;
import com.dnd8th.repository.ReviewRepository;
import com.dnd8th.repository.UserRepository;
import com.dnd8th.util.DateParser;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final DateParser dateParser;
    private final ReviewUpdateDao reviewUpdateDao;

    public void createReview(ReviewCreateRequest reviewCreateRequest, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);
        Date date = dateParser.parseDate(reviewCreateRequest.getDate());

        reviewRepository.save(reviewCreateRequest.toEntity(user, date));
    }

    public void deleteReview(String userEmail, Long reviewId) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);
        User reviewOwner = review.getUser();
        if (reviewOwner != user) {
            throw new ReviewAccessDeniedException();
        }
        reviewRepository.delete(review);
    }

    public ReviewGetResponse getReview(String userEmail, Long reviewId) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);
        User reviewOwner = review.getUser();
        if (reviewOwner != user) {
            throw new ReviewAccessDeniedException();
        }
        String date = dateParser.toString(review.getDate());
        ReviewGetResponse reviewGetResponse = ReviewGetResponse.builder()
                .date(date)
                .emoji(review.getEmotion())
                .review(review.getRetrospection())
                .secret(review.getRetrospectionLock()).build();
        return reviewGetResponse;
    }

    public void updateReview(String userEmail, Long reviewId,
            ReviewUpdateRequest reviewUpdateRequest) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);
        User reviewOwner = review.getUser();
        if (reviewOwner != user) {
            throw new ReviewAccessDeniedException();
        }
        reviewUpdateDao.updateReview(reviewId, reviewUpdateRequest);
    }
}
