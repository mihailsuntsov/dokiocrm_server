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

public class ShipmentJSON {
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
    private String shipment_date;
    private String company;
    private String date_time_created;
    private String date_time_changed;
    private String description;
    private boolean nds;
    private boolean nds_included;
    private String      status_name;
    private String      status_color;
    private String      status_description;
    private Long        product_count;
    private Long        status_id;
    private Boolean     is_completed; // завершена
    private String uid;
    private BigDecimal sum_price;
    private Long customers_orders_id;
    private Long shift_id;
    private int shift_number;
    private Long receipt_id;
    private boolean hasSellReceipt;
    private Long department_type_price_id;//тип цены для отделения в этой Отгрузке
    private Long cagent_type_price_id;//тип цены для покупателя в этой Отгрузке
    private Long default_type_price_id;//тип цены по умолчанию (устанавливается в Типах цен)
    private String shipment_time;

    public String getShipment_time() {
        return shipment_time;
    }

    public void setShipment_time(String shipment_time) {
        this.shipment_time = shipment_time;
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

    public Long getCustomers_orders_id() {
        return customers_orders_id;
    }

    public void setCustomers_orders_id(Long customers_orders_id) {
        this.customers_orders_id = customers_orders_id;
    }

    public Long getShift_id() {
        return shift_id;
    }

    public void setShift_id(Long shift_id) {
        this.shift_id = shift_id;
    }

    public int getShift_number() {
        return shift_number;
    }

    public void setShift_number(int shift_number) {
        this.shift_number = shift_number;
    }

    public Long getReceipt_id() {
        return receipt_id;
    }

    public void setReceipt_id(Long receipt_id) {
        this.receipt_id = receipt_id;
    }

    public boolean isHasSellReceipt() {
        return hasSellReceipt;
    }

    public void setHasSellReceipt(boolean hasSellReceipt) {
        this.hasSellReceipt = hasSellReceipt;
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

    public boolean isNds() {
        return nds;
    }

    public void setNds(boolean nds) {
        this.nds = nds;
    }

    public String getCagent() {
        return cagent;
    }

    public void setCagent(String cagent) {
        this.cagent = cagent;
    }

    public boolean isNds_included() {
        return nds_included;
    }

    public void setNds_included(boolean nds_included) {
        this.nds_included = nds_included;
    }

    public Long getCagent_id() {
        return cagent_id;
    }

    public void setCagent_id(Long cagent_id) {
        this.cagent_id = cagent_id;
    }

    public Long getDepartment_type_price_id() {
        return department_type_price_id;
    }

    public void setDepartment_type_price_id(Long department_type_price_id) {
        this.department_type_price_id = department_type_price_id;
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

    public String getShipment_date() {
        return shipment_date;
    }

    public void setShipment_date(String shipment_date) {
        this.shipment_date = shipment_date;
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

    public boolean isIs_completed() {
        return is_completed;
    }

    public void setIs_completed(boolean is_completed) {
        this.is_completed = is_completed;
    }
}
