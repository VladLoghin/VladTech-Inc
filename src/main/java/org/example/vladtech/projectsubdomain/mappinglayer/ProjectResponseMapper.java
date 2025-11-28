package org.example.vladtech.projectsubdomain.mappinglayer;

import org.example.vladtech.projectsubdomain.dataaccesslayer.Address;
import org.example.vladtech.projectsubdomain.dataaccesslayer.Project;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectPhoto;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectType;
import org.example.vladtech.projectsubdomain.presentationlayer.AddressResponseModel;
import org.example.vladtech.projectsubdomain.presentationlayer.PhotoResponseModel;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectResponseModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectResponseMapper {

    public ProjectResponseModel entityToResponseModel(Project project) {
        if (project == null) {
            return null;
        }

        ProjectResponseModel responseModel = new ProjectResponseModel();
        responseModel.setProjectIdentifier(project.getProjectIdentifier());
        responseModel.setName(project.getName());
        responseModel.setAddress(mapAddress(project.getAddress()));
        responseModel.setDescription(project.getDescription());
        responseModel.setStartDate(project.getStartDate());
        responseModel.setDueDate(project.getDueDate());
        responseModel.setProjectType(mapProjectType(project.getProjectType()));
        responseModel.setAssignedEmployeeIds(project.getAssignedEmployeeIds());
        responseModel.setPhotos(mapPhotos(project.getPhotos()));

        return responseModel;
    }

    public List<ProjectResponseModel> entityListToResponseModelList(List<Project> projects) {
        if (projects == null) {
            return null;
        }
        return projects.stream()
                .map(this::entityToResponseModel)
                .collect(Collectors.toList());
    }

    private String mapProjectType(ProjectType projectType) {
        if (projectType == null || projectType.getType() == null) {
            return null;
        }
        return projectType.getType().name();
    }

    private AddressResponseModel mapAddress(Address address) {
        if (address == null) {
            return null;
        }
        return new AddressResponseModel(
                address.getStreetAddress(),
                address.getCity(),
                address.getProvince(),
                address.getCountry(),
                address.getPostalCode()
        );
    }

    private List<PhotoResponseModel> mapPhotos(List<ProjectPhoto> photos) {
        if (photos == null) {
            return null;
        }
        return photos.stream()
                .map(this::mapPhoto)
                .collect(Collectors.toList());
    }

    private PhotoResponseModel mapPhoto(ProjectPhoto photo) {
        if (photo == null) {
            return null;
        }
        return new PhotoResponseModel(
                photo.getPhotoId(),
                photo.getPhotoUrl(),
                photo.getDescription()
        );
    }
}