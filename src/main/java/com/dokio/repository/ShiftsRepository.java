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

package com.dokio.repository;

import com.dokio.message.response.Reports.ShiftsJSON;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.additional.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class ShiftsRepository {

    Logger logger = Logger.getLogger("ShiftsRepository");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserDetailsServiceImpl userRepository;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    SecurityRepositoryJPA securityRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    ProductsRepositoryJPA productsRepository;


    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("shift_number","name","kassa","acquiring_bank","zn_kkt","revenue_all","num_receipts","revenue_cash","revenue_electronically","company","department","creator","date_time_created_sort","date_time_closed_sort","shift_status_id","fn_serial","closer")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));


//*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<ShiftsJSON> getShiftsTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, Long companyId, Long departmentId, Long cashierId, Long kassaId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(43L, "560,561,566"))//(см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            boolean needToSetParameter_MyDepthsIds = false;
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());


            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as closer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.closer_id as closer_id, " +
                    "           p.company_id as company_id, " +
                    "           p.department_id as department_id, " +
                    "           dp.name as department, " +
                    "           p.shift_number as shift_number, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_closed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_closed, " +

                    "           p.kassa_id as kassa_id, " +// id KKM
                    "           ka.name as kassa, " +
                    "           p.acquiring_bank_id as acquiring_bank_id, " + // id банка эквайера
                    "           aqu.name as acquiring_bank, " + // наименование банка эквайера
                    "           p.zn_kkt as zn_kkt, " +  // заводской номер KKT
                    "           p.shift_status_id as shift_status_id, " +
                    "           p.shift_expired_at as shift_expired_at, " +
                    "           p.fn_serial as fn_serial, " +
                    "           p.uid as uid, " +
                    "           (" +
                    "               coalesce((select sum(coalesce(cash,0)) from receipts where shift_id=p.id and operation_id='sell'),0)-" +
                    "               coalesce((select sum(coalesce(cash,0)) from receipts where shift_id=p.id and operation_id='return'),0)" +
                    "           ) as revenue_cash, " +
                    "           (" +
                    "               coalesce((select sum(coalesce(electronically,0)) from receipts where shift_id=p.id and operation_id='sell'),0)-" +
                    "               coalesce((select sum(coalesce(electronically,0)) from receipts where shift_id=p.id and operation_id='return'),0)" +
                    "           ) as revenue_electronically, " +
                    "           (" +
                    "               coalesce((select sum(coalesce(cash,0))+sum(coalesce(electronically,0)) from receipts where shift_id=p.id and operation_id='sell'),0)-" +
                    "               coalesce((select sum(coalesce(cash,0))+sum(coalesce(electronically,0)) from receipts where shift_id=p.id and operation_id='return'),0)" +
                    "           ) as revenue_all, " +
                    "           coalesce((select count(*) from receipts where shift_id=p.id),0) as num_receipts," +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_closed as date_time_closed_sort " +

                    "           from shifts p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           INNER JOIN kassa ka ON p.kassa_id=ka.id " +
                    "           INNER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN cagents aqu ON p.acquiring_bank_id=aqu.id " +
                    "           LEFT OUTER JOIN users uc ON p.closer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId+
                    ((cashierId>0)?" and (p.creator_id = "+cashierId+" or p.closer_id="+cashierId+")":"") +
                    ((kassaId>0)?" and p.kassa_id = "+kassaId:"");


            if (!securityRepositoryJPA.userHasPermissions_OR(43L, "560")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения
                if (!securityRepositoryJPA.userHasPermissions_OR(43L, "561")) //Если нет прав на просм по своему предприятию
                {//остается только на просмотр всех доков в своих отделениях (566)
                    stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;
                }//т.е. по всем и своему предприятиям нет а на свои отделения есть
                else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(p.shift_number,'0000000000') like CONCAT('%',:sg) or "+
                        " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(aqu.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(ka.name)  like upper(CONCAT('%',:sg,'%'))"+")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            if (departmentId > 0) {
                stringQuery = stringQuery + " and p.department_id=" + departmentId;
            }


            if (VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) && VALID_COLUMNS_FOR_ASC.contains(sortAsc)) {
                stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            } else {
                throw new IllegalArgumentException("Invalid query parameters");
            }


            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                query.setFirstResult(offsetreal).setMaxResults(result);


                List<Object[]> queryList = query.getResultList();
                List<ShiftsJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    ShiftsJSON doc=new ShiftsJSON();
                    doc.setId(Long.parseLong(                     obj[0].toString()));
                    doc.setMaster((String)                        obj[1]);
                    doc.setCreator((String)                       obj[2]);
                    doc.setCloser((String)                        obj[3]);
                    doc.setMaster_id(Long.parseLong(              obj[4].toString()));
                    doc.setCreator_id(Long.parseLong(             obj[5].toString()));
                    doc.setCloser_id(obj[6]!=null?Long.parseLong( obj[6].toString()):null);
                    doc.setCompany_id(Long.parseLong(             obj[7].toString()));
                    doc.setDepartment_id(Long.parseLong(          obj[8].toString()));
                    doc.setDepartment((String)                    obj[9]);
                    doc.setShift_number((Integer)                 obj[10]);
                    doc.setCompany((String)                       obj[11]);
                    doc.setDate_time_created((String)             obj[12]);
                    doc.setDate_time_closed((String)              obj[13]);
                    doc.setKassa_id(Long.parseLong(               obj[14].toString()));
                    doc.setKassa((String)                         obj[15]);
                    doc.setAcquiring_bank_id(obj[16]!=null?Long.parseLong( obj[16].toString()):null);
                    doc.setAcquiring_bank((String)                obj[17]);
                    doc.setZn_kkt((String)                        obj[18]);
                    doc.setShift_status_id((String)               obj[19]);
                    doc.setShift_expired_at((String)              obj[20]);
                    doc.setFn_serial((String)                     obj[21]);
                    doc.setUid((String)                           obj[22]);
                    doc.setRevenue_cash((BigDecimal)              obj[23]);
                    doc.setRevenue_electronically((BigDecimal)    obj[24]);
                    doc.setRevenue_all((BigDecimal)               obj[25]);
                    doc.setNum_receipts(Long.parseLong(           obj[26].toString()));
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getShiftsTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getShiftsSize(int result, String searchString, Long companyId, Long departmentId, Long cashierId, Long kassaId, Set<Integer> filterOptionsIds) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from shifts p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN users u ON p.master_id=u.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           INNER JOIN kassa ka ON p.kassa_id=ka.id " +
                "           INNER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN cagents aqu ON p.acquiring_bank_id=aqu.id " +
                "           LEFT OUTER JOIN users uc ON p.closer_id=uc.id " +
                "           where  p.master_id=" + myMasterId+
                ((cashierId>0)?" and (p.creator_id = "+cashierId+" or p.closer_id="+cashierId+")":"") +
                ((kassaId>0)?" and p.kassa_id = "+kassaId:"");

        if (!securityRepositoryJPA.userHasPermissions_OR(43L, "560")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения
            if (!securityRepositoryJPA.userHasPermissions_OR(43L, "561")) //Если нет прав на просм по своему предприятию
            {//остается только на просмотр всех доков в своих отделениях (566)
                stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;
            }//т.е. по всем и своему предприятиям нет а на свои отделения есть
            else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
        }

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.shift_number,'0000000000') like CONCAT('%',:sg) or "+
                    " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(aqu.name) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(ka.name)  like upper(CONCAT('%',:sg,'%'))"+")";
        }
        if (companyId > 0) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
        }
        if (departmentId > 0) {
            stringQuery = stringQuery + " and p.department_id=" + departmentId;
        }
        try{

            Query query = entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}
            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}
            return query.getResultList().size();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getShiftsSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

    // Возвращает список всех когда либо использовавшихся касс (если department_id > 0 то в отделении)
    @SuppressWarnings("Duplicates")
    public List<IdAndNameJSON>getShiftsKassa(Long companyId, Long department_id, String docName){
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;

        boolean onlyMyDepths;
        if(docName.equals("shifts")) onlyMyDepths=!securityRepositoryJPA.userHasPermissions_OR(43L, "560,561"); // Если нет прав на просм по всем предприятиям или по всем отделениям своего предприятия - будет фильтр только по своим отделениям
        else onlyMyDepths=!securityRepositoryJPA.userHasPermissions_OR(44L, "563,564"); // "receipts"

        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        stringQuery=
                "select id, name from kassa where master_id="+myMasterId+" and company_id="+companyId+" and id in " +
                        "(select kassa_id from shifts where master_id="+myMasterId+" and company_id = "+ companyId +
                        ((department_id>0)?" and department_id = "+department_id:"");

        if (onlyMyDepths){//Если нет прав на просм по всем предприятиям или по всем отделениям своего предприятия
            stringQuery=stringQuery + " and department_id in :myDepthsIds "; needToSetParameter_MyDepthsIds=true;
        }
        stringQuery=stringQuery + " group by kassa_id) order by name asc";
        try
        {
            Query query =  entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            List<Object[]> queryList = query.getResultList();
            List<IdAndNameJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                IdAndNameJSON doc=new IdAndNameJSON();
                doc.setId(Long.parseLong(                             obj[0].toString()));
                doc.setName((String)                                  obj[1]);
                returnList.add(doc);
            }
            return returnList;
        }catch (Exception e) {
            logger.error("Exception in method getShiftsKassa. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    // Возвращает список всех когда либо работавших с кассами пользователей касс (если department_id > 0 то в отделении)
    @SuppressWarnings("Duplicates")
    public List<IdAndNameJSON>getShiftsCashiers(Long companyId, Long department_id, String docName){
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;

        boolean onlyMyDepths;
        if(docName.equals("shifts")) onlyMyDepths=!securityRepositoryJPA.userHasPermissions_OR(43L, "560,561"); // Если нет прав на просм по всем предприятиям или по всем отделениям своего предприятия - будет фильтр только по своим отделениям
        else onlyMyDepths=!securityRepositoryJPA.userHasPermissions_OR(44L, "563,564"); // "receipts"

        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        stringQuery=
                "select id, name from users where master_id="+myMasterId+" and company_id="+companyId+" and id in " +
                        "(" +
                                        "select creator_id from shifts where master_id="+myMasterId+" and company_id = "+ companyId +
                                        ((department_id>0)?" and department_id = "+department_id:"");

        if (onlyMyDepths){//Если нет прав на просм по всем предприятиям или по всем отделениям своего предприятия
            stringQuery=stringQuery +   " and department_id in :myDepthsIds "; needToSetParameter_MyDepthsIds=true;
        }

        stringQuery=stringQuery + " UNION ";

        stringQuery=stringQuery +       " select closer_id from shifts where master_id="+myMasterId+" and company_id = "+ companyId +
                ((department_id>0)?" and department_id = "+department_id:"");

        if (onlyMyDepths){//Если нет прав на просм по всем предприятиям или по всем отделениям своего предприятия
            stringQuery=stringQuery +   " and department_id in :myDepthsIds "; needToSetParameter_MyDepthsIds=true;
        }

        stringQuery=stringQuery +       ") order by name asc";
        try
        {
            Query query =  entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            List<Object[]> queryList = query.getResultList();
            List<IdAndNameJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                IdAndNameJSON doc=new IdAndNameJSON();
                doc.setId(Long.parseLong(                             obj[0].toString()));
                doc.setName((String)                                  obj[1]);
                returnList.add(doc);
            }
            return returnList;
        }catch (Exception e) {
            logger.error("Exception in method getShiftsCashiers. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }



}