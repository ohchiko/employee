package com.ohchiko.restful.employee;

class EmployeeNotFoundException extends RuntimeException {

    EmployeeNotFoundException(Long id) {
        super("Could not find employee with id " + id);
    }
}
