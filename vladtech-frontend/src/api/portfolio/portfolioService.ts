//import axios from "axios";
import { api } from "../http";


const API_BASE = "/portfolio";

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
    const response = await api.post(
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

export const getAllPortfolioItems = async (type?: string) => {
  try {
    const params = type ? { type } : {};
    const response = await api.get(API_BASE, { params });
    return response.data;
  } catch (error) {
    console.error("Error fetching portfolio items:", error);
    throw error;
  }
};

export const getPortfolioItemById = async (portfolioId: string) => {
  try {
    const response = await api.get(`${API_BASE}/${portfolioId}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching portfolio item:", error);
    throw error;
  }
};

export const createPortfolioItem = async (
  title: string,
  imageUrl: string,
  imageUrls: string[],
  type: string,
  accessToken: string
) => {
  try {
    const response = await api.post(
      API_BASE,
      {
        title,
        imageUrl,
        imageUrls,
        type,
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

export const updatePortfolioItem = async (
  portfolioId: string,
  title: string,
  imageUrls: string[],
  type: string,
  accessToken: string
) => {
  try {
    const response = await api.put(
      `${API_BASE}/${portfolioId}`,
      {
        title,
        imageUrls,
        type,
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
    console.error("Error updating portfolio item:", error);
    throw error;
  }
};

export const deletePortfolioItem = async (
  portfolioId: string,
  accessToken: string
) => {
  try {
    await api.delete(`${API_BASE}/${portfolioId}`, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    });
  } catch (error) {
    console.error("Error deleting portfolio item:", error);
    throw error;
  }
};
