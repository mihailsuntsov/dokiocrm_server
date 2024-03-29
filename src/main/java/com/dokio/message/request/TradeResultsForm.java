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

public class TradeResultsForm {
    private Long id;
    private String company;
    private String company_id;
    private String department;
    private String department_id;
    private String master;
    private String master_id;
    private String creator;
    private String creator_id;
    private String changer;
    private String changer_id;
    private String employee;
    private String employee_id;
    private String date_time_created;
    private String date_time_changed;
    private String trade_date;
    private String additional;
    private String incoming_cash_checkout;
    private String incoming_cashless_checkout;
    private String incoming_cash2;
    private String incoming_cashless2;
    private String refund_cash;
    private String refund_cashless;
    private String encashment_cash;
    private String encashment_cashless;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(String department_id) {
        this.department_id = department_id;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getMaster_id() {
        return master_id;
    }

    public void setMaster_id(String master_id) {
        this.master_id = master_id;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    public String getChanger() {
        return changer;
    }

    public void setChanger(String changer) {
        this.changer = changer;
    }

    public String getChanger_id() {
        return changer_id;
    }

    public void setChanger_id(String changer_id) {
        this.changer_id = changer_id;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(String employee_id) {
        this.employee_id = employee_id;
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

    public String getTrade_date() {
        return trade_date;
    }

    public void setTrade_date(String trade_date) {
        this.trade_date = trade_date;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public String getIncoming_cash_checkout() {
        return incoming_cash_checkout;
    }

    public void setIncoming_cash_checkout(String incoming_cash_checkout) {
        this.incoming_cash_checkout = incoming_cash_checkout;
    }

    public String getIncoming_cashless_checkout() {
        return incoming_cashless_checkout;
    }

    public void setIncoming_cashless_checkout(String incoming_cashless_checkout) {
        this.incoming_cashless_checkout = incoming_cashless_checkout;
    }

    public String getIncoming_cash2() {
        return incoming_cash2;
    }

    public void setIncoming_cash2(String incoming_cash2) {
        this.incoming_cash2 = incoming_cash2;
    }

    public String getIncoming_cashless2() {
        return incoming_cashless2;
    }

    public void setIncoming_cashless2(String incoming_cashless2) {
        this.incoming_cashless2 = incoming_cashless2;
    }

    public String getRefund_cash() {
        return refund_cash;
    }

    public void setRefund_cash(String refund_cash) {
        this.refund_cash = refund_cash;
    }

    public String getRefund_cashless() {
        return refund_cashless;
    }

    public void setRefund_cashless(String refund_cashless) {
        this.refund_cashless = refund_cashless;
    }

    public String getEncashment_cash() {
        return encashment_cash;
    }

    public void setEncashment_cash(String encashment_cash) {
        this.encashment_cash = encashment_cash;
    }

    public String getEncashment_cashless() {
        return encashment_cashless;
    }

    public void setEncashment_cashless(String encashment_cashless) {
        this.encashment_cashless = encashment_cashless;
    }

    @Override
    public String toString() {
        return "TradeResultsForm: id=" + this.id + ", company" + this.company + ", company_id" + this.company_id +
                ", department" + this.department + ", department_id" + this.department_id;
    }
}
