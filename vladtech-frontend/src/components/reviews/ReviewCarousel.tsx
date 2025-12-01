import { useEffect, useState } from "react";
import { getAllVisibleReviews } from "../../api/reviews/reviewsService";
import "./review.css";
import ReviewCard from "./ReviewCard";

// Swiper imports
import { Swiper, SwiperSlide } from "swiper/react";
import { Autoplay, Navigation, Pagination } from "swiper/modules";
import "swiper/css";
import "swiper/css/navigation";
import "swiper/css/pagination";

interface Review {
    reviewId: string;
    clientId: string;
    appointmentId: string;
    comment: string;
    visible: boolean;
    photos?: string[];
    rating?: number;
}

const ReviewCarousel = () => {
    const [reviews, setReviews] = useState<Review[]>([]);

    useEffect(() => {
        getAllVisibleReviews()
            .then((data) => {
                console.log("LOADED REVIEWS:", data);
                setReviews(data);
            })
            .catch((err) => console.error("Failed to fetch reviews", err));
    }, []);

    if (!reviews.length) return <p className="text-center">No reviews available</p>;

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
            autoplay={{ delay: 3000, disableOnInteraction: false }}
            navigation
            pagination={{ clickable: true }}
        >
            {reviews.map((review) => (
                <SwiperSlide key={review.reviewId}>
                    <ReviewCard review={review} />
                </SwiperSlide>
            ))}
        </Swiper>
    );
};

export default ReviewCarousel;
