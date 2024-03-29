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

public class KassaJSON {
    private Long id;
    private String master;
    private String creator;
    private String changer;
    private String cagent;
    private Long   master_id;
    private Long   cagent_id;
    private Long   creator_id;
    private Long   changer_id;
    private Long   company_id;
    private Long   department_id;
    private String department;
    private String company;
    private String date_time_created;
    private String date_time_changed;
    private String name;
    private String server_type;
    private int    sno1_id;
    private String sno1_name_api_atol;
    private String device_server_uid;
    private String additional;
    private String server_address;
    private Boolean allow_to_use;
    private Boolean is_deleted;
    private String company_name;
    private String company_email;
    private String company_vatin;
    private String billing_address;
    private String zn_kkt;

    private Boolean is_virtual; //виртуальная касса
    private Boolean allow_acquiring; //прием безнала на данной кассе
    private Long acquiring_bank_id; // id банк-эквайер
    private BigDecimal acquiring_precent; // процент банку за услугу эквайринга
    private String acquiring_bank; // банк-эквайер
    private Long acquiring_service_id; // id услуги банка-эквайера
    private String acquiring_service; // услуга банка-эквайера
    private Long payment_account_id; // id расчетного счета
    private String payment_account; // расчетный счет
    private Long expenditure_id; // id статьи расходов
    private String expenditure; // статья расходов

    public Long getPayment_account_id() {
        return payment_account_id;
    }

    public void setPayment_account_id(Long payment_account_id) {
        this.payment_account_id = payment_account_id;
    }

    public String getPayment_account() {
        return payment_account;
    }

    public void setPayment_account(String payment_account) {
        this.payment_account = payment_account;
    }

    public Long getExpenditure_id() {
        return expenditure_id;
    }

    public void setExpenditure_id(Long expenditure_id) {
        this.expenditure_id = expenditure_id;
    }

    public String getExpenditure() {
        return expenditure;
    }

    public void setExpenditure(String expenditure) {
        this.expenditure = expenditure;
    }

    public Long getAcquiring_service_id() {
        return acquiring_service_id;
    }

    public void setAcquiring_service_id(Long acquiring_service_id) {
        this.acquiring_service_id = acquiring_service_id;
    }

    public String getAcquiring_service() {
        return acquiring_service;
    }

    public void setAcquiring_service(String acquiring_service) {
        this.acquiring_service = acquiring_service;
    }

    public String getAcquiring_bank() {
        return acquiring_bank;
    }

    public void setAcquiring_bank(String acquiring_bank) {
        this.acquiring_bank = acquiring_bank;
    }

    public Boolean getIs_virtual() {
        return is_virtual;
    }

    public void setIs_virtual(Boolean is_virtual) {
        this.is_virtual = is_virtual;
    }

    public Boolean getAllow_acquiring() {
        return allow_acquiring;
    }

    public void setAllow_acquiring(Boolean allow_acquiring) {
        this.allow_acquiring = allow_acquiring;
    }

    public Long getAcquiring_bank_id() {
        return acquiring_bank_id;
    }

    public void setAcquiring_bank_id(Long acquiring_bank_id) {
        this.acquiring_bank_id = acquiring_bank_id;
    }

    public BigDecimal getAcquiring_precent() {
        return acquiring_precent;
    }

    public void setAcquiring_precent(BigDecimal acquiring_precent) {
        this.acquiring_precent = acquiring_precent;
    }

    public String getZn_kkt() {
        return zn_kkt;
    }

    public void setZn_kkt(String zn_kkt) {
        this.zn_kkt = zn_kkt;
    }


    public String getCompany_vatin() {
        return company_vatin;
    }

    public void setCompany_vatin(String company_vatin) {
        this.company_vatin = company_vatin;
    }

    public String getCompany_email() {
        return company_email;
    }

    public void setCompany_email(String company_email) {
        this.company_email = company_email;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public Long getId() {
        return id;
    }

    public String getSno1_name_api_atol() {
        return sno1_name_api_atol;
    }

    public void setSno1_name_api_atol(String sno1_name_api_atol) {
        this.sno1_name_api_atol = sno1_name_api_atol;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServer_type() {
        return server_type;
    }

    public void setServer_type(String server_type) {
        this.server_type = server_type;
    }

    public int getSno1_id() {
        return sno1_id;
    }

    public void setSno1_id(int sno1_id) {
        this.sno1_id = sno1_id;
    }

    public String getDevice_server_uid() {
        return device_server_uid;
    }

    public void setDevice_server_uid(String device_server_uid) {
        this.device_server_uid = device_server_uid;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public String getServer_address() {
        return server_address;
    }

    public void setServer_address(String server_address) {
        this.server_address = server_address;
    }

    public Boolean getAllow_to_use() {
        return allow_to_use;
    }

    public void setAllow_to_use(Boolean allow_to_use) {
        this.allow_to_use = allow_to_use;
    }

    public Boolean getIs_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(Boolean is_deleted) {
        this.is_deleted = is_deleted;
    }

    public String getBilling_address() {
        return billing_address;
    }

    public void setBilling_address(String billing_address) {
        this.billing_address = billing_address;
    }
}
