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

package com.dokio.message.response.Settings;

public class SettingsOrdersupJSON {

    private Long        id;
    private Long        companyId;                      // id предприятия
    private Long        departmentId;                   // id отделения
    private Long        cagentId;                       // id поставщика
    private String      cagent;                         // наименование поставщика
    private Boolean     autocreate;                     // автосоздание нового документа
    private Long        statusIdOnComplete;             // статус при успешном проведении
    private Boolean     autoAdd;                        // автодобавление
    private Boolean     auto_price;                     // автоматическая подстановка цены
    private String      name;                           // наименование заказа по умолчанию

    public SettingsOrdersupJSON(Boolean autocreate, Boolean autoAdd, Boolean auto_price, String name) {
        this.autocreate = autocreate;
        this.autoAdd = autoAdd;
        this.auto_price = auto_price;
        this.name = name;
    }

    public SettingsOrdersupJSON() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getCagentId() {
        return cagentId;
    }

    public void setCagentId(Long cagentId) {
        this.cagentId = cagentId;
    }

    public String getCagent() {
        return cagent;
    }

    public void setCagent(String cagent) {
        this.cagent = cagent;
    }

    public Boolean getAutocreate() {
        return autocreate;
    }

    public void setAutocreate(Boolean autocreate) {
        this.autocreate = autocreate;
    }

    public Long getStatusIdOnComplete() {
        return statusIdOnComplete;
    }

    public void setStatusIdOnComplete(Long statusIdOnComplete) {
        this.statusIdOnComplete = statusIdOnComplete;
    }

    public Boolean getAutoAdd() {
        return autoAdd;
    }

    public void setAutoAdd(Boolean autoAdd) {
        this.autoAdd = autoAdd;
    }

    public Boolean getAuto_price() {
        return auto_price;
    }

    public void setAuto_price(Boolean auto_price) {
        this.auto_price = auto_price;
    }
}
