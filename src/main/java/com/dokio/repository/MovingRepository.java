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
package com.dokio.repository;

import com.dokio.message.request.MovingForm;
import com.dokio.message.request.MovingProductForm;
import com.dokio.message.request.SearchForm;
import com.dokio.message.request.Settings.SettingsMovingForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.MovingJSON;
import com.dokio.message.response.Settings.SettingsMovingJSON;
import com.dokio.message.response.additional.FilesMovingJSON;
import com.dokio.message.response.ProductHistoryJSON;
import com.dokio.message.response.additional.LinkedDocsJSON;
import com.dokio.message.response.additional.MovingProductTableJSON;
import com.dokio.repository.Exceptions.CantInsertProductRowCauseErrorException;
import com.dokio.repository.Exceptions.CantInsertProductRowCauseOversellException;
import com.dokio.repository.Exceptions.CantSaveProductQuantityException;
import com.dokio.repository.Exceptions.InsertProductHistoryExceprions;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Repository
public class MovingRepository {
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
    DepartmentRepositoryJPA departmentRepositoryJPA;
    @Autowired
    private CommonUtilites commonUtilites;
    @Autowired
    ProductsRepositoryJPA productsRepository;

    Logger logger = Logger.getLogger("MovingRepository");

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("doc_number","status_name","product_count","is_completed","company","department_from","department_to","creator","date_time_created_sort","description")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));
    //*****************************************************************************************************************************************************
    //****************************************************      MENU      *********************************************************************************
    //*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<MovingJSON> getMovingTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, Long companyId, Long departmentFromId, Long departmentToId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(30L, "384,385,386,387"))//(см. файл Permissions Id)
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные

            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           coalesce(p.overhead,0) as overhead, " +
                    "           p.department_from_id as department_from_id, " +
                    "           p.department_to_id as department_to_id, " +
                    "           dp_from.name as department_from, " +
                    "           dp_to.name as department_to, " +
                    "           p.doc_number as doc_number, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           coalesce(p.overhead_netcost_method,0) as overhead_netcost_method, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           (select count(*) from moving_product ip where coalesce(ip.moving_id,0)=p.id) as product_count" + //подсчет кол-ва товаров
                    "           from moving p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp_from ON p.department_from_id=dp_from.id " +
                    "           INNER JOIN departments dp_to ON p.department_to_id=dp_to.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(30L, "384")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(30L, "385")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(30L, "386")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_from_id in :myDepthsIds and p.department_to_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_from_id in :myDepthsIds and p.department_to_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +

                        " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                        " upper(dp_from.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(dp_to.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.description) like upper(CONCAT('%',:sg,'%'))"+")";

            }
            if (companyId > 0L) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            if (departmentFromId > 0L) {
                stringQuery = stringQuery + " and p.department_from_id=" + departmentFromId;
            }
            if (departmentToId > 0L) {
                stringQuery = stringQuery + " and p.department_to_id=" + departmentToId;
            }
            if (VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) && VALID_COLUMNS_FOR_ASC.contains(sortAsc)) {
                stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            } else {
                throw new IllegalArgumentException("Недопустимые параметры запроса");
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery)
                        .setFirstResult(offsetreal)
                        .setMaxResults(result);
                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}
                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}
                List<Object[]> queryList = query.getResultList();
                List<MovingJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    MovingJSON doc=new MovingJSON();
                    doc.setId(Long.parseLong(                     obj[0].toString()));
                    doc.setMaster((String)                        obj[1]);
                    doc.setCreator((String)                       obj[2]);
                    doc.setChanger((String)                       obj[3]);
                    doc.setMaster_id(Long.parseLong(              obj[4].toString()));
                    doc.setCreator_id(Long.parseLong(             obj[5].toString()));
                    doc.setChanger_id(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
                    doc.setCompany_id(Long.parseLong(             obj[7].toString()));
                    doc.setOverhead((BigDecimal)                  obj[8]);
                    doc.setDepartment_from_id(Long.parseLong(     obj[9].toString()));
                    doc.setDepartment_to_id(Long.parseLong(       obj[10].toString()));
                    doc.setDepartment_from((String)               obj[11]);
                    doc.setDepartment_to((String)                 obj[12]);
                    doc.setDoc_number(Long.parseLong(             obj[13].toString()));
                    doc.setCompany((String)                       obj[14]);
                    doc.setDate_time_created((String)             obj[15]);
                    doc.setDate_time_changed((String)             obj[16]);
                    doc.setDescription((String)                   obj[17]);
                    doc.setIs_completed((Boolean)                 obj[18]);
                    doc.setOverhead_netcost_method((Integer)      obj[21]);
                    doc.setStatus_id(obj[22]!=null?Long.parseLong(obj[22].toString()):null);
                    doc.setStatus_name((String)                   obj[23]);
                    doc.setStatus_color((String)                  obj[24]);
                    doc.setStatus_description((String)            obj[25]);
                    doc.setProduct_count(Long.parseLong(          obj[26].toString()));
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getMovingTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getMovingSize(String searchString, Long companyId, Long departmentFromId, Long departmentToId, Set<Integer> filterOptionsIds) {
        Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from moving p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN departments dp_from ON p.department_from_id=dp_from.id " +
                "           INNER JOIN departments dp_to ON p.department_to_id=dp_to.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(30L, "384")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(30L, "385")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(30L, "386")) //Если нет прав на просмотр всех доков в своих подразделениях
                {//остается только на свои документы
                    stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_from_id in :myDepthsIds and p.department_to_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                }else{stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_from_id in :myDepthsIds and p.department_to_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
            } else stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
        }

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +

                    " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                    " upper(dp_from.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(dp_to.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(p.description) like upper(CONCAT('%',:sg,'%'))"+")";
        }
        if (companyId > 0L) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
        }
        if (departmentFromId > 0L) {
            stringQuery = stringQuery + " and p.department_from_id=" + departmentFromId;
        }
        if (departmentToId > 0L) {
            stringQuery = stringQuery + " and p.department_to_id=" + departmentToId;
        }
        try{
            Query query = entityManager.createNativeQuery(stringQuery);

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}

            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            return query.getResultList().size();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getMovingSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<MovingProductTableJSON> getMovingProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(30L, "384,385,386,387"))//(см. файл Permissions Id)
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            stringQuery =   " select " +
                    " ap.product_id," +
                    " ap.moving_id," +
                    " ap.product_count," +
                    " ap.product_price," +
                    " ap.product_sumprice," +
                    " ap.product_netcost," +
                    " p.name as name," +
                    " (select edizm.short_name from sprav_sys_edizm edizm where edizm.id = p.edizm_id) as edizm," +
                    " p.indivisible as indivisible," +// неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
                    " coalesce((select quantity from product_quantity where product_id = ap.product_id and department_id = a.department_from_id),0) as total, "+ //всего на складе (т.е остаток)
                    " (select " +
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    "   sum(coalesce(reserved_current,0)-0) " +//пока отгрузки не реализованы, считаем, что отгружено 0. Потом надо будет высчитывать из всех Отгрузок, исходящих из этого Заказа покупателя
                    //по логике: сумма( резерв > (всего - отгружено) ? (всего - отгружено) : резерв)    (при условии не позволять в заказах покупателей делать резерв больше "всего" (reserved_current!>product_count))
                    "   from " +
                    "   customers_orders_product " +
                    "   where " +
                    "   product_id=ap.product_id "+
                    "   and department_id = a.department_from_id) as reserved, "+//зарезервировано во всех документах Заказ покупателя в отделении "ИЗ"
                    " ppr.is_material as is_material " +
                    " from " +
                    " moving_product ap " +
                    " INNER JOIN moving a ON ap.moving_id=a.id " +
                    " INNER JOIN products p ON ap.product_id=p.id " +
                    " INNER JOIN sprav_sys_ppr ppr ON p.ppr_id=ppr.id " +
                    " where a.master_id = " + myMasterId +
                    " and ap.moving_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(30L, "384")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(30L, "385")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(30L, "386")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_from_id in :myDepthsIds and p.department_to_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_from_id in :myDepthsIds and p.department_to_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and a.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
            }

            stringQuery = stringQuery + " order by p.name asc ";
            Query query = entityManager.createNativeQuery(stringQuery);
            try{
                if(needToSetParameter_MyDepthsIds)
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();
                List<MovingProductTableJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    MovingProductTableJSON doc=new MovingProductTableJSON();
                    doc.setProduct_id(Long.parseLong(                       obj[0].toString()));
                    doc.setMoving_id(Long.parseLong(                        obj[1].toString()));
                    doc.setProduct_count((BigDecimal)                       obj[2]);
                    doc.setProduct_price((BigDecimal)                       obj[3]);
                    doc.setProduct_sumprice((BigDecimal)                    obj[4]);
                    doc.setProduct_netcost((BigDecimal)                     obj[5]);
                    doc.setName((String)                                    obj[6]);
                    doc.setEdizm((String)                                   obj[7]);
                    doc.setIndivisible((Boolean)                            obj[8]);
                    doc.setTotal((BigDecimal)                               obj[9]);
                    doc.setReserved(                                        obj[10]==null?BigDecimal.ZERO:(BigDecimal)obj[10]);
                    doc.setIs_material((Boolean)                            obj[11]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getMovingProductTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    //*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    @Transactional
    public MovingJSON getMovingValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(30L, "384,385,386,387"))//см. _Permissions Id.txt
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
            String myTimeZone = userRepository.getUserTimeZone();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            stringQuery = "select " +
                    "           p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           coalesce(p.overhead,0) as overhead, " +
                    "           p.department_from_id as department_from_id, " +
                    "           p.department_to_id as department_to_id, " +
                    "           dp_from.name as department_from, " +
                    "           dp_to.name as department_to, " +
                    "           p.doc_number as doc_number, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           coalesce(p.overhead_netcost_method,0) as overhead_netcost_method, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description " +
                    "           from moving p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp_from ON p.department_from_id=dp_from.id " +
                    "           INNER JOIN departments dp_to ON p.department_to_id=dp_to.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;
            if (!securityRepositoryJPA.userHasPermissions_OR(30L, "384")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(30L, "385")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(30L, "386")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_from_id in :myDepthsIds and p.department_to_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_from_id in :myDepthsIds and p.department_to_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();

                MovingJSON returnObj=new MovingJSON();

                for(Object[] obj:queryList){
                    returnObj.setId(Long.parseLong(                     obj[0].toString()));
                    returnObj.setMaster((String)                        obj[1]);
                    returnObj.setCreator((String)                       obj[2]);
                    returnObj.setChanger((String)                       obj[3]);
                    returnObj.setMaster_id(Long.parseLong(              obj[4].toString()));
                    returnObj.setCreator_id(Long.parseLong(             obj[5].toString()));
                    returnObj.setChanger_id(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
                    returnObj.setCompany_id(Long.parseLong(             obj[7].toString()));
                    returnObj.setOverhead((BigDecimal)                  obj[8]);
                    returnObj.setDepartment_from_id(Long.parseLong(     obj[9].toString()));
                    returnObj.setDepartment_to_id(Long.parseLong(       obj[10].toString()));
                    returnObj.setDepartment_from((String)               obj[11]);
                    returnObj.setDepartment_to((String)                 obj[12]);
                    returnObj.setDoc_number(Long.parseLong(             obj[13].toString()));
                    returnObj.setCompany((String)                       obj[14]);
                    returnObj.setDate_time_created((String)             obj[15]);
                    returnObj.setDate_time_changed((String)             obj[16]);
                    returnObj.setDescription((String)                   obj[17]);
                    returnObj.setIs_completed((Boolean)                 obj[18]);
                    returnObj.setOverhead_netcost_method((Integer)      obj[21]);
                    returnObj.setStatus_id(obj[22]!=null?Long.parseLong(obj[22].toString()):null);
                    returnObj.setStatus_name((String)                   obj[23]);
                    returnObj.setStatus_color((String)                  obj[24]);
                    returnObj.setStatus_description((String)            obj[25]);
                }
                return returnObj;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getMovingValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class, CantInsertProductRowCauseErrorException.class})
    public Long insertMoving(MovingForm request) {

        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        // для случая, если у пользователя есть только право на "Создание для своих отделений",  необходимо проверить возможность для создания для всех отделений (отделение "ИЗ" и отделение "В")
        Boolean iCan = securityRepositoryJPA.userHasPermissionsToCreateDock( request.getCompany_id(), request.getDepartment_from_id(), 30L, "377", "378", "379") &&
                       securityRepositoryJPA.userHasPermissionsToCreateDock( request.getCompany_id(), request.getDepartment_to_id(), 30L, "377", "378", "379");
        if(iCan==Boolean.TRUE)
        {
            String stringQuery;
            Long myId = userRepository.getUserId();
            Long newDockId;
            Long doc_number;//номер документа

            //генерируем номер документа, если его (номера) нет
            if (request.getDoc_number() != null) {
                doc_number=Long.valueOf(request.getDoc_number());
            } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"moving");

            String timestamp = new Timestamp(System.currentTimeMillis()).toString();

            stringQuery =   "insert into moving (" +
                    " master_id," + //мастер-аккаунт
                    " creator_id," + //создатель
                    " company_id," + //предприятие, для которого создается документ
                    " department_from_id," + //отделение, из которого перемещают
                    " department_to_id," + //отделение в которое перемещают
                    " date_time_created," + //дата и время создания
                    " overhead," +
                    " overhead_netcost_method," +
                    " doc_number," + //номер заказа
                    " description," +//доп. информация по заказу
                    " status_id" + //статус
                    ") values ("+
                    myMasterId + ", "+//мастер-аккаунт
                    myId + ", "+ //создатель
                    request.getCompany_id() + ", "+//предприятие, для которого создается документ
                    request.getDepartment_from_id() + ", "+//отделение, из(для) которого создается документ
                    request.getDepartment_to_id() + ", "+//отделение, из(для) которого создается документ
                    "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                    request.getOverhead() + ", "+
                    request.getOverhead_netcost_method() + ", "+
                    doc_number + ", "+//номер заказа
                    " :description, " +//описание
                    request.getStatus_id() + ")";// статус
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
                query.executeUpdate();
                stringQuery = "select id from moving where creator_id=" + myId + " and date_time_created=(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS'))";
                Query query2 = entityManager.createNativeQuery(stringQuery);
                newDockId = Long.valueOf(query2.getSingleResult().toString());

                if(insertMovingProducts(request, newDockId, myMasterId)){
                    return newDockId;
                } else return null;
            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertMoving on inserting into moving_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                logger.error("Exception in method insertMoving. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else {
            //null - ошибка, т.е. либо предприятие или отдление не принадлежат мастер-аккаунту, либо друг другу
            //0 - недостаточно прав
            if(Objects.isNull(iCan)) return null; else return 0L;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class, CantInsertProductRowCauseErrorException.class, CantSaveProductQuantityException.class, InsertProductHistoryExceprions.class})
    public  Integer updateMoving(MovingForm request) {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(30L,"388") && securityRepositoryJPA.isItAllMyMastersDocuments("moving",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(30L,"389") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("moving",request.getId().toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(30L,"390") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("moving",request.getId().toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(30L,"391") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("moving",request.getId().toString())))
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            try {
                //апдейт основного документа, без таблицы товаров
                updateMovingWithoutTable(request,myMasterId);
                //сохранение таблицы
                insertMovingProducts(request, request.getId(), myMasterId);
                //если завершается приемка - запись в историю товара
                if(request.isIs_completed()){

                    for (MovingProductForm row : request.getMovingProductTable()) {
                        if (!addMovingProductHistory(row, request, myMasterId)) {//       //сохранение истории операций с записью актуальной инфо о количестве товара в отделении
                            break;
                        } else {
                            if (!setProductQuantity(row, request, myMasterId)) {// запись о количестве товара в отделении в отдельной таблице
                                break;
                            }
                        }
                    }
                }
                return 1;
            } catch (CantSaveProductQuantityException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updateMoving on inserting into product_quantity cause error.", e);
                e.printStackTrace();
                return null;
            } catch (CantInsertProductRowCauseOversellException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updateMoving on updating cause oversell.", e);
                e.printStackTrace();
                return 0;// недостаточно товара на складе
            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updateMoving on inserting into moving_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (CantSaveProductHistoryException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updateMoving on inserting into products_history.", e);
                e.printStackTrace();
                return null;
            } catch (Exception e){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updateMoving.", e);
                e.printStackTrace();
                return null;
            }
        } else return -1;//недостаточно прав
    }

    @SuppressWarnings("Duplicates")
    private Boolean updateMovingWithoutTable(MovingForm request, Long myMasterId) {

        Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery =   " update moving set " +
                " changer_id = " + myId + ", "+
                " date_time_changed= now()," +
                " description = :description, "+
                " doc_number =" + request.getDoc_number() + "," +
                " overhead =" + request.getOverhead() + "," +                               //расходы
                " overhead_netcost_method =" + request.getOverhead_netcost_method() + "," + //Распределение затрат на себестоимость товаров. 0 - нет, 1 - по весу цены в поставке
                " is_completed = " + request.isIs_completed() + "," +
                " status_id = " + request.getStatus_id() +
                " where " +
                " id= "+request.getId() +
                " and master_id="+myMasterId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
            query.executeUpdate();
            return true;
        }catch (Exception e) {
            logger.error("Exception in method MovingRepository/updateMovingWithoutTable. stringQuery=" + stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    //сохранение таблицы товаров
    @SuppressWarnings("Duplicates")
    private boolean insertMovingProducts(MovingForm request, Long parentDockId, Long myMasterId) throws CantInsertProductRowCauseErrorException {
        Set<Long> productIds=new HashSet<>();

        if (request.getMovingProductTable()!=null && request.getMovingProductTable().size() > 0) {
            for (MovingProductForm row : request.getMovingProductTable()) {
                row.setMoving_id(parentDockId);// т.к. он может быть неизвестен при создании документа
                if (!saveMovingProductTable(row, myMasterId, request.getCompany_id())) {
                    throw new CantInsertProductRowCauseErrorException();
                }
                productIds.add(row.getProduct_id());
            }
        }
        if (!deleteMovingProductTableExcessRows(productIds.size()>0?(commonUtilites.SetOfLongToString(productIds,",","","")):"0", request.getId())){
            throw new CantInsertProductRowCauseErrorException();
        } else return true;
    }

    private Boolean saveMovingProductTable(MovingProductForm row, Long myMasterId, Long companyId) throws CantInsertProductRowCauseErrorException {
        String stringQuery;

        stringQuery =   " insert into moving_product (" +
                "master_id," +
                "company_id," +
                "product_id," +
                "moving_id," +
                "product_count," +
                "product_price," +
                "product_sumprice," +
                "product_netcost" +
                ") values ("
                + myMasterId + ","
                + companyId + ","
                + "(select id from products where id="+row.getProduct_id() +" and master_id="+myMasterId+"),"//Проверки, что никто не шалит
                + "(select id from moving where id="+row.getMoving_id() +" and master_id="+myMasterId+"),"
                + row.getProduct_count() + ","
                + row.getProduct_price() +","
                + row.getProduct_sumprice() +","
                + row.getProduct_netcost() +") " +
                "ON CONFLICT ON CONSTRAINT moving_product_uq " +// "upsert"
                " DO update set " +
                " product_id = " + "(select id from products where id="+row.getProduct_id() +" and master_id="+myMasterId+")," +
                " moving_id = "+ "(select id from moving where id="+row.getMoving_id() +" and master_id="+myMasterId+")," +
                " product_count = " + row.getProduct_count() + "," +
                " product_price = " + row.getProduct_price() + "," +
                " product_sumprice = " + row.getProduct_sumprice() + "," +
                " product_netcost = " + row.getProduct_netcost();
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method MovingRepository/saveMovingProductTable. SQL query:"+stringQuery, e);
            throw new CantInsertProductRowCauseErrorException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }

    private Boolean deleteMovingProductTableExcessRows(String productIds, Long moving_id) {
        String stringQuery;

        stringQuery =   " delete from moving_product " +
                " where moving_id=" + moving_id +
                " and product_id not in (" + productIds + ")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method MovingRepository/deleteMovingProductTableExcessRows. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }
    @SuppressWarnings("Duplicates")
    private Boolean addMovingProductHistory(MovingProductForm row, MovingForm request , Long masterId) throws CantSaveProductHistoryException, CantInsertProductRowCauseOversellException {
        String stringQuery;
        try {
            //берем последнюю запись об истории товара в данном отделении
            ProductHistoryJSON lastProductHistoryRecordFrom =  productsRepository.getLastProductHistoryRecord(row.getProduct_id(),request.getDepartment_from_id());
            ProductHistoryJSON lastProductHistoryRecordTo =    productsRepository.getLastProductHistoryRecord(row.getProduct_id(),request.getDepartment_to_id());
            //последнее количество товара
            BigDecimal lastQuantityFrom= lastProductHistoryRecordFrom.getQuantity();
            BigDecimal lastQuantityTo= lastProductHistoryRecordTo.getQuantity();
            //последняя средняя цена закупа
            BigDecimal lastAvgPurchasePriceFrom= lastProductHistoryRecordFrom.getAvg_purchase_price();
            BigDecimal lastAvgPurchasePriceTo= lastProductHistoryRecordTo.getAvg_purchase_price();
            //последняя средняя себестоимость
            BigDecimal lastAvgNetcostPriceFrom= lastProductHistoryRecordFrom.getAvg_netcost_price();
//            BigDecimal lastAvgNetcostPriceTo= lastProductHistoryRecordTo.getAvg_netcost_price();
            //средняя цена закупа = ((ПОСЛЕДНЕЕ_КОЛИЧЕСТВО*СРЕДНЯЯ_ЦЕНА_ЗАКУПА)+СУММА_ПО_НОВОМУ_ТОВАРУ) / ПОСЛЕДНЕЕ_КОЛИЧЕСТВО+КОЛИЧЕСТВО_ПО_НОВОМУ_ТОВАРУ
            //Именно поэтому нельзя допускать отрицательных остатков - если знаменатель будет = 0, то возникнет эксепшн деления на 0.
//            BigDecimal avgPurchasePriceFrom =lastProductHistoryRecordFrom.getLast_purchase_price();
            BigDecimal avgPurchasePriceTo = ((lastQuantityTo.multiply(lastAvgPurchasePriceTo)).add(row.getProduct_sumprice())).divide(lastQuantityTo.add(row.getProduct_count()),2,BigDecimal.ROUND_HALF_UP);
            //средняя себестоимость = ((ПОСЛЕДНЕЕ_КОЛИЧЕСТВО*СРЕДНЯЯ_СЕБЕСТОИМОСТЬ) + КОЛ-ВО_НОВОГО_ТОВАРА * ЕГО_СЕБЕСТОИМОСТЬ) / ПОСЛЕДНЕЕ_КОЛИЧЕСТВО + КОЛ-ВО_НОВОГО_ТОВАРА
//            BigDecimal avgNetcostPriceFrom =  lastProductHistoryRecordFrom.getAvg_netcost_price();
            BigDecimal avgNetcostPriceTo = ((lastQuantityTo.multiply(lastAvgPurchasePriceTo)).add(row.getProduct_count().multiply(row.getProduct_netcost()))).divide(lastQuantityTo.add(row.getProduct_count()),2,BigDecimal.ROUND_HALF_UP);
            //последняя закуп. цена
            BigDecimal lastPurchasePriceFrom= lastProductHistoryRecordFrom.getLast_purchase_price();
//            BigDecimal lastPurchasePriceTo= lastProductHistoryRecordTo.getLast_purchase_price();
            //все резервы товара со "склада ИЗ"
            BigDecimal allReserves = productsRepository.getProductReserves(request.getDepartment_from_id(), row.getProduct_id());

            //необходимо проверить, что списываем количество товара не более доступного количества, которое равно разнице всего количества товара на складе и резервов этого товара на данном складе.

            //  всё кол-во товара  минус ( кол-во к перемещению   +  все резервы) должно быть >= 0
            if((lastQuantityFrom.subtract(row.getProduct_count().add(allReserves))).compareTo(new BigDecimal("0")) < 0) {
                logger.error("Для перемещения с id = "+request.getId()+", номер документа "+request.getDoc_number()+", количество товара к перемещению больше доступного количества товара на складе");
                throw new CantInsertProductRowCauseOversellException();//кидаем исключение чтобы произошла отмена транзакции
            }


            stringQuery =   " insert into products_history (" +
                    " master_id," +
                    " company_id," +
                    " department_id," +
                    " doc_type_id," +
                    " doc_id," +
                    " product_id," +
                    " quantity," +//                кол-во товара на складе в результате операции
                    " change," +//1                 кол-во товара в операции
                    " avg_purchase_price," +//2     средняя цена приобретения
                    " avg_netcost_price," +//3      средняя себестоимость
                    " last_purchase_price," +//4    последняя цена приобретения
                    " last_operation_price," +//5   цена последней операции
                    " date_time_created"+
                    ") values ("+
                    masterId +","+
                    request.getCompany_id() +","+
                    request.getDepartment_from_id() + ","+
                    30 +","+
                    row.getMoving_id() + ","+
                    row.getProduct_id() + ","+
                    lastQuantityFrom.subtract(row.getProduct_count())+","+// в отделении ИЗ от этого товара вычитаем перемещаемое количество товара
                    row.getProduct_count().negate() +","+//1  negate т.к. в историю операций для "отделения ИЗ" должно записаться отрицательное кол-во товара
                    lastAvgPurchasePriceFrom +","+//2
                    lastAvgNetcostPriceFrom +","+//3
                    lastPurchasePriceFrom+","+//4     в операциях убытия (списания, продажа, перемещение из), последняя цена приобретения остается старой
                    row.getProduct_price()+","+//5//  в операциях убытия (списания, продажа, перемещение из), цена последней операции равна цене товара в данной операции
                    " now())";
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();

            stringQuery =   " insert into products_history (" +
                    " master_id," +
                    " company_id," +
                    " department_id," +
                    " doc_type_id," +
                    " doc_id," +
                    " product_id," +
                    " quantity," +//                кол-во товара на складе в результате операции
                    " change," +//1                 кол-во товара в операции
                    " avg_purchase_price," +//2     средняя цена приобретения
                    " avg_netcost_price," +//3      средняя себестоимость
                    " last_purchase_price," +//4    последняя цена приобретения
                    " last_operation_price," +//5   цена последней операции
                    " date_time_created"+
                    ") values ("+
                    masterId +","+
                    request.getCompany_id() +","+
                    request.getDepartment_to_id() + ","+
                    30 +","+
                    row.getMoving_id() + ","+
                    row.getProduct_id() + ","+
                    lastQuantityTo.add(row.getProduct_count())+","+// в отделении В к этому товару прибавляем перемещаемое количество товара
                    row.getProduct_count() +","+//1
                    avgPurchasePriceTo +","+//2     средняя цена приобретения
                    avgNetcostPriceTo +","+//3      средняя себестоимость. В данном случае (при перемещении) расходы на перемещение распределяются по себестоимости товара принимающей стороны
                    row.getProduct_price()+","+//   в операциях поступления (оприходование, приёмка, перемещение в) последняя цена приобретения last_purchase_price равна цене товара, в отличии от операций убытия (списания, продажа), где цена последнего приобретения остается старой
                    row.getProduct_price()+","+//   цена последней операции last_operation_price равна цене товара в данной операции
                    " now())";
            query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;

        } catch (CantInsertProductRowCauseOversellException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Exception in method updateMoving on updating cause oversell.", e);
            e.printStackTrace();
            throw new CantInsertProductRowCauseOversellException();// переброска эксепшена о "недостаточно товара на складе"
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method MovingRepository/addMovingProductHistory. ", e);
            throw new CantSaveProductHistoryException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }


    @SuppressWarnings("Duplicates")
    private Boolean setProductQuantity(MovingProductForm row, MovingForm request , Long masterId) throws CantSaveProductQuantityException {
        String stringQuery;
        try {
            ProductHistoryJSON lastProductHistoryRecord =  productsRepository.getLastProductHistoryRecord(row.getProduct_id(),request.getDepartment_from_id());
            BigDecimal lastQuantity= lastProductHistoryRecord.getQuantity();
            stringQuery =
                    " insert into product_quantity (" +
                            " master_id," +
                            " department_id," +
                            " product_id," +
                            " quantity" +
                            ") values ("+
                            masterId + ","+
                            request.getDepartment_from_id() + ","+
                            row.getProduct_id() + ","+
                            lastQuantity +
                            ") ON CONFLICT ON CONSTRAINT product_quantity_uq " +// "upsert"
                            " DO update set " +
                            " department_id = " + request.getDepartment_from_id() + ","+
                            " product_id = " + row.getProduct_id() + ","+
                            " master_id = "+ masterId + "," +
                            " quantity = "+ lastQuantity;
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();

            lastProductHistoryRecord =  productsRepository.getLastProductHistoryRecord(row.getProduct_id(),request.getDepartment_to_id());
            lastQuantity= lastProductHistoryRecord.getQuantity();
            stringQuery =
                    " insert into product_quantity (" +
                            " master_id," +
                            " department_id," +
                            " product_id," +
                            " quantity" +
                            ") values ("+
                            masterId + ","+
                            request.getDepartment_to_id() + ","+
                            row.getProduct_id() + ","+
                            lastQuantity +
                            ") ON CONFLICT ON CONSTRAINT product_quantity_uq " +// "upsert"
                            " DO update set " +
                            " department_id = " + request.getDepartment_to_id() + ","+
                            " product_id = " + row.getProduct_id() + ","+
                            " master_id = "+ masterId + "," +
                            " quantity = "+ lastQuantity;
            query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method MovingRepository/setProductQuantity. ", e);
            throw new CantSaveProductQuantityException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }
    @SuppressWarnings("Duplicates")
    public List<LinkedDocsJSON> getMovingLinkedDocsList(Long docId, String docName) {
        String stringQuery;
        String myTimeZone = userRepository.getUserTimeZone();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//        String tableName=(docName.equals("return")?"return":"");
        stringQuery =   " select " +
                " ap.id," +
                " to_char(ap.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI'), " +
                " ap.description," +
                " coalesce(ap.is_completed,false)," +
                " ap.doc_number" +
                " from "+docName+" ap" +
                " where ap.master_id = " + myMasterId +
                " and coalesce(ap.is_deleted,false)!=true "+
                " and ap.moving_id = " + docId;
        stringQuery = stringQuery + " order by ap.date_time_created asc ";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<LinkedDocsJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                LinkedDocsJSON doc=new LinkedDocsJSON();
                doc.setId(Long.parseLong(                       obj[0].toString()));
                doc.setDate_time_created((String)               obj[1]);
                doc.setDescription((String)                     obj[2]);
                doc.setIs_completed((Boolean)                   obj[3]);
                doc.setDoc_number(Long.parseLong(               obj[4].toString()));
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getMovingLinkedDocsList. SQL query:" + stringQuery, e);
            return null;
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Boolean deleteMoving (String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(30L,"380") && securityRepositoryJPA.isItAllMyMastersDocuments("moving",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(30L,"381") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("moving",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(30L,"382") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("moving",delNumbers))||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(30L,"383") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("moving",delNumbers)))
        {
            String stringQuery;// на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            stringQuery = "Update moving p" +
                    " set is_deleted=true, " + //удален
                    " changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now() " +//дату и время изменения
                    " where p.id in ("+delNumbers+")" +
                    " and coalesce(p.is_completed,false) !=true";
            try{
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }catch (Exception e) {
                logger.error("Exception in method deleteMoving. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Boolean undeleteMoving (String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(30L,"380") && securityRepositoryJPA.isItAllMyMastersDocuments("moving",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(30L,"381") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("moving",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(30L,"382") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("moving",delNumbers))||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(30L,"383") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("moving",delNumbers)))
        {
            String stringQuery;// на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            stringQuery = "Update moving p" +
                    " set is_deleted=false, " + //удален
                    " changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now() " +//дату и время изменения
                    " where p.id in ("+delNumbers+")";
            try{
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }catch (Exception e) {
                logger.error("Exception in method undeleteMoving. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return false;
    }
//*****************************************************************************************************************************************************
//***************************************************      UTILS      *********************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")  //генератор номера документа
    private Long generateDocNumberCode(Long company_id)
    {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "select coalesce(max(doc_number)+1,1) from moving where company_id="+company_id+" and master_id="+myMasterId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString(),10);
        }
        catch (Exception e) {
            logger.error("Exception in method generateDocNumberCode. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return 0L;
        }
    }

    @SuppressWarnings("Duplicates") // проверка на уникальность номера документа
    public Boolean isMovingNumberUnical(UniversalForm request)
    {
        Long company_id=request.getId1();
        Long code=request.getId2();
        Long product_id=request.getId3();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "" +
                "select id from moving where " +
                "company_id="+company_id+
                " and master_id="+myMasterId+
                " and doc_number="+code;
        if(product_id>0) stringQuery=stringQuery+" and id !="+product_id; // чтобы он не срабатывал сам на себя
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            if(query.getResultList().size()>0)
                return false;// код не уникальный
            else return true; // код уникальный
        }
        catch (Exception e) {
            logger.error("Exception in method isMovingNumberUnical. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return true;
        }
    }



//*****************************************************************************************************************************************************
//****************************************************   F   I   L   E   S   **************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean addFilesToMoving(UniversalForm request){
        Long movingId = request.getId1();
        //Если есть право на "Изменение по всем предприятиям" и id докмента принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта, ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(30L,"388") && securityRepositoryJPA.isItAllMyMastersDocuments("moving",movingId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(30L,"389") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("moving",movingId.toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(30L,"390") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("moving",movingId.toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(30L,"391") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("moving",movingId.toString())))
        {
            try
            {
                String stringQuery;
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select moving_id from moving_files where moving_id=" + movingId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_MovingId_FileId(movingId,fileId);
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                logger.error("Exception in method addFilesToMoving.", ex);
                ex.printStackTrace();
                return false;
            }
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_MovingId_FileId(Long movingId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into moving_files " +
                    "(moving_id,file_id) " +
                    "values " +
                    "(" + movingId + ", " + fileId +")")
                    .executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            logger.error("Exception in method manyToMany_MovingId_FileId. ", ex);
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates") //отдает информацию по файлам, прикрепленным к документу
    public List<FilesMovingJSON> getListOfMovingFiles(Long movingId) {
        if(securityRepositoryJPA.userHasPermissions_OR(30L, "384,385,386,387"))//Просмотр документов
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
            boolean needToSetParameter_MyDepthsIds = false;
            String stringQuery="select" +
                    "           f.id as id," +
                    "           f.date_time_created as date_time_created," +
                    "           f.name as name," +
                    "           f.original_name as original_name" +
                    "           from" +
                    "           moving p" +
                    "           inner join" +
                    "           moving_files pf" +
                    "           on p.id=pf.moving_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + movingId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(30L, "384")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(30L, "385")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(30L, "386")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery+" order by f.original_name asc ";

            try {
                Query query = entityManager.createNativeQuery(stringQuery);

                if (needToSetParameter_MyDepthsIds) {
                    query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());
                }

                List<Object[]> queryList = query.getResultList();

                List<FilesMovingJSON> returnList = new ArrayList<>();
                for (Object[] obj : queryList) {
                    FilesMovingJSON doc = new FilesMovingJSON();
                    doc.setId(Long.parseLong(obj[0].toString()));
                    doc.setDate_time_created((Timestamp) obj[1]);
                    doc.setName((String) obj[2]);
                    doc.setOriginal_name((String) obj[3]);
                    returnList.add(doc);
                }
                return returnList;
            }
            catch (Exception e) {
                logger.error("Exception in method getListOfMovingFiles. SQL query:" + stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteMovingFile(SearchForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(30L,"388") && securityRepositoryJPA.isItAllMyMastersDocuments("moving", String.valueOf(request.getAny_id()))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(30L,"389") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("moving",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(30L,"390") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("moving",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(30L,"391") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("moving",String.valueOf(request.getAny_id()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
//            int myCompanyId = userRepositoryJPA.getMyCompanyId();
            stringQuery  =  " delete from moving_files "+
                    " where moving_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from moving where id="+request.getAny_id()+")="+myMasterId ;
            try
            {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method ______________. SQL query:" + stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    //сохраняет настройки документа
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean saveSettingsMoving(SettingsMovingForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {

            stringQuery =
                    " insert into settings_moving (" +
                            "master_id, " +
                            "company_id, " +
                            "user_id, " +
                            "pricing_type, " +          //тип расценки (выпад. список: 1. Тип цены (priceType), 2. Ср. себестоимость (avgCostPrice) 3. Последняя закупочная цена (lastPurchasePrice) 4. Средняя закупочная цена (avgPurchasePrice))
                            "price_type_id, " +         //тип цены из справочника Типы цен
                            "change_price, " +          //наценка/скидка в цифре (например, 50)
                            "plus_minus, " +            //определят, чем является changePrice - наценкой или скидкой (принимает значения plus или minus)
                            "change_price_type, " +     //тип наценки/скидки. Принимает значения currency (валюта) или procents(проценты)
                            "hide_tenths, " +           //убирать десятые (копейки) - boolean
                            "department_from_id, " +    //отделения по умолчанию
                            "department_to_id, " +
                            "status_on_finish_id, "+    //статус документа при завершении инвентаризации
                            "auto_add"+                 // автодобавление товара из формы поиска в таблицу
                            ") values (" +
                            myMasterId + "," +
                            row.getCompanyId() + "," +
                            myId + ",'" +
                            row.getPricingType() + "'," +
                            row.getPriceTypeId() + "," +
                            row.getChangePrice() + ",'" +
                            row.getPlusMinus() + "','" +
                            row.getChangePriceType() + "'," +
                            row.getHideTenths() + "," +
                            row.getDepartmentFromId() + "," +
                            row.getDepartmentToId() + "," +
                            row.getStatusOnFinishId() + "," +
                            row.getAutoAdd() +
                            ") " +
                            "ON CONFLICT ON CONSTRAINT settings_moving_user_uq " +// "upsert"
                            " DO update set " +
                            " pricing_type = '" + row.getPricingType() + "',"+
                            " price_type_id = " + row.getPriceTypeId() + ","+
                            " change_price = " + row.getChangePrice() + ","+
                            " plus_minus = '" + row.getPlusMinus() + "',"+
                            " change_price_type = '" + row.getChangePriceType() + "',"+
                            " hide_tenths = " + row.getHideTenths() +
                            ", department_from_id = "+row.getDepartmentFromId()+
                            ", department_to_id = "+row.getDepartmentToId()+
                            ", company_id = "+row.getCompanyId()+
                            ", status_on_finish_id = "+row.getStatusOnFinishId()+
                            ", auto_add = "+row.getAutoAdd();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveSettingsMoving. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Загружает настройки документа для текущего пользователя (из-под которого пришел запрос)
    @SuppressWarnings("Duplicates")
    public SettingsMovingJSON getSettingsMoving() {

        String stringQuery;
        Long myId=userRepository.getUserId();
        stringQuery = "select " +
                "           p.department_from_id as department_from_id, " +             // id отделения
                "           p.department_to_id as department_to_id, " +                 // id отделения
                "           p.company_id as company_id, " +                             // id предприятия
                "           p.status_on_finish_id as status_on_finish_id, " +           // статус документа при завершении инвентаризации
                "           coalesce(p.auto_add,false) as auto_add, " +                 // автодобавление товара из формы поиска в таблицу
                "           p.pricing_type as pricing_type, " +                         // тип расценки (радиокнопки: 1. Тип цены (priceType), 2. Ср. себестоимость (avgCostPrice) 3. Последняя закупочная цена (lastPurchasePrice) 4. Средняя закупочная цена (avgPurchasePrice))
                "           p.price_type_id as price_type_id, " +                       // тип цены из справочника Типы цен
                "           p.change_price as change_price, " +                         // наценка/скидка в цифре (например, 50)
                "           p.plus_minus as plus_minus, " +                             // определят, что есть changePrice - наценка или скидка (plus или minus)
                "           p.change_price_type as change_price_type, " +               // тип наценки/скидки (валюта currency или проценты procents)
                "           coalesce(p.hide_tenths,false) as hide_tenths " +           // убирать десятые (копейки)
                "           from settings_moving p " +
                "           where p.user_id= " + myId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            SettingsMovingJSON movingObj=new SettingsMovingJSON();

            for(Object[] obj:queryList){
                movingObj.setDepartmentFromId(obj[0]!=null?Long.parseLong(  obj[0].toString()):null);
                movingObj.setDepartmentToId(obj[1]!=null?Long.parseLong(    obj[1].toString()):null);
                movingObj.setCompanyId(Long.parseLong(                      obj[2].toString()));
                movingObj.setStatusOnFinishId(obj[3]!=null?Long.parseLong(  obj[3].toString()):null);
                movingObj.setAutoAdd((Boolean)                              obj[4]);
                movingObj.setPricingType((String)                           obj[5]);
                movingObj.setPriceTypeId(obj[6]!=null?Long.parseLong(       obj[6].toString()):null);
                movingObj.setChangePrice((BigDecimal)                       obj[7]);
                movingObj.setPlusMinus((String)                             obj[8]);
                movingObj.setChangePriceType((String)                       obj[9]);
                movingObj.setHideTenths((Boolean)                           obj[10]);
            }
            return movingObj;
        }
        catch (Exception e) {
            logger.error("Exception in method getSettingsMoving. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw e;
        }
    }

}