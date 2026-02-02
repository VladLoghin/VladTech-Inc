package org.example.vladtech.contact.businesslayer;

import org.example.vladtech.contact.datamapperlayer.ContactEmailMapper;
import org.example.vladtech.contact.dataaccesslayer.ContactEmailSender;
import org.example.vladtech.contact.datamapperlayer.ReviewNotificationEmailMapper;
import org.example.vladtech.contact.domain.ContactEmail;
import org.example.vladtech.contact.domain.ReviewNotificationEmail;
import org.example.vladtech.contact.presentationlayer.ContactRequestDto;
import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.presentation.ReviewRequestModel;
import org.springframework.stereotype.Service;

@Service
public class ContactServiceImpl implements ContactService {

    private final ContactEmailMapper mapper;
    private final ContactEmailSender emailSender;

    private final ReviewNotificationEmailMapper reviewNotificationEmailMapper;
    public ContactServiceImpl(ContactEmailMapper mapper,
                              ContactEmailSender emailSender, ReviewNotificationEmailMapper reviewNotificationEmailMapper) {
        this.mapper = mapper;
        this.emailSender = emailSender;
        this.reviewNotificationEmailMapper = reviewNotificationEmailMapper;
    }

    @Override
    public void sendContactMessage(ContactRequestDto requestDto, String name, String senderEmail) {
        // Convert DTO → domain object with user's name
        ContactEmail contactEmail = mapper.toContactEmail(requestDto, name, senderEmail);

        // Send the email
        emailSender.send(contactEmail, name);
    }


    @Override
    public void notifyAdminReviewSubmitted(ReviewRequestModel review) {
        ContactEmail email = reviewNotificationEmailMapper.toNotificationEmail(review);
        emailSender.send(email, "System");
    }

}
