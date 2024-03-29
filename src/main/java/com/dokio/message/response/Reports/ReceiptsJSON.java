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

package com.dokio.message.response.Reports;

import java.math.BigDecimal;

public class ReceiptsJSON {

    private Long id;
    private Long master_id;
    private Long creator_id;
    private Long company_id;
    private Long department_id;
    private Long kassa_id;  // id KKM
    private Long shift_id;  // id смены
    private Integer document_id; //id документа в таблице documents
    private Long return_id; // id Возврата покупателя
    private Long retail_sales_id; // id Розничной продажи
    private Long shipment_id; // id отгрузки
    private String date_time_created;
    private String operation_id; // id операции: sell - продажа, return - возврат
    private Integer sno_id; // id системы налогообложения
    private String sno;
    private String billing_address;
    private String payment_type;
    private BigDecimal cash;
    private BigDecimal electronically;
    private Long acquiring_bank_id; // банк эквайер
    private String master;
    private String creator;
    private String company;
    private String department;
    private String kassa;
    private String acquiring_bank;
    private String document; // Наименование документа из таблицы documents
    private String parent_tablename;
    private Integer shift_number;
    private BigDecimal summ; //всего сумма в чеке (нал + электронные)
    private String uid;
    private Long parent_doc_id;

    public Long getParent_doc_id() {
        return parent_doc_id;
    }

    public void setParent_doc_id(Long parent_doc_id) {
        this.parent_doc_id = parent_doc_id;
    }

    public BigDecimal getSumm() {
        return summ;
    }

    public void setSumm(BigDecimal summ) {
        this.summ = summ;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getKassa_id() {
        return kassa_id;
    }

    public void setKassa_id(Long kassa_id) {
        this.kassa_id = kassa_id;
    }

    public Long getShift_id() {
        return shift_id;
    }

    public void setShift_id(Long shift_id) {
        this.shift_id = shift_id;
    }

    public Integer getDocument_id() {
        return document_id;
    }

    public void setDocument_id(Integer document_id) {
        this.document_id = document_id;
    }

    public Long getReturn_id() {
        return return_id;
    }

    public void setReturn_id(Long return_id) {
        this.return_id = return_id;
    }

    public Long getRetail_sales_id() {
        return retail_sales_id;
    }

    public void setRetail_sales_id(Long retail_sales_id) {
        this.retail_sales_id = retail_sales_id;
    }

    public Long getShipment_id() {
        return shipment_id;
    }

    public void setShipment_id(Long shipment_id) {
        this.shipment_id = shipment_id;
    }

    public String getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(String date_time_created) {
        this.date_time_created = date_time_created;
    }

    public String getOperation_id() {
        return operation_id;
    }

    public void setOperation_id(String operation_id) {
        this.operation_id = operation_id;
    }

    public Integer getSno_id() {
        return sno_id;
    }

    public void setSno_id(Integer sno_id) {
        this.sno_id = sno_id;
    }

    public String getBilling_address() {
        return billing_address;
    }

    public void setBilling_address(String billing_address) {
        this.billing_address = billing_address;
    }

    public String getPayment_type() {
        return payment_type;
    }

    public void setPayment_type(String payment_type) {
        this.payment_type = payment_type;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public void setCash(BigDecimal cash) {
        this.cash = cash;
    }

    public BigDecimal getElectronically() {
        return electronically;
    }

    public void setElectronically(BigDecimal electronically) {
        this.electronically = electronically;
    }

    public Long getAcquiring_bank_id() {
        return acquiring_bank_id;
    }

    public void setAcquiring_bank_id(Long acquiring_bank_id) {
        this.acquiring_bank_id = acquiring_bank_id;
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

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getKassa() {
        return kassa;
    }

    public void setKassa(String kassa) {
        this.kassa = kassa;
    }

    public String getAcquiring_bank() {
        return acquiring_bank;
    }

    public void setAcquiring_bank(String acquiring_bank) {
        this.acquiring_bank = acquiring_bank;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getParent_tablename() {
        return parent_tablename;
    }

    public void setParent_tablename(String parent_tablename) {
        this.parent_tablename = parent_tablename;
    }

    public Integer getShift_number() {
        return shift_number;
    }

    public void setShift_number(Integer shift_number) {
        this.shift_number = shift_number;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }
}