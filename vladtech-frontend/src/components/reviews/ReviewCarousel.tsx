import "./Review.css";
import ReviewCard from "./ReviewCard.jsx";
import { Swiper, SwiperSlide } from "swiper/react";
import { Autoplay, Navigation, Pagination } from "swiper/modules";

import "swiper/css";
import "swiper/css/navigation";
import "swiper/css/pagination";

const ReviewCarousel = ({ reviews, onReviewClick, onDelete }) => {
    if (!reviews.length) {
        return <p className="text-center">No reviews available</p>;
    }

    return (
        <Swiper
            modules={[Autoplay, Navigation, Pagination]}
            spaceBetween={20}
            slidesPerView={1}
            breakpoints={{
                640: { slidesPerView: 2 },
                768: { slidesPerView: 3 },
                1024: { slidesPerView: 4 },
            }}
            autoplay={{ delay: 10000 }}
            navigation
            pagination={{ clickable: true }}
        >
            {reviews.map((review) => (
                <SwiperSlide key={review.id ?? review.reviewId}>
                    <ReviewCard
                        review={review}
                        onClick={
                            onReviewClick
                                ? () => onReviewClick(review)
                                : undefined
                        }
                        onDelete={onDelete}
                    />
                </SwiperSlide>
            ))}
        </Swiper>
    );
};

export default ReviewCarousel;
