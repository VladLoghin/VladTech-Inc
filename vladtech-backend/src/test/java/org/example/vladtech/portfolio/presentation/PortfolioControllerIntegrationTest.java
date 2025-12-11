package org.example.vladtech.portfolio.presentation;

import org.example.vladtech.portfolio.data.PortfolioComment;
import org.example.vladtech.portfolio.data.PortfolioItem;
import org.example.vladtech.portfolio.data.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
class PortfolioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PortfolioRepository portfolioRepository;

    private PortfolioItem portfolioItem1;
    private PortfolioItem portfolioItem2;
    private PortfolioItem portfolioItem3;

    @BeforeEach
    void setup() {
        // Clean the database before each test
        portfolioRepository.deleteAll();

        Instant now = Instant.now();

        // Create test portfolio items
        portfolioItem1 = new PortfolioItem(
                "Modern Kitchen Counter",
                "/uploads/portfolio/kitchencounter.jpg",
                4.9,
                List.of(
                        new PortfolioComment("Sarah M.", "auth0|user1", now.minusSeconds(10800), "Beautiful countertop!"),
                        new PortfolioComment("John D.", "auth0|user2", now.minusSeconds(3600), "Love the modern design.")
                )
        );

        portfolioItem2 = new PortfolioItem(
                "Complete Kitchen Remodel",
                "/uploads/portfolio/kitchenremodel.jpg",
                5.0,
                List.of(
                        new PortfolioComment("Emma L.", "auth0|user3", now.minusSeconds(18000), "Amazing transformation!")
                )
        );

        portfolioItem3 = new PortfolioItem(
                "Luxury Bathroom Renovation",
                "/uploads/portfolio/newbathroom.jpg",
                4.8,
                List.of(
                        new PortfolioComment("Lisa K.", "auth0|user4", now.minusSeconds(14400), "Stunning bathroom design.")
                )
        );

        portfolioRepository.saveAll(List.of(portfolioItem1, portfolioItem2, portfolioItem3));
    }

    @Test
    void getAllPortfolioItems_ShouldReturnAllItems() throws Exception {
        mockMvc.perform(get("/api/portfolio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].title", is("Modern Kitchen Counter")))
                .andExpect(jsonPath("$[0].imageUrl", is("/uploads/portfolio/kitchencounter.jpg")))
                .andExpect(jsonPath("$[0].rating", is(4.9)))
                .andExpect(jsonPath("$[0].comments", hasSize(2)))
                .andExpect(jsonPath("$[1].title", is("Complete Kitchen Remodel")))
                .andExpect(jsonPath("$[1].rating", is(5.0)))
                .andExpect(jsonPath("$[2].title", is("Luxury Bathroom Renovation")));
    }

    @Test
    void getAllPortfolioItems_WhenNoItems_ShouldReturnEmptyList() throws Exception {
        portfolioRepository.deleteAll();

        mockMvc.perform(get("/api/portfolio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getPortfolioItemById_WhenValidId_ShouldReturnItem() throws Exception {
        String portfolioId = portfolioItem1.getPortfolioId();

        mockMvc.perform(get("/api/portfolio/{portfolioId}", portfolioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioId", is(portfolioId)))
                .andExpect(jsonPath("$.title", is("Modern Kitchen Counter")))
                .andExpect(jsonPath("$.imageUrl", is("/uploads/portfolio/kitchencounter.jpg")))
                .andExpect(jsonPath("$.rating", is(4.9)))
                .andExpect(jsonPath("$.comments", hasSize(2)))
                .andExpect(jsonPath("$.comments[0].authorName", is("Sarah M.")))
                .andExpect(jsonPath("$.comments[0].timestamp").exists())
                .andExpect(jsonPath("$.comments[0].text", is("Beautiful countertop!")))
                .andExpect(jsonPath("$.comments[1].authorName", is("John D.")));
    }


    @Test
    void getAllPortfolioItems_ShouldIncludeAllComments() throws Exception {
        mockMvc.perform(get("/api/portfolio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comments[0].authorName", is("Sarah M.")))
                .andExpect(jsonPath("$[0].comments[1].authorName", is("John D.")));
    }

    @Test
    void getPortfolioItemById_ShouldReturnCorrectRating() throws Exception {
        String portfolioId = portfolioItem2.getPortfolioId();

        mockMvc.perform(get("/api/portfolio/{portfolioId}", portfolioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(5.0)))
                .andExpect(jsonPath("$.title", is("Complete Kitchen Remodel")));
    }

    @Test
    void getAllPortfolioItems_ShouldReturnItemsWithCorrectImageUrls() throws Exception {
        mockMvc.perform(get("/api/portfolio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].imageUrl", is("/uploads/portfolio/kitchencounter.jpg")))
                .andExpect(jsonPath("$[1].imageUrl", is("/uploads/portfolio/kitchenremodel.jpg")))
                .andExpect(jsonPath("$[2].imageUrl", is("/uploads/portfolio/newbathroom.jpg")));
    }

    @Test
    void getPortfolioItemById_WithNoComments_ShouldReturnEmptyCommentsList() throws Exception {
        PortfolioItem itemWithoutComments = new PortfolioItem(
                "Simple Office",
                "/uploads/portfolio/newoffice.jpg",
                4.5,
                List.of()
        );
        portfolioRepository.save(itemWithoutComments);
        String portfolioId = itemWithoutComments.getPortfolioId();

        mockMvc.perform(get("/api/portfolio/{portfolioId}", portfolioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(0)))
                .andExpect(jsonPath("$.title", is("Simple Office")))
                .andExpect(jsonPath("$.rating", is(4.5)));
    }
}

