package org.example.vladtech.contact.businesslayer;

import org.example.vladtech.contact.presentationlayer.ContactRequestDto;
import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.presentation.ReviewRequestModel;
import org.springframework.security.oauth2.jwt.Jwt;

public interface ContactService {

    void sendContactMessage(ContactRequestDto requestDto, String name, String senderEmail);
    void notifyAdminReviewSubmitted(ReviewRequestModel review);
}
