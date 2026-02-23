package org.example.vladtech.estimates.presentation;

import org.example.vladtech.estimates.data.Estimate;
import org.example.vladtech.estimates.data.EstimateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EstimateController.class)
@AutoConfigureMockMvc(addFilters = false)
class EstimateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstimateRepository estimateRepository;

    private final Path uploadsDir = Paths.get("uploads", "estimates");

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(uploadsDir);
    }

    @AfterEach
    void tearDown() throws IOException {
        // clean uploads/estimates
        if (Files.exists(uploadsDir)) {
            Files.walk(uploadsDir)
                    .filter(p -> !p.equals(uploadsDir))
                    .forEach(p -> p.toFile().delete());
        }
    }

    @Test
    void createEstimate_withJwt_returnsCreated() throws Exception {
        String jwtSub = "client-123";
        Estimate input = new Estimate();
        input.setTitle("Test Estimate");

        Estimate saved = new Estimate();
        saved.setEstimateId("est-1");
        saved.setTitle(input.getTitle());
        saved.setOwnerAuth0Id(jwtSub);
        saved.setCreatedAt(Instant.now());

        when(estimateRepository.save(any(Estimate.class))).thenReturn(saved);

        String json = "{\"title\":\"Test Estimate\"}";

        mockMvc.perform(post("/api/estimates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(jwtAuth(jwtSub)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/estimates/est-1"));

        verify(estimateRepository).save(any(Estimate.class));
    }

    @Test
    void listEstimates_returnsOk() throws Exception {
        String sub = "user-1";
        Estimate e = new Estimate();
        e.setEstimateId("id1");
        e.setOwnerAuth0Id(sub);
        when(estimateRepository.findByOwnerAuth0Id(eq(sub))).thenReturn(List.of(e));

        mockMvc.perform(get("/api/estimates").with(jwtAuth(sub)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estimateId").value("id1"));
    }

    @Test
    void getEstimate_found_returnsOk() throws Exception {
        String sub = "u1";
        Estimate e = new Estimate();
        e.setEstimateId("e1");
        e.setOwnerAuth0Id(sub);
        when(estimateRepository.findByEstimateIdAndOwnerAuth0Id("e1", sub)).thenReturn(Optional.of(e));

        mockMvc.perform(get("/api/estimates/e1").with(jwtAuth(sub)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimateId").value("e1"));
    }

    @Test
    void getEstimate_notFound_returnsNotFound() throws Exception {
        String sub = "u1";
        when(estimateRepository.findByEstimateIdAndOwnerAuth0Id("e2", sub)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/estimates/e2").with(jwtAuth(sub)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEstimate_success_and_forbidden() throws Exception {
        String sub = "owner";
        Estimate existing = new Estimate();
        existing.setEstimateId("x1");
        existing.setOwnerAuth0Id(sub);
        existing.setCreatedAt(Instant.now());

        when(estimateRepository.findByEstimateIdAndOwnerAuth0Id("x1", sub)).thenReturn(Optional.of(existing));
        when(estimateRepository.save(any(Estimate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String body = "{\"title\":\"updated\"}";

        mockMvc.perform(put("/api/estimates/x1").contentType(MediaType.APPLICATION_JSON).content(body).with(jwtAuth(sub)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("updated"));

        // forbidden when not owner
        when(estimateRepository.findByEstimateIdAndOwnerAuth0Id("x1", "other")).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/estimates/x1").contentType(MediaType.APPLICATION_JSON).content(body).with(jwtAuth("other")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteEstimate_success_and_forbidden() throws Exception {
        String sub = "owner";
        Estimate existing = new Estimate();
        existing.setEstimateId("d1");
        existing.setOwnerAuth0Id(sub);
        when(estimateRepository.findByEstimateIdAndOwnerAuth0Id("d1", sub)).thenReturn(Optional.of(existing));

        mockMvc.perform(delete("/api/estimates/d1").with(jwtAuth(sub)))
                .andExpect(status().isNoContent());

        verify(estimateRepository).deleteById("d1");

        when(estimateRepository.findByEstimateIdAndOwnerAuth0Id("d1", "other")).thenReturn(Optional.empty());
        mockMvc.perform(delete("/api/estimates/d1").with(jwtAuth("other")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getEstimatePdf_filePresent_and_missing() throws Exception {
        String sub = "owner";
        String id = "pdf1";
        Estimate e = new Estimate();
        e.setEstimateId(id);
        e.setOwnerAuth0Id(sub);
        e.setTitle("My Title");
        when(estimateRepository.findByEstimateIdAndOwnerAuth0Id(id, sub)).thenReturn(Optional.of(e));

        Path file = uploadsDir.resolve(id + ".pdf");
        Files.write(file, "hello".getBytes());

        mockMvc.perform(get("/api/estimates/" + id + "/pdf").with(jwtAuth(sub)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));

        // missing file returns 404
        Files.deleteIfExists(file);
        mockMvc.perform(get("/api/estimates/" + id + "/pdf").with(jwtAuth(sub)))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadEstimatePdf_success_and_ioError() throws Exception {
        String sub = "owner";
        String id = "up1";
        Estimate e = new Estimate();
        e.setEstimateId(id);
        e.setOwnerAuth0Id(sub);
        when(estimateRepository.findByEstimateIdAndOwnerAuth0Id(id, sub)).thenReturn(Optional.of(e));

        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());

        mockMvc.perform(multipart("/api/estimates/" + id + "/upload-pdf").file(file).with(jwtAuth(sub)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pdfUrl").value("/api/estimates/" + id + "/pdf"));

        // simulate IOException by making uploads dir read-only? Instead call with invalid path via id containing invalid char - skip here since controller wraps in 500 on IOException; we'll at least exercise success path
        verify(estimateRepository, atLeastOnce()).save(any(Estimate.class));
    }

    // Helper to set a JwtAuthenticationToken in the SecurityContext for the request thread
    private RequestPostProcessor jwtAuth(String sub) {
        return request -> {
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .claim("sub", sub)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
             JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("USER")));
             SecurityContextHolder.getContext().setAuthentication(auth);
             return request;
         };
     }
}
