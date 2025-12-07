import { useState } from "react";
import { X } from "lucide-react";
import { useAuth0 } from "@auth0/auth0-react";

interface ReviewModalProps {
    open: boolean;
    onClose: () => void;
    onSubmitSuccess?: (newReview: any) => void;
    appointmentId?: string;
}

export default function ReviewModal({ open, onClose, onSubmitSuccess, appointmentId }: ReviewModalProps) {
    const { getAccessTokenSilently } = useAuth0();

    if (!open) return null;

    const [clientName, setClientName] = useState("");
    const [comment, setComment] = useState("");
    const [stars, setStars] = useState<1 | 2 | 3 | 4 | 5>(5);
    const [imageFile, setImageFile] = useState<File | null>(null);

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();

        const formData = new FormData();
        const reviewPayload = {
            clientId: clientName,
            appointmentId: appointmentId || "temp-appointment",
            comment,
            visible: true,
            rating: stars - 1, // backend enum [0..4]
        };

        formData.append(
            "review",
            new Blob([JSON.stringify(reviewPayload)], { type: "application/json" })
        );

        if (imageFile) {
            formData.append("photos", imageFile);
        }

        try {
            const token = await getAccessTokenSilently();
            const res = await fetch("http://localhost:8080/api/reviews", {
                method: "POST",
                body: formData,
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!res.ok) {
                console.error("Failed to submit review:", await res.text());
                return;
            }

            const createdReview = await res.json(); // get created review from backend

            if (onSubmitSuccess) onSubmitSuccess(createdReview);
            onClose();

            // Reset form
            setClientName("");
            setComment("");
            setStars(5);
            setImageFile(null);
        } catch (err) {
            console.error("Error submitting review:", err);
        }
    }

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
            <div className="bg-white rounded-2xl p-6 w-full max-w-md shadow-xl">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-xl font-semibold">Leave a Review</h2>
                    <button onClick={onClose}><X className="w-6 h-6" /></button>
                </div>

                <form className="space-y-5" onSubmit={handleSubmit}>
                    <input
                        type="text"
                        placeholder="Your name"
                        value={clientName}
                        onChange={(e) => setClientName(e.target.value)}
                        className="w-full border border-gray-300 rounded-xl p-3"
                        required
                    />
                    <textarea
                        placeholder="Your message"
                        value={comment}
                        onChange={(e) => setComment(e.target.value)}
                        className="w-full border border-gray-300 rounded-xl p-3 h-24"
                        required
                    />
                    <div className="flex gap-1">
                        {[1, 2, 3, 4, 5].map((s) => (
                            <button
                                key={s}
                                type="button"
                                onClick={() => setStars(s as 1 | 2 | 3 | 4 | 5)}
                            >
                                <span className={`text-2xl ${s <= stars ? "text-yellow-500" : "text-gray-300"}`}>
                                    ★
                                </span>
                            </button>
                        ))}
                    </div>
                    <div>
                        <label className="block mb-1 font-medium">Upload Photo</label>
                        <input
                            type="file"
                            accept="image/*"
                            onChange={(e) => setImageFile(e.target.files?.[0] ?? null)}
                            className="w-full border border-gray-300 rounded-xl p-2"
                        />
                    </div>
                    <button
                        type="submit"
                        className="w-full bg-blue-600 text-white py-3 rounded-xl font-semibold"
                    >
                        Submit Review
                    </button>
                </form>
            </div>
        </div>
    );
}
