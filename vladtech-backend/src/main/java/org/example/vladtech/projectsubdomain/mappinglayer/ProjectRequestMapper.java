package org.example.vladtech.projectsubdomain.mappinglayer;

import org.example.vladtech.projectsubdomain.dataaccesslayer.Address;
import org.example.vladtech.projectsubdomain.dataaccesslayer.Project;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectType;
import org.example.vladtech.projectsubdomain.presentationlayer.AddressRequestModel;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectRequestModel;
import org.springframework.stereotype.Component;

@Component
public class ProjectRequestMapper {

    public Project requestModelToEntity(ProjectRequestModel requestModel) {
        if (requestModel == null) {
            return null;
        }

        Project project = new Project();
        project.setName(requestModel.getName());
        project.setClientId(requestModel.getClientId());
        project.setClientName(requestModel.getClientName());
        project.setClientEmail(requestModel.getClientEmail());
        project.setAddress(mapAddress(requestModel.getAddress()));
        project.setDescription(requestModel.getDescription());
        project.setStartDate(requestModel.getStartDate());
        project.setDueDate(requestModel.getDueDate());
        project.setProjectType(mapProjectType(requestModel.getProjectType()));
        project.setAssignedEmployeeIds(requestModel.getAssignedEmployeeIds());
        project.setAssignedEmployeeEmails(requestModel.getAssignedEmployeeEmails());
        return project;
    }

    private ProjectType mapProjectType(String type) {
        if (type == null) {
            return null;
        }
        ProjectType projectType = new ProjectType();
        projectType.setType(ProjectType.ProjectTypeEnum.valueOf(type.toUpperCase()));
        return projectType;
    }

    private Address mapAddress(AddressRequestModel addressRequestModel) {
        if (addressRequestModel == null) {
            return null;
        }
        return new Address(
                addressRequestModel.getStreetAddress(),
                addressRequestModel.getCity(),
                addressRequestModel.getProvince(),
                addressRequestModel.getCountry(),
                addressRequestModel.getPostalCode()
        );
    }
}