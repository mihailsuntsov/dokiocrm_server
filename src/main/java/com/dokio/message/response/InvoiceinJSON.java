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

public class InvoiceinJSON {

    private Long id;
    private String master;
    private String creator;
    private String changer;
    private String cagent;
    private Long master_id;
    private Long cagent_id;
    private Long creator_id;
    private Long changer_id;
    private Long company_id;
    private Long department_id;
    private String department;
    private Long doc_number;
    private String invoicein_date;
    private String company;
    private String date_time_created;
    private String date_time_changed;
    private String description;
    private boolean nds;
    private boolean nds_included;
    private String status_name;
    private String status_color;
    private String status_description;
    private Long product_count;
    private Long status_id;
    private Boolean is_completed;               // проведено
    private String uid;
    private BigDecimal sum_price;
    private Long department_type_price_id;  //тип цены для отделения в этом документе
    private Long cagent_type_price_id;      //тип цены для покупателя в этом документе
    private Long default_type_price_id;     //тип цены по умолчанию (устанавливается в Типах цен)
    private String name;
    private String income_number;              // входящий внутренний номер поставщика
    private String income_number_date;         // входящая дата счета поставщика
    private String invoicein_time;

    public String getInvoicein_time() {
        return invoicein_time;
    }

    public void setInvoicein_time(String invoicein_time) {
        this.invoicein_time = invoicein_time;
    }

    public String getIncome_number() {
        return income_number;
    }

    public void setIncome_number(String income_number) {
        this.income_number = income_number;
    }

    public String getIncome_number_date() {
        return income_number_date;
    }

    public void setIncome_number_date(String income_number_date) {
        this.income_number_date = income_number_date;
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

    public String getCagent() {
        return cagent;
    }

    public void setCagent(String cagent) {
        this.cagent = cagent;
    }

    public Long getMaster_id() {
        return master_id;
    }

    public void setMaster_id(Long master_id) {
        this.master_id = master_id;
    }

    public Long getCagent_id() {
        return cagent_id;
    }

    public void setCagent_id(Long cagent_id) {
        this.cagent_id = cagent_id;
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

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Long getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(Long doc_number) {
        this.doc_number = doc_number;
    }

    public String getInvoicein_date() {
        return invoicein_date;
    }

    public void setInvoicein_date(String invoicein_date) {
        this.invoicein_date = invoicein_date;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isNds() {
        return nds;
    }

    public void setNds(boolean nds) {
        this.nds = nds;
    }

    public boolean isNds_included() {
        return nds_included;
    }

    public void setNds_included(boolean nds_included) {
        this.nds_included = nds_included;
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

    public Long getStatus_id() {
        return status_id;
    }

    public void setStatus_id(Long status_id) {
        this.status_id = status_id;
    }

    public Boolean getIs_completed() {
        return is_completed;
    }

    public void setIs_completed(Boolean is_completed) {
        this.is_completed = is_completed;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public BigDecimal getSum_price() {
        return sum_price;
    }

    public void setSum_price(BigDecimal sum_price) {
        this.sum_price = sum_price;
    }

    public Long getDepartment_type_price_id() {
        return department_type_price_id;
    }

    public void setDepartment_type_price_id(Long department_type_price_id) {
        this.department_type_price_id = department_type_price_id;
    }

    public Long getCagent_type_price_id() {
        return cagent_type_price_id;
    }

    public void setCagent_type_price_id(Long cagent_type_price_id) {
        this.cagent_type_price_id = cagent_type_price_id;
    }

    public Long getDefault_type_price_id() {
        return default_type_price_id;
    }

    public void setDefault_type_price_id(Long default_type_price_id) {
        this.default_type_price_id = default_type_price_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}