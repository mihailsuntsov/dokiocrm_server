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

package com.dokio.message.request;

import java.util.Set;

public class ShipmentForm {
    private Long id;
    private Long company_id;
    private String description;
    private Long department_id;
    private Long cagent_id;
    private String new_cagent;
    private Long status_id;
    private Long customers_orders_id;
    private Long shift_id;
    private String doc_number;
    private String shipment_date;
    private Set<ShipmentProductTableForm> shipmentProductTable;
    private boolean nds;
    private boolean nds_included;
    private String uid;
    private Long linked_doc_id;//id связанного документа
    private String linked_doc_name;//имя (таблицы) связанного документа
    private String  parent_uid;// uid исходящего (родительского) документа
    private String  child_uid; // uid дочернего документа. Дочерний - не всегда тот, которого создают из текущего документа. Например, при создании из Отгрузки Счёта покупателю - Отгрузка будет дочерней для него.
    private Boolean is_completed;// проведён
    private String shipment_time;

    public String getShipment_time() {
        return shipment_time;
    }

    public void setShipment_time(String shipment_time) {
        this.shipment_time = shipment_time;
    }


    public Boolean isIs_completed() {
        return is_completed;
    }

    public void setIs_completed(Boolean is_completed) {
        this.is_completed = is_completed;
    }

    public String getParent_uid() {
        return parent_uid;
    }

    public void setParent_uid(String parent_uid) {
        this.parent_uid = parent_uid;
    }

    public Long getStatus_id() {
        return status_id;
    }

    public void setStatus_id(Long status_id) {
        this.status_id = status_id;
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

    public String getChild_uid() {
        return child_uid;
    }

    public void setChild_uid(String child_uid) {
        this.child_uid = child_uid;
    }

    public String getNew_cagent() {
        return new_cagent;
    }

    public void setNew_cagent(String new_cagent) {
        this.new_cagent = new_cagent;
    }

    public Long getLinked_doc_id() {
        return linked_doc_id;
    }

    public void setLinked_doc_id(Long linked_doc_id) {
        this.linked_doc_id = linked_doc_id;
    }

    public String getLinked_doc_name() {
        return linked_doc_name;
    }

    public void setLinked_doc_name(String linked_doc_name) {
        this.linked_doc_name = linked_doc_name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }

    public Long getCagent_id() {
        return cagent_id;
    }

    public void setCagent_id(Long cagent_id) {
        this.cagent_id = cagent_id;
    }

    public String getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(String doc_number) {
        this.doc_number = doc_number;
    }

    public String getShipment_date() {
        return shipment_date;
    }

    public void setShipment_date(String shipment_date) {
        this.shipment_date = shipment_date;
    }

    public Set<ShipmentProductTableForm> getShipmentProductTable() {
        return shipmentProductTable;
    }

    public void setShipmentProductTable(Set<ShipmentProductTableForm> shipmentProductTable) {
        this.shipmentProductTable = shipmentProductTable;
    }

    @Override
    public String toString() {
        return "ShipmentForm{" +
                "id=" + id +
                ", company_id=" + company_id +
                ", description='" + description + '\'' +
                ", department_id=" + department_id +
                ", cagent_id=" + cagent_id +
                ", new_cagent='" + new_cagent + '\'' +
                ", status_id=" + status_id +
                ", customers_orders_id=" + customers_orders_id +
                ", shift_id=" + shift_id +
                ", doc_number='" + doc_number + '\'' +
                ", shipment_date='" + shipment_date + '\'' +
                ", shipmentProductTable=" + shipmentProductTable +
                ", nds=" + nds +
                ", nds_included=" + nds_included +
                ", uid='" + uid + '\'' +
                ", linked_doc_id=" + linked_doc_id +
                ", linked_doc_name='" + linked_doc_name + '\'' +
                ", parent_uid='" + parent_uid + '\'' +
                ", child_uid='" + child_uid + '\'' +
                ", is_completed=" + is_completed +
                ", shipment_time='" + shipment_time + '\'' +
                '}';
    }
}
