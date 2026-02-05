package org.example.vladtech.projectsubdomain.businesslayer;

import org.example.vladtech.projectsubdomain.dataaccesslayer.UserProjectPin;
import org.example.vladtech.projectsubdomain.dataaccesslayer.UserProjectPinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProjectPinServiceTest {

    @Mock
    private UserProjectPinRepository userProjectPinRepository;

    @InjectMocks
    private UserProjectPinService userProjectPinService;

    private String userId;
    private String projectId;
    private UserProjectPin userProjectPin;

    @BeforeEach
    void setUp() {
        userId = "auth0|user123";
        projectId = "project-mongo-id-123";

        userProjectPin = new UserProjectPin();
        userProjectPin.setId("pin-id-123");
        userProjectPin.setUserId(userId);
        userProjectPin.setProjectId(projectId);
        userProjectPin.setPinnedAt(LocalDateTime.now());
    }

    @Test
    void pinProject_WhenProjectNotAlreadyPinned_ShouldCreateNewPin() {
        // Arrange
        when(userProjectPinRepository.existsByUserIdAndProjectId(userId, projectId))
                .thenReturn(false);
        when(userProjectPinRepository.save(any(UserProjectPin.class)))
                .thenReturn(userProjectPin);

        // Act
        userProjectPinService.pinProject(userId, projectId);

        // Assert
        verify(userProjectPinRepository, times(1))
                .existsByUserIdAndProjectId(userId, projectId);
        verify(userProjectPinRepository, times(1))
                .save(any(UserProjectPin.class));
    }

    @Test
    void pinProject_WhenProjectAlreadyPinned_ShouldNotCreateDuplicate() {
        // Arrange
        when(userProjectPinRepository.existsByUserIdAndProjectId(userId, projectId))
                .thenReturn(true);

        // Act
        userProjectPinService.pinProject(userId, projectId);

        // Assert
        verify(userProjectPinRepository, times(1))
                .existsByUserIdAndProjectId(userId, projectId);
        verify(userProjectPinRepository, never())
                .save(any(UserProjectPin.class));
    }

    @Test
    void unpinProject_ShouldCallRepositoryDelete() {
        // Act
        userProjectPinService.unpinProject(userId, projectId);

        // Assert
        verify(userProjectPinRepository, times(1))
                .deleteByUserIdAndProjectId(userId, projectId);
    }

    @Test
    void isProjectPinned_WhenPinExists_ShouldReturnTrue() {
        // Arrange
        when(userProjectPinRepository.existsByUserIdAndProjectId(userId, projectId))
                .thenReturn(true);

        // Act
        boolean result = userProjectPinService.isProjectPinned(userId, projectId);

        // Assert
        assertThat(result).isTrue();
        verify(userProjectPinRepository, times(1))
                .existsByUserIdAndProjectId(userId, projectId);
    }

    @Test
    void isProjectPinned_WhenPinDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(userProjectPinRepository.existsByUserIdAndProjectId(userId, projectId))
                .thenReturn(false);

        // Act
        boolean result = userProjectPinService.isProjectPinned(userId, projectId);

        // Assert
        assertThat(result).isFalse();
        verify(userProjectPinRepository, times(1))
                .existsByUserIdAndProjectId(userId, projectId);
    }

    @Test
    void getPinnedProjects_ShouldReturnListOfPins() {
        // Arrange
        UserProjectPin pin1 = new UserProjectPin();
        pin1.setUserId(userId);
        pin1.setProjectId("project-1");
        pin1.setPinnedAt(LocalDateTime.now());

        UserProjectPin pin2 = new UserProjectPin();
        pin2.setUserId(userId);
        pin2.setProjectId("project-2");
        pin2.setPinnedAt(LocalDateTime.now());

        List<UserProjectPin> expectedPins = Arrays.asList(pin1, pin2);

        when(userProjectPinRepository.findByUserId(userId))
                .thenReturn(expectedPins);

        // Act
        List<UserProjectPin> result = userProjectPinService.getPinnedProjects(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedPins);
        verify(userProjectPinRepository, times(1))
                .findByUserId(userId);
    }

    @Test
    void getPinnedProjects_WhenNoPinsExist_ShouldReturnEmptyList() {
        // Arrange
        when(userProjectPinRepository.findByUserId(userId))
                .thenReturn(List.of());

        // Act
        List<UserProjectPin> result = userProjectPinService.getPinnedProjects(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(userProjectPinRepository, times(1))
                .findByUserId(userId);
    }
}
