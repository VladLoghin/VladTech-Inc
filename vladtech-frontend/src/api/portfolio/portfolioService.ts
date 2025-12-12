import axios from "axios";

const API_BASE = "http://localhost:8080/api/portfolio";

export interface AddCommentRequest {
  text: string;
}

export interface PortfolioCommentDto {
  authorName: string;
  authorUserId: string;
  timestamp: string;
  text: string;
}

export const addComment = async (
  portfolioId: string,
  commentText: string,
  authorName: string,
  accessToken: string
): Promise<PortfolioCommentDto> => {
  try {
    const response = await axios.post(
      `${API_BASE}/${portfolioId}/comments`,
      { 
        text: commentText,
        authorName: authorName 
      },
      {
        headers: {
          Authorization: `Bearer ${accessToken}`,
          "Content-Type": "application/json",
        },
      }
    );
    return response.data;
  } catch (error) {
    console.error("Error adding comment:", error);
    throw error;
  }
};

export const getAllPortfolioItems = async () => {
  try {
    const response = await axios.get(API_BASE);
    return response.data;
  } catch (error) {
    console.error("Error fetching portfolio items:", error);
    throw error;
  }
};

export const getPortfolioItemById = async (portfolioId: string) => {
  try {
    const response = await axios.get(`${API_BASE}/${portfolioId}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching portfolio item:", error);
    throw error;
  }
};

export const createPortfolioItem = async (
  title: string,
  imageUrl: string,
  rating: number,
  accessToken: string
) => {
  try {
    const response = await axios.post(
      API_BASE,
      {
        title,
        imageUrl,
        rating,
      },
      {
        headers: {
          Authorization: `Bearer ${accessToken}`,
          "Content-Type": "application/json",
        },
      }
    );
    return response.data;
  } catch (error) {
    console.error("Error creating portfolio item:", error);
    throw error;
  }
};
