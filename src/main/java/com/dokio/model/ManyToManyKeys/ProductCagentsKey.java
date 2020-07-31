/*
Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
программного обеспечения;
Эта программа распространяется в расчете на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу:
<http://www.gnu.org/licenses/>
 */
package com.dokio.model.ManyToManyKeys;
// Данный класс выполняет роль первичного ключа для класса ProductFields и таблицы product_fields
// которая связывает таблицы products и product_group_fields
// https://www.baeldung.com/jpa-many-to-many

// Класс для связи Товаров и услуг (Products) и их поставщиков (Cagents)

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ProductCagentsKey implements Serializable {

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "cagent_id")
    private Long cagentId;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getCagentId() {
        return cagentId;
    }

    public void setCagentId(Long cagentId) {
        this.cagentId = cagentId;
    }
}