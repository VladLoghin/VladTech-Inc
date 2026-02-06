package org.example.vladtech.projectsubdomain.businesslayer;

import lombok.RequiredArgsConstructor;
import org.example.vladtech.projectsubdomain.dataaccesslayer.UserProjectPin;
import org.example.vladtech.projectsubdomain.dataaccesslayer.UserProjectPinRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProjectPinService {

    private final UserProjectPinRepository userProjectPinRepository;

    public void pinProject(String userId, String projectId) {
        if (!userProjectPinRepository.existsByUserIdAndProjectId(userId, projectId)) {
            UserProjectPin pin = new UserProjectPin();
            pin.setUserId(userId);
            pin.setProjectId(projectId);
            pin.setPinnedAt(LocalDateTime.now());
            userProjectPinRepository.save(pin);
        }
    }

    public void unpinProject(String userId, String projectId) {
        userProjectPinRepository.deleteByUserIdAndProjectId(userId, projectId);
    }

    public boolean isProjectPinned(String userId, String projectId) {
        return userProjectPinRepository.existsByUserIdAndProjectId(userId, projectId);
    }

    public List<UserProjectPin> getPinnedProjects(String userId) {
        return userProjectPinRepository.findByUserId(userId);
    }
}
