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

import java.math.BigDecimal;

public class WriteoffProductForm {
    private Long        product_id;
    private Long        writeoff_id;
    private BigDecimal  product_count;
    private Long        edizm_id;
    private BigDecimal  product_price;
    private BigDecimal  product_sumprice;
    private BigDecimal  product_netcost;
    private Integer     nds_id;
    private String      name;
    private String      nds;
    private String      edizm;
    private String      reason;
    private Integer     reason_id;
    private String      additional;
    private Boolean     indivisible;                    // неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
    private BigDecimal  total;

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Boolean getIndivisible() {
        return indivisible;
    }

    public void setIndivisible(Boolean indivisible) {
        this.indivisible = indivisible;
    }

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public Long getWriteoff_id() {
        return writeoff_id;
    }

    public void setWriteoff_id(Long writeoff_id) {
        this.writeoff_id = writeoff_id;
    }

    public Integer getReason_id() {
        return reason_id;
    }

    public void setReason_id(Integer reason_id) {
        this.reason_id = reason_id;
    }

    public String getAdditional() {
        return additional;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public BigDecimal getProduct_count() {
        return product_count;
    }

    public void setProduct_count(BigDecimal product_count) {
        this.product_count = product_count;
    }

    public Long getEdizm_id() {
        return edizm_id;
    }

    public void setEdizm_id(Long edizm_id) {
        this.edizm_id = edizm_id;
    }

    public BigDecimal getProduct_price() {
        return product_price;
    }

    public void setProduct_price(BigDecimal product_price) {
        this.product_price = product_price;
    }

    public BigDecimal getProduct_sumprice() {
        return product_sumprice;
    }

    public void setProduct_sumprice(BigDecimal product_sumprice) {
        this.product_sumprice = product_sumprice;
    }

    public BigDecimal getProduct_netcost() {
        return product_netcost;
    }

    public void setProduct_netcost(BigDecimal product_netcost) {
        this.product_netcost = product_netcost;
    }

    public Integer getNds_id() {
        return nds_id;
    }

    public void setNds_id(Integer nds_id) {
        this.nds_id = nds_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNds() {
        return nds;
    }

    public void setNds(String nds) {
        this.nds = nds;
    }

    public String getEdizm() {
        return edizm;
    }

    public void setEdizm(String edizm) {
        this.edizm = edizm;
    }
}
