/*
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU Affero GPL редакции 3 (GNU AGPLv3),
опубликованной Фондом свободного программного обеспечения;
Эта программа распространяется в расчёте на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу: http://www.gnu.org/licenses
*/
package com.dokio.message.request;

import java.math.BigDecimal;

public class VatinvoiceoutForm {

    private Long id;
    private Long company_id;
    private String description;
    private Long cagent_id;
    private Long cagent2_id; //грузополучатель
    private String new_cagent;
    private String new_cagent2;
    private String parent_tablename;
    private Long orderin_id;
    private Long paymentin_id;
    private Long shipment_id;
    private String gov_id;
    private Long status_id;
    private String doc_number;
    private String uid;
    private Long linked_doc_id;//id связанного документа
    private String linked_doc_name;//имя (таблицы) связанного документа
    private String parent_uid;// uid исходящего (родительского) документа
    private String child_uid; // uid дочернего документа. Дочерний - не всегда тот, которого создают из текущего документа. Например, при создании из Отгрузки Счёта покупателю - Отгрузка будет дочерней для него.
    private Boolean is_completed;// проведён
    private String paydoc_number;
    private String paydoc_date;

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

    public Long getCagent_id() {
        return cagent_id;
    }

    public void setCagent_id(Long cagent_id) {
        this.cagent_id = cagent_id;
    }

    public String getNew_cagent() {
        return new_cagent;
    }

    public void setNew_cagent(String new_cagent) {
        this.new_cagent = new_cagent;
    }

    public Long getStatus_id() {
        return status_id;
    }

    public void setStatus_id(Long status_id) {
        this.status_id = status_id;
    }

    public String getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(String doc_number) {
        this.doc_number = doc_number;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getParent_uid() {
        return parent_uid;
    }

    public void setParent_uid(String parent_uid) {
        this.parent_uid = parent_uid;
    }

    public String getChild_uid() {
        return child_uid;
    }

    public void setChild_uid(String child_uid) {
        this.child_uid = child_uid;
    }

    public Boolean getIs_completed() {
        return is_completed;
    }

    public void setIs_completed(Boolean is_completed) {
        this.is_completed = is_completed;
    }

    public Long getCagent2_id() {
        return cagent2_id;
    }

    public void setCagent2_id(Long cagent2_id) {
        this.cagent2_id = cagent2_id;
    }

    public String getNew_cagent2() {
        return new_cagent2;
    }

    public void setNew_cagent2(String new_cagent2) {
        this.new_cagent2 = new_cagent2;
    }

    public String getParent_tablename() {
        return parent_tablename;
    }

    public void setParent_tablename(String parent_tablename) {
        this.parent_tablename = parent_tablename;
    }

    public Long getOrderin_id() {
        return orderin_id;
    }

    public void setOrderin_id(Long orderin_id) {
        this.orderin_id = orderin_id;
    }

    public Long getPaymentin_id() {
        return paymentin_id;
    }

    public void setPaymentin_id(Long paymentin_id) {
        this.paymentin_id = paymentin_id;
    }

    public Long getShipment_id() {
        return shipment_id;
    }

    public void setShipment_id(Long shipment_id) {
        this.shipment_id = shipment_id;
    }

    public String getGov_id() {
        return gov_id;
    }

    public void setGov_id(String gov_id) {
        this.gov_id = gov_id;
    }

    public String getPaydoc_number() {
        return paydoc_number;
    }

    public void setPaydoc_number(String paydoc_number) {
        this.paydoc_number = paydoc_number;
    }

    public String getPaydoc_date() {
        return paydoc_date;
    }

    public void setPaydoc_date(String paydoc_date) {
        this.paydoc_date = paydoc_date;
    }
}