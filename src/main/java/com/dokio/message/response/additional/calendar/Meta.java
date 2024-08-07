package com.dokio.message.response.additional.calendar;

import java.math.BigDecimal;
import java.util.Set;

public class Meta {

    private CalendarUser user;
    private String docType;
    private Long departmentPartId;
    private Set<ItemResource> itemResources;
    private String statusName;
    private Integer statusType; //тип статуса : 1 - обычный; 2 - конечный положительный 3 - конечный отрицательный
    private Long statusId;
    private String statusColor;
    private Boolean isCompleted;
    private BigDecimal sumAll;
    private BigDecimal sumShipped;
    private BigDecimal sumPayed;


    public Meta() {
    }

    public Meta(CalendarUser user, String docType, Long departmentPartId, String statusName, Integer statusType, Long statusId, String statusColor, Boolean isCompleted, BigDecimal sumAll, BigDecimal sumShipped, BigDecimal sumPayed) {
        this.user = user;
        this.docType = docType;
        this.departmentPartId = departmentPartId;
        this.statusName = statusName;
        this.statusType = statusType;
        this.statusId = statusId;
        this.statusColor = statusColor;
        this.isCompleted = isCompleted;
        this.sumAll = sumAll;
        this.sumShipped=sumShipped;
        this.sumPayed=sumPayed;
    }

    public BigDecimal getSumAll() {
        return sumAll;
    }

    public void setSumAll(BigDecimal sumAll) {
        this.sumAll = sumAll;
    }

    public BigDecimal getSumShipped() {
        return sumShipped;
    }

    public void setSumShipped(BigDecimal sumShipped) {
        this.sumShipped = sumShipped;
    }

    public BigDecimal getSumPayed() {
        return sumPayed;
    }

    public void setSumPayed(BigDecimal sumPayed) {
        this.sumPayed = sumPayed;
    }

    public Boolean getCompleted() {
        return isCompleted;
    }

    public void setCompleted(Boolean completed) {
        isCompleted = completed;
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public Integer getStatusType() {
        return statusType;
    }

    public void setStatusType(Integer statusType) {
        this.statusType = statusType;
    }

    public CalendarUser getUser() {
        return user;
    }

    public void setUser(CalendarUser user) {
        this.user = user;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public Long getDepartmentPartId() {
        return departmentPartId;
    }

    public void setDepartmentPartId(Long departmentPartId) {
        this.departmentPartId = departmentPartId;
    }

    public Set<ItemResource> getItemResources() {
        return itemResources;
    }

    public void setItemResources(Set<ItemResource> itemResources) {
        this.itemResources = itemResources;
    }
}
