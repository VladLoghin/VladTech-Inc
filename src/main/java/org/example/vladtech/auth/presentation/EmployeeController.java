package auth.presentation;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    @PreAuthorize("hasAuthority('Employee')")
    @RequestMapping("/info")
    public String employeeInfo() {
        return "Hello, Employee User!";
    }
}
