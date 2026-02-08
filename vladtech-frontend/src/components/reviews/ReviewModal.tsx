import { useEffect, useState } from "react";
import { X } from "lucide-react";
import { useAuth0 } from "@auth0/auth0-react";
import { api } from "../../api/http";

interface ProjectOption {
    projectIdentifier: string;
    name: string;
}

interface ReviewModalProps {
    open: boolean;
    onClose: () => void;
    onSubmitSuccess?: (newReview: any) => void;
    appointmentId?: string;
}

export default function ReviewModal({ open, onClose, onSubmitSuccess, appointmentId }: ReviewModalProps) {

    const { getAccessTokenSilently, user } = useAuth0();
    
    const clientId = user?.sub;

    const [clientName, setClientName] = useState("");
    const [comment, setComment] = useState("");
    const [stars, setStars] = useState<1 | 2 | 3 | 4 | 5>(5);
    const [imageFile, setImageFile] = useState<File | null>(null);
    const [projectId, setProjectId] = useState("");
    const [projects, setProjects] = useState<ProjectOption[]>([]);
    const [loadingProjects, setLoadingProjects] = useState(false);
    const [errors, setErrors] = useState<{ name?: string; comment?: string; projectId?: string; submit?: string }>({});

    const reviewTypes = ["Interior", "Kitchen", "Bathroom", "Exterior/Yard"];

    const [type, setType] = useState("Interior");

    useEffect(() => {
        if (!open) return;

        const fetchProjects = async () => {
            setLoadingProjects(true);
            try {
                const token = await getAccessTokenSilently({
                    authorizationParams: { audience: "https://vladtech/api" },
                });

                const res = await api.get("/projects/client/completed", {
                    headers: { Authorization: `Bearer ${token}` },
                });

                const projectOptions: ProjectOption[] = res.data.map((p: any) => ({
                    projectIdentifier: p.projectIdentifier,
                    name: p.name,
                }));

                setProjects(projectOptions);

                if (projectOptions.length > 0) {
                    setProjectId(projectOptions[0].projectIdentifier); // auto‑select first
                }
            } catch {
                setErrors({ submit: "Failed to load projects." });
            } finally {
                setLoadingProjects(false);
            }
        };

        fetchProjects();
    }, [open, getAccessTokenSilently]);

    if (!open) return null;

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();

        const newErrors: { name?: string; comment?: string; projectId?: string } = {};
        if (!clientName.trim()) newErrors.name = "Name is required";
        if (!comment.trim()) newErrors.comment = "Description is required";
        if (!projectId) newErrors.projectId = "Please select a project";

        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        setErrors({});

        const ratingEnum = ["ONE", "TWO", "THREE", "FOUR", "FIVE"][stars - 1];

        const reviewPayload = {
            projectId,
            clientId,
            clientName,
            appointmentId: appointmentId || "temp-appointment",
            comment,
            visible: false,
            rating: ratingEnum,
            sentToPortfolio: false,
            type
        };

        const formData = new FormData();
        formData.append(
            "review",
            new Blob([JSON.stringify(reviewPayload)], {
                type: "application/json;charset=UTF-8",
            })
        );

        if (imageFile) {
            formData.append("photos", imageFile);
        }

        try {
            const token = await getAccessTokenSilently({
                authorizationParams: { audience: "https://vladtech/api" },
            });

            const res = await api.post("/reviews", formData, {
                headers: { Authorization: `Bearer ${token}` },
            });

            const createdReview = res.data;
            onSubmitSuccess?.(createdReview);
            onClose();

            setClientName("");
            setComment("");
            setStars(5);
            setImageFile(null);
            setProjectId("");
        } catch (err: any) {
            const status = err?.response?.status;
            if (status === 409) {
                setErrors({ submit: "You already reviewed this project." });
            } else if (status === 403) {
                const data = err?.response?.data;
                if (data?.permanent) {
                    setErrors({ submit: "You are permanently banned from creating reviews." });
                } else if (data?.banUntil) {
                    const until = new Date(data.banUntil).toLocaleString();
                    setErrors({ submit: `You are banned from creating reviews until ${until}.` });
                } else {
                    setErrors({ submit: "You are banned from creating reviews." });
                }
            } else {
                setErrors({ submit: "Error submitting review." });
            }
        }
    }

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50" onClick={onClose}>
            <div className="bg-white rounded-2xl p-6 w-full max-w-md shadow-xl" onClick={(e) => e.stopPropagation()}>
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-xl font-semibold">Leave a Review</h2>
                    <button onClick={onClose}><X className="w-6 h-6" /></button>
                </div>

                <form className="space-y-5" onSubmit={handleSubmit}>
                    <div>
                        <label className="block text-sm font-semibold mb-2">
                            Project <span className="text-red-500">*</span>
                        </label>
                        <select
                            value={projectId}
                            onChange={(e) => setProjectId(e.target.value)}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                            required
                            disabled={loadingProjects || projects.length === 0}
                        >
                            {projects.map((p) => (
                                <option key={p.projectIdentifier} value={p.projectIdentifier}>
                                    {p.name}
                                </option>
                            ))}
                        </select>
                        {errors.projectId && <p className="text-red-600 text-sm font-semibold mt-1">{errors.projectId}</p>}
                    </div>

                    <div>
                        <input
                            type="text"
                            placeholder="Your name"
                            value={clientName}
                            onChange={(e) => setClientName(e.target.value)}
                            className="w-full border border-gray-300 rounded-xl p-3"
                            required
                        />
                        {errors.name && <p className="text-red-600 text-sm font-semibold mt-1">{errors.name}</p>}
                    </div>

                    <div>
                        <textarea
                            placeholder="Your message"
                            value={comment}
                            onChange={(e) => setComment(e.target.value)}
                            className="w-full border border-gray-300 rounded-xl p-3 h-24"
                            required
                        />
                        {errors.comment && <p className="text-red-600 text-sm font-semibold mt-1">{errors.comment}</p>}
                    </div>

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
                        <label className="block text-sm font-semibold mb-2">
                            Project Type <span className="text-red-500">*</span>
                        </label>
                        <select
                            value={type}
                            onChange={(e) => setType(e.target.value)}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                            required
                        >
                            {reviewTypes.map((t) => (
                                <option key={t} value={t}>{t}</option>
                            ))}
                        </select>
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

                   

                    {errors.submit && (
                        <p className="text-red-600 text-sm font-semibold">{errors.submit}</p>
                    )}

                    <button
                        type="submit"
                        style={{ backgroundColor: '#FCC700' }}
                        className="w-full text-black py-3 rounded-xl font-semibold"
                    >
                        Submit Review
                    </button>
                </form>
            </div>
        </div>
    );
}
