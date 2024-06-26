package com.dokio.message.response.additional;

import java.util.List;

public class DepartmentWithPartsJSON {

    private Long   department_id;
    private String department_name;
    private List<DepartmentPartJSON> parts;

    public DepartmentWithPartsJSON(Long department_id, String department_name, List<DepartmentPartJSON> parts) {
        this.department_id = department_id;
        this.department_name = department_name;
        this.parts = parts;
    }
//    private Long part_id;
//    private Long department_id;
//    private String part_name;
//    private String part_description;
//    private Boolean is_active;
//    private String department_name;


    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }

    public String getDepartment_name() {
        return department_name;
    }

    public void setDepartment_name(String department_name) {
        this.department_name = department_name;
    }

    public List<DepartmentPartJSON> getParts() {
        return parts;
    }

    public void setParts(List<DepartmentPartJSON> parts) {
        this.parts = parts;
    }
}
