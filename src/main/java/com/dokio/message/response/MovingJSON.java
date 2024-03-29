/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package com.dokio.message.response;

import java.math.BigDecimal;

public class MovingJSON {

    private Long            id;                         // id документа
    private String          master;                     // ФИО из мастер-аккаунта
    private String          creator;                    // ФИО создателя
    private String          changer;                    // ФИО кто последний изменил
    private Long            master_id;                  // id мастер-аккаунта
    private Long            creator_id;                 // id создателя
    private Long            changer_id;                 // id кто последний изменил
    private Long            company_id;                 // id предприятия
    private Long            department_from_id;         // id отделения из
    private Long            department_to_id;           // id отделения в
    private String          company;                    // наименование предприятия
    private String          department_from;            // отделение из
    private String          department_to;              // отделение в
    private Long            doc_number;                 // номер документа
    private String          date_time_created;          // дата создания
    private String          date_time_changed;          // дата последнего изменения
    private BigDecimal      overhead;                   // расходы на перемещение
    private Integer         overhead_netcost_method;    // распределение расходов на перемещение 0 - нет, 1 - по весу цены в поставке
    private String          description;                // описание перемещения
    private Boolean         is_completed;               // проведён
    private Long            status_id;                  // id статуса
    private String          status_name;                // наименование статуса
    private String          status_color;               // цвет статуса
    private String          status_description;         // описание статуса
    private Long            product_count;              // кол-во товарных позиций в перемещении
    private String          uid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getChanger() {
        return changer;
    }

    public void setChanger(String changer) {
        this.changer = changer;
    }

    public Long getMaster_id() {
        return master_id;
    }

    public void setMaster_id(Long master_id) {
        this.master_id = master_id;
    }

    public Long getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(Long creator_id) {
        this.creator_id = creator_id;
    }

    public Long getChanger_id() {
        return changer_id;
    }

    public void setChanger_id(Long changer_id) {
        this.changer_id = changer_id;
    }

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public Long getDepartment_from_id() {
        return department_from_id;
    }

    public void setDepartment_from_id(Long department_from_id) {
        this.department_from_id = department_from_id;
    }

    public Long getDepartment_to_id() {
        return department_to_id;
    }

    public void setDepartment_to_id(Long department_to_id) {
        this.department_to_id = department_to_id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDepartment_from() {
        return department_from;
    }

    public void setDepartment_from(String department_from) {
        this.department_from = department_from;
    }

    public String getDepartment_to() {
        return department_to;
    }

    public void setDepartment_to(String department_to) {
        this.department_to = department_to;
    }

    public Long getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(Long doc_number) {
        this.doc_number = doc_number;
    }

    public String getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(String date_time_created) {
        this.date_time_created = date_time_created;
    }

    public String getDate_time_changed() {
        return date_time_changed;
    }

    public void setDate_time_changed(String date_time_changed) {
        this.date_time_changed = date_time_changed;
    }

    public BigDecimal getOverhead() {
        return overhead;
    }

    public void setOverhead(BigDecimal overhead) {
        this.overhead = overhead;
    }

    public Integer getOverhead_netcost_method() {
        return overhead_netcost_method;
    }

    public void setOverhead_netcost_method(Integer overhead_netcost_method) {
        this.overhead_netcost_method = overhead_netcost_method;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIs_completed() {
        return is_completed;
    }

    public void setIs_completed(Boolean is_completed) {
        this.is_completed = is_completed;
    }

    public Long getStatus_id() {
        return status_id;
    }

    public void setStatus_id(Long status_id) {
        this.status_id = status_id;
    }

    public String getStatus_name() {
        return status_name;
    }

    public void setStatus_name(String status_name) {
        this.status_name = status_name;
    }

    public String getStatus_color() {
        return status_color;
    }

    public void setStatus_color(String status_color) {
        this.status_color = status_color;
    }

    public String getStatus_description() {
        return status_description;
    }

    public void setStatus_description(String status_description) {
        this.status_description = status_description;
    }

    public Long getProduct_count() {
        return product_count;
    }

    public void setProduct_count(Long product_count) {
        this.product_count = product_count;
    }
}
