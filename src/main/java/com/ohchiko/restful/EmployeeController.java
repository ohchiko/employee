package com.ohchiko.restful;

import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;

import java.util.stream.Collectors;
import java.net.URI;
import java.net.URISyntaxException;

@RestController
class EmployeeController {

    private final EmployeeRepository repository;
    private final EmployeeResourceAssembler assembler;

    EmployeeController(EmployeeRepository repository,
                       EmployeeResourceAssembler assembler) {

        this.repository = repository;
        this.assembler = assembler;
    }

    @GetMapping("/employees")
    Resources<Resource<Employee>> all() {

        List<Resource<Employee>> employees = repository.findAll().stream()
            .map(assembler::toResource)
            .collect(Collectors.toList());

        return new Resources<>(employees,
                               linkTo(methodOn(EmployeeController.class).all()).withSelfRel());
    }

    @PostMapping("/employees")
    Employee newEmployee(@RequestBody Employee newEmployee) {

        return repository.save(newEmployee);
    }

    @GetMapping("/employees/{id}")
    Resource<Employee> one(@PathVariable Long id) {

        Employee employee = repository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));

        return assembler.toResource(employee);
    }

    @PutMapping("/employees/{id}")
    ResponseEntity<?> replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) throws URISyntaxException {

        Employee updatedEmployee = repository.findById(id)
            .map(employee -> {
                    employee.setName(newEmployee.getName());
                    employee.setRole(newEmployee.getRole());
                    return repository.save(employee);
                })
            .orElseGet(() -> {
                    newEmployee.setId(id);
                    return repository.save(newEmployee);
                });

        Resource<Employee> resource = assembler.toResource(updatedEmployee);

        return ResponseEntity
            .created(new URI(resource.getId().expand().getHref()))
            .body(resource);
    }

    @DeleteMapping("/employees/{id}")
    void deleteEmployee(@PathVariable Long id) {

        repository.deleteById(id);
    }
}
