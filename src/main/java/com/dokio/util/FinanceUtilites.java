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

package com.dokio.util;

import com.dokio.repository.UserRepositoryJPA;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;

@Repository
public class FinanceUtilites {

    Logger logger = Logger.getLogger("FinanceUtilites");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    private UserDetailsServiceImpl userRepository;

//    Расчет баланса контрагента, строится на разнице сумм дебиторской и кредиторской задолженности с учётом суммы по корректировкам
//    Документы, генерирующие дебиторскую задолженность:
//            -Отгрузка*-
//            -Исходящий платеж*-
//            -Расходный ордер*-
//            -Возврат поставщику*-
//            -Корректировка*-
//
//    Документы, генерирующие кредиторскую задолженность:
//            -Приёмка*-
//            -Входящий платёж*-
//            -Приходный ордер*-
//            -Возврат покупателя*-
//            -Корректировка*-
//
//    Отрицательный результат - Нам должны
//    Положительный результат - Мы должны
    public BigDecimal getCagentBalance(Long companyId, Long cagentId) {

        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        String stringQuery ="WITH " +
        " credit as ("+
            " select "+
                " (select coalesce(sum(acp.product_sumprice),0) from acceptance_product acp where acp.acceptance_id in " +
                    " (select ac.id from acceptance ac where ac.master_id="+myMasterId+" and ac.company_id="+companyId+" and coalesce(ac.is_completed,false)=true and ac.cagent_id="+cagentId+")) " +
                " + " +
                " (select coalesce(sum(rcp.product_sumprice),0) from return_product rcp where rcp.return_id in " +
                    " (select rc.id from return rc where rc.master_id="+myMasterId+" and rc.company_id="+companyId+" and coalesce(rc.is_completed,false)=true and rc.cagent_id="+cagentId+")) " +
                " + " +
                " (select coalesce(sum(pi.summ),0) from paymentin pi where pi.master_id="+myMasterId+" and pi.company_id="+companyId+" and pi.cagent_id="+cagentId+" and coalesce(pi.is_completed,false)=true) " +
                " + " +
                " (select coalesce(sum(oi.summ),0) from orderin oi where oi.master_id="+myMasterId+" and oi.company_id="+companyId+" and oi.cagent_id="+cagentId+" and coalesce(oi.is_completed,false)=true) " +
        " ), " +
        " debet as ( " +
            " select " +
                " (select coalesce(sum(shp.product_sumprice),0) from shipment_product shp where shp.shipment_id in " +
                    " (select sh.id from shipment sh where sh.master_id="+myMasterId+" and sh.company_id="+companyId+" and coalesce(sh.is_completed,false)=true and sh.cagent_id="+cagentId+")) " +
                " + " +
                " (select coalesce(sum(rsp.product_sumprice),0) from returnsup_product rsp where rsp.returnsup_id in " +
                    " (select rs.id from returnsup rs where rs.master_id="+myMasterId+" and rs.company_id="+companyId+" and coalesce(rs.is_completed,false)=true and rs.cagent_id="+cagentId+")) " +
                " + " +
                " (select coalesce(sum(po.summ),0) from paymentout po where po.master_id="+myMasterId+" and po.company_id="+companyId+" and po.cagent_id="+cagentId+" and coalesce(po.is_completed,false)=true) " +
                " + " +
                " (select coalesce(sum(oo.summ),0) from orderout oo where oo.master_id="+myMasterId+" and oo.company_id="+companyId+" and oo.cagent_id="+cagentId+" and coalesce(oo.is_completed,false)=true) " +
        " )," +
        " correction as(" +
            " select " +
                " (select coalesce(sum(co.summ),0) from correction co where co.master_id="+myMasterId+" and co.company_id="+companyId+" and co.cagent_id="+cagentId+" and coalesce(co.is_completed,false)=true) " +
                " ) " +
            " select " +
                " ((select * from credit)-(select * from debet)+(select * from correction)) as balance";

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return (BigDecimal) query.getSingleResult();
        } catch (NoResultException nre) {
            return  new BigDecimal(0);
        } catch (Exception e) {
            logger.error("Exception in method getCagentBalance. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }



//    Расчет баланса расчётного счёта, строится на разнице сумм входящих и исходящих платежей с учётом суммы по корректировкам
//    Документы, генерирующие отрицательную составляюзую баланса:
//            -Исходящий платеж
//            -Корректировка
//
//    Документы, генерирующие положительную составляюзую баланса:
//            -Входящий платёж
//            -Корректировка

    public BigDecimal getPaymentAccountBalance(Long companyId, Long accountId) {

        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        String stringQuery ="WITH " +
        " income as ( " +
            " select " +
                " (select coalesce(sum(pi.summ),0) from paymentin pi where pi.master_id="+myMasterId+" and pi.company_id="+companyId+" and pi.payment_account_id="+accountId+" and coalesce(pi.is_completed,false)=true) " +
        " ), " +
        " outcome as( " +
            " select " +
                " (select coalesce(sum(po.summ),0) from paymentout po where po.master_id="+myMasterId+" and po.company_id="+companyId+" and po.payment_account_id="+accountId+" and coalesce(po.is_completed,false)=true) " +
        " )," +
        " correction as(" +
            " select " +
                " (select coalesce(sum(co.summ),0) from correction co where co.master_id="+myMasterId+" and co.company_id="+companyId+" and co.payment_account_id="+accountId+" and coalesce(co.is_completed,false)=true) " +
        " ) " +
        " select " +
            " ((select * from income)-(select * from outcome)+(select * from correction)) as balance";

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return (BigDecimal) query.getSingleResult();
        } catch (NoResultException nre) {
            return  new BigDecimal(0);
        } catch (Exception e) {
            logger.error("Exception in method getPaymentAccountBalance. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

//    Расчет баланса кассы предприятия, строится на разнице сумм приходных и расходных кассовых ордеров с учётом суммы по корректировкам
//    Документы, генерирующие отрицательную составляюзую баланса:
//            -Расходный ордер
//            -Корректировка
//
//    Документы, генерирующие положительную составляюзую баланса:
//            -Приходный оредер
//            -Корректировка

    public BigDecimal getBoxofficeBalance(Long companyId, Long boxofficeId) {

        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        String stringQuery ="WITH " +
        " income as ( " +
            " select " +
                " (select coalesce(sum(oi.summ),0) from orderin oi where oi.master_id="+myMasterId+" and oi.company_id="+companyId+" and boxoffice_id="+boxofficeId+" and coalesce(oi.is_completed,false)=true) " +
        " ), " +
        " outcome as ( " +
            " select " +
                " (select coalesce(sum(oo.summ),0) from orderout oo where oo.master_id="+myMasterId+" and oo.company_id="+companyId+" and boxoffice_id="+boxofficeId+" and coalesce(oo.is_completed,false)=true) " +
        " )," +
        " correction as(" +
            " select " +
                " (select coalesce(sum(co.summ),0) from correction co where co.master_id="+myMasterId+" and co.company_id="+companyId+" and co.boxoffice_id="+boxofficeId+" and coalesce(co.is_completed,false)=true) " +
        " ) " +
            " select " +
                " ((select * from income)-(select * from outcome)+(select * from correction)) as balance";

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return (BigDecimal) query.getSingleResult();
        } catch (NoResultException nre) {
            return  new BigDecimal(0);
        } catch (Exception e) {
            logger.error("Exception in method getBoxofficeBalance. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }


    // возвращает балансы на дату
    public BigDecimal getBalancesOnDate(Long companyId, String dateTo) {

        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String myTimeZone = userRepository.getUserTimeZone();
        String stringQuery =" WITH " +
        " income_payment_account as ( " +
        "   select " +
        "       (select coalesce(sum(pi.summ),0) from paymentin pi  where pi.master_id="+myMasterId+" and pi.company_id="+companyId+" and coalesce(pi.is_completed,false)=true and pi.date_time_created at time zone '"+myTimeZone+"'  < to_timestamp(:dateTo||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and pi.payment_account_id in (select id from companies_payment_accounts where master_id="+myMasterId+" and company_id="+companyId+")) " +
        "   ), " +
        " outcome_payment_account as( " +
        "   select " +
        "       (select coalesce(sum(po.summ),0) from paymentout po where po.master_id="+myMasterId+" and po.company_id="+companyId+" and coalesce(po.is_completed,false)=true and po.date_time_created at time zone '"+myTimeZone+"'  < to_timestamp(:dateTo||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS')  and po.payment_account_id in (select id from companies_payment_accounts where master_id="+myMasterId+" and company_id="+companyId+")) " +
        "   ), " +
        " correction_payment_account as( " +
        "   select " +
        "       (select coalesce(sum(co.summ),0) from correction co where co.master_id="+myMasterId+" and co.company_id="+companyId+" and coalesce(co.is_completed,false)=true and co.date_time_created at time zone '"+myTimeZone+"'  < to_timestamp(:dateTo||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and co.type='account' and co.payment_account_id in (select id from companies_payment_accounts where master_id="+myMasterId+" and company_id="+companyId+")) " +
        "   ), " +
        " income_boxoffice as ( " +
        "   select " +
        "       (select coalesce(sum(oi.summ),0) from orderin oi    where oi.master_id="+myMasterId+" and oi.company_id="+companyId+" and coalesce(oi.is_completed,false)=true and oi.date_time_created at time zone '"+myTimeZone+"'  < to_timestamp(:dateTo||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and oi.boxoffice_id in (select id from sprav_boxoffice where master_id="+myMasterId+" and company_id="+companyId+")) " +
        "   ), " +
        " outcome_boxoffice as( " +
        "   select " +
        "       (select coalesce(sum(oo.summ),0) from orderout oo   where oo.master_id="+myMasterId+" and oo.company_id="+companyId+" and coalesce(oo.is_completed,false)=true and oo.date_time_created at time zone '"+myTimeZone+"'  < to_timestamp(:dateTo||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and oo.boxoffice_id in (select id from sprav_boxoffice where master_id="+myMasterId+" and company_id="+companyId+")) " +
        "   ), " +
        " correction_boxoffice as( " +
        "   select " +
        "       (select coalesce(sum(cb.summ),0) from correction cb where cb.master_id="+myMasterId+" and cb.company_id="+companyId+" and coalesce(cb.is_completed,false)=true and cb.date_time_created at time zone '"+myTimeZone+"'  < to_timestamp(:dateTo||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and cb.type='boxoffice' and cb.boxoffice_id in (select id from sprav_boxoffice where master_id="+myMasterId+" and company_id="+companyId+")) " +
        "   ) " +
        " select " +
        " ((select * from income_payment_account)-(select * from outcome_payment_account)+(select * from correction_payment_account))+ " +
        " ((select * from income_boxoffice)-(select * from outcome_boxoffice)+(select * from correction_boxoffice)) as balance; ";

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("dateTo",dateTo);
            return (BigDecimal) query.getSingleResult();
        } catch (NoResultException nre) {
            return  new BigDecimal(0);
        } catch (Exception e) {
            logger.error("Exception in method getBalancesOnDate. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }



    // есть ли проведенный Входящий платеж или Приходный оредр у Исходящего платежа или Расходного ордера с данным id
    public Boolean isRecipientCompleted(Long myMasterId, Long id, String docTableName, String docColumnName){

        String stringQuery;
        stringQuery =
            " select id from "+docTableName+
            " where master_id = :myMasterId and " + docColumnName + " = :id and is_completed = true";
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("id",id);
            query.setParameter("myMasterId",myMasterId);
            return (query.getResultList().size()>0);
        }
        catch (NoResultException nre) {
            return false;
        }
        catch (Exception e) {
            logger.error("Exception in method isRecipientCompleted. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }


}
