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

public class ReturnsupProductTableForm {
    private Long returnsup_id;                         // id родиельского документа
    private Long product_id;                        // id товара
    private BigDecimal product_price;               // цена
    private BigDecimal  product_count;              // кол-во товара
    private Integer nds_id;                         // ндс
    private BigDecimal product_sumprice;            // сумма

    public Long getReturnsup_id() {
        return returnsup_id;
    }

    public void setReturnsup_id(Long returnsup_id) {
        this.returnsup_id = returnsup_id;
    }

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public BigDecimal getProduct_price() {
        return product_price;
    }

    public void setProduct_price(BigDecimal product_price) {
        this.product_price = product_price;
    }

    public BigDecimal getProduct_count() {
        return product_count;
    }

    public void setProduct_count(BigDecimal product_count) {
        this.product_count = product_count;
    }

    public Integer getNds_id() {
        return nds_id;
    }

    public void setNds_id(Integer nds_id) {
        this.nds_id = nds_id;
    }

    public BigDecimal getProduct_sumprice() {
        return product_sumprice;
    }

    public void setProduct_sumprice(BigDecimal product_sumprice) {
        this.product_sumprice = product_sumprice;
    }
}
