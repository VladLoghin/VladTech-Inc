import "./Review.css";
import ReviewCard from "./ReviewCard.jsx";
import { Swiper, SwiperSlide } from "swiper/react";
import { Autoplay, Navigation, Pagination } from "swiper/modules";

// @ts-ignore
import "swiper/css";
// @ts-ignore
import "swiper/css/navigation";
// @ts-ignore
import "swiper/css/pagination";

interface Photo {
    clientId: string;
    filename: string;
    imageType: string;
    url: string;
}

interface Review {
    reviewId: string;
    clientId: string;
    appointmentId: string;
    comment: string;
    visible: boolean;
    photos?: Photo[];
    rating?: string;
}

interface ReviewCarouselProps {
    reviews: Review[];
    onReviewClick?: (review: Review) => void;
}

const ReviewCarousel = ({ reviews, onReviewClick}: ReviewCarouselProps) => {
    if (!reviews.length)
        return <p className="text-center" data-testid="no-reviews">No reviews available</p>;

    return (
        <Swiper
            modules={[Autoplay, Navigation, Pagination]}
            spaceBetween={20}
            slidesPerView={1}
            breakpoints={{
                640: { slidesPerView: 2, spaceBetween: 20 },
                768: { slidesPerView: 3, spaceBetween: 30 },
                1024: { slidesPerView: 4, spaceBetween: 40 },
            }}
            autoplay={{ delay: 10000, disableOnInteraction: false }}
            navigation
            pagination={{ clickable: true }}
            data-testid="review-carousel"
        >
            {reviews.map((review) => (
                <SwiperSlide key={review.reviewId} data-testid="review-slide">
                    <ReviewCard
                        review={review}
                        onClick={onReviewClick ? () => onReviewClick(review) : undefined}
                    />
                </SwiperSlide>
            ))}
        </Swiper>
    );
};

export default ReviewCarousel;
