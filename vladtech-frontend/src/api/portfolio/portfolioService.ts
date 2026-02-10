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

export const archivePortfolioItem = async (
  portfolioId: string,
  accessToken: string
) => {
  try {
    await api.put(`${API_BASE}/${portfolioId}/archive`, {}, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    });
  } catch (error) {
    console.error("Error archiving portfolio item:", error);
    throw error;
  }
};

export const unarchivePortfolioItem = async (
  portfolioId: string,
  accessToken: string
) => {
  try {
    await api.put(`${API_BASE}/${portfolioId}/unarchive`, {}, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    });
  } catch (error) {
    console.error("Error unarchiving portfolio item:", error);
    throw error;
  }
};

export const getArchivedPortfolioItems = async (accessToken: string) => {
  try {
    const response = await api.get(`${API_BASE}/archived`, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching archived portfolio items:", error);
    throw error;
  }
};
