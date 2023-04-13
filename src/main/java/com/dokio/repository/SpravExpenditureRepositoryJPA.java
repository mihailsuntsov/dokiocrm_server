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

import com.dokio.message.request.Sprav.SpravExpenditureForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.Sprav.SpravExpenditureJSON;
import com.dokio.model.Companies;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.persistence.*;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class SpravExpenditureRepositoryJPA {

    Logger logger = Logger.getLogger("SpravExpenditureRepositoryJPA");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory emf;
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
    UserDetailsServiceImpl userService;
    @Autowired
    CommonUtilites commonUtilites;

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("name","type","cagent","company","creator","date_time_created_sort","is_completed","is_default")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    @Transactional
    @SuppressWarnings("Duplicates")
    public List<SpravExpenditureJSON> getExpenditureTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int documentId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(40L, "502,503"))//"Статусы документов" (см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
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
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.name as name, " +
                    "           p.type as type, " +//тип расхода: return (возврат),  purchases (закупки товаров), taxes (налоги и сборы), moving (перемещение меж. своими счетами или кассами), other_opex (другие операционные)
                    "           p.is_completed as is_completed, " +
                    "           coalesce(p.is_default, false) as is_default, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort " +
                    "           from sprav_expenditure_items p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(40L, "502")) //Если нет прав на "Просмотр "Статусы документов" по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " upper(p.name)   like upper(CONCAT('%',:sg,'%'))"+ ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            if (VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) && VALID_COLUMNS_FOR_ASC.contains(sortAsc)) {
                stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            } else {
                throw new IllegalArgumentException("Invalid query parameters");
            }

            try{

                Query query = entityManager.createNativeQuery(stringQuery)
                        .setFirstResult(offsetreal)
                        .setMaxResults(result);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                List<Object[]> queryList = query.getResultList();
                List<SpravExpenditureJSON> returnList = new ArrayList<>();
                for (Object[] obj : queryList) {
                    SpravExpenditureJSON doc = new SpravExpenditureJSON();

                    doc.setId(Long.parseLong(obj[0].toString()));
                    doc.setMaster((String) obj[1]);
                    doc.setCreator((String) obj[2]);
                    doc.setChanger((String) obj[3]);
                    doc.setMaster_id(Long.parseLong(obj[4].toString()));
                    doc.setCreator_id(Long.parseLong(obj[5].toString()));
                    doc.setChanger_id(obj[6] != null ? Long.parseLong(obj[6].toString()) : null);
                    doc.setCompany_id(Long.parseLong(obj[7].toString()));
                    doc.setCompany((String) obj[8]);
                    doc.setDate_time_created((String) obj[9]);
                    doc.setDate_time_changed((String) obj[10]);
                    doc.setName((String) obj[11]);
                    doc.setType((String) obj[12]);
                    doc.setIs_completed((Boolean) obj[13]);
                    doc.setIs_default((Boolean) obj[14]);
                    returnList.add(doc);
                }
                return returnList;

            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getExpenditureTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public int getExpenditureSize(String searchString, int companyId, int documentId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(40L, "502,503"))//"Статусы документов" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery = "select  p.id as id " +
                    "           from sprav_expenditure_items p " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(40L, "502")) //Если нет прав на "Меню - таблица - "Статусы документов" по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " upper(p.name)   like upper(CONCAT('%',:sg,'%'))"+ ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            Query query = entityManager.createNativeQuery(stringQuery);

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}

            return query.getResultList().size();
        } else return 0;
    }

//*****************************************************************************************************************************************************
//****************************************************   C  R  U  D   *********************************************************************************
//*****************************************************************************************************************************************************

    @Transactional
    @SuppressWarnings("Duplicates")
    public SpravExpenditureJSON getExpenditureValues(Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(40L, "502,503"))//"Статусы документов" (см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.name as name, " +
                    "           p.type as type, " +//тип расхода: return (возврат),  purchases (закупки товаров), taxes (налоги и сборы), moving (перемещение меж. своими счетами или кассами), other_opex (другие операционные)
                    "           p.is_completed as is_completed " +
                    "           from sprav_expenditure_items p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(40L, "502")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (503)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            SpravExpenditureJSON doc = new SpravExpenditureJSON();

            for (Object[] obj : queryList) {

                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setMaster((String) obj[1]);
                doc.setCreator((String) obj[2]);
                doc.setChanger((String) obj[3]);
                doc.setMaster_id(Long.parseLong(obj[4].toString()));
                doc.setCreator_id(Long.parseLong(obj[5].toString()));
                doc.setChanger_id(obj[6] != null ? Long.parseLong(obj[6].toString()) : null);
                doc.setCompany_id(Long.parseLong(obj[7].toString()));
                doc.setCompany((String) obj[8]);
                doc.setDate_time_created((String) obj[9]);
                doc.setDate_time_changed((String) obj[10]);
                doc.setName((String) obj[11]);
                doc.setType((String) obj[12]);
                doc.setIs_completed((Boolean) obj[13]);
            }
            return doc;
        } else return null;

    }


    @SuppressWarnings("Duplicates")
    @Transactional
    public Integer updateExpenditure(SpravExpenditureForm request) {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(40L,"504") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_expenditure_items",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(40L,"505") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_expenditure_items",request.getId().toString())))
        {
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            String stringQuery;
            stringQuery =   " update sprav_expenditure_items set " +
                    " changer_id = " + myId + ", "+
                    " date_time_changed= now()," +
                    " name = :name, " +
                    " type = :type " +
                    " where " +
                    " id= "+request.getId()+
                    " and master_id="+myMasterId;
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                query.setParameter("type",request.getType());
                query.executeUpdate();

                return 1;

            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updateExpenditure. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; //недостаточно прав

    }

    // Возвращаем id в случае успешного создания
    // Возвращаем null в случае ошибки
    // Возвращаем -1 в случае отсутствия прав
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Long insertExpenditure(SpravExpenditureForm request) {
        EntityManager emgr = emf.createEntityManager();
        Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
        Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
        Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        if ((   //если есть право на создание по всем предприятиям, или
                (securityRepositoryJPA.userHasPermissions_OR(40L, "498")) ||
                        //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                        (securityRepositoryJPA.userHasPermissions_OR(40L, "499") && myCompanyId.equals(request.getCompany_id()))) &&
                //создается документ для предприятия моего владельца (т.е. под юрисдикцией главного аккаунта)
                DocumentMasterId.equals(myMasterId))
        {
            String stringQuery;
            Long myId = userRepository.getUserId();

            String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            stringQuery = "insert into sprav_expenditure_items (" +
                    " master_id," + //мастер-аккаунт
                    " creator_id," + //создатель
                    " company_id," + //предприятие, для которого создается документ
                    " date_time_created," + //дата и время создания
                    " name," +//наименование
                    " is_completed, " +
                    " type" +// тип
                    ") values ("+
                    myMasterId + ", "+//мастер-аккаунт
                    myId + ", "+ //создатель
                    request.getCompany_id() + ", "+//предприятие, для которого создается документ
                    " to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                    " :name, " +
                    " false, " +
                    " :type)";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                query.setParameter("type",request.getType());
                query.executeUpdate();
                stringQuery="select id from sprav_expenditure_items where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                Query query2 = entityManager.createNativeQuery(stringQuery);

                return Long.valueOf(query2.getSingleResult().toString());
            } catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertExpenditure on inserting into sprav_expenditure_items. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else {
            return -1L;
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer deleteExpenditure(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(40L, "500") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_expenditure_items", delNumbers)) ||
            //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
            (securityRepositoryJPA.userHasPermissions_OR(40L, "501") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_expenditure_items", delNumbers))) {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update sprav_expenditure_items p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=true " +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method deleteExpenditure. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }

        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer undeleteExpenditure(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(40L, "500") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_expenditure_items", delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(40L, "501") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_expenditure_items", delNumbers))) {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update sprav_expenditure_items p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method undeleteExpenditure. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    // позволяет поределить, какого типа расход (moving, taxes, purchases, other_opex и др) выбран по его id
    public String getExpTypeByExpId(Long expId) throws Exception {
        String stringQuery =
                " select type from sprav_expenditure_items where " +
                        " id= " + expId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return (String) query.getSingleResult();
        } catch (NoResultException nre) {
            return "";
        } catch (Exception e) {
            logger.error("Exception in method getExpTypeByExpId. SQL: " + stringQuery, e);
            e.printStackTrace();
            throw new Exception();// чтобы отменилась транзакция в вызвавшем его документе
        }
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Integer setDefaultExpenditure(UniversalForm request) {// id : предприятие, id3 : id расхода
        EntityManager emgr = emf.createEntityManager();
        Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
        Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getId());//предприятие для создаваемого документа
        Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        if ((   //если есть право на редактирование по всем предприятиям, или
                (securityRepositoryJPA.userHasPermissions_OR(40L, "504")) ||
                //если есть право на редактирование по всем отделениям своего предприятия, и предприятие документа своё, и
                (securityRepositoryJPA.userHasPermissions_OR(40L, "505") && myCompanyId.equals(request.getId()))) &&
                //редактируется документ предприятия моего владельца (т.е. под юрисдикцией главного аккаунта)
                DocumentMasterId.equals(myMasterId))
        {
            try
            {
                String stringQuery;
                stringQuery =   " update sprav_expenditure_items set is_default=(" +
                        " case when (id="+request.getId3()+") then true else false end) " +
                        " where " +
                        " company_id= "+request.getId();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    public Long getDefaultExpenditure(Long companyId) {
        String stringQuery =
                " select id from sprav_expenditure_items where company_id= " + companyId + " and is_default = true";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return ((BigInteger)query.getSingleResult()).longValue();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            logger.error("Exception in method getDefaultExpenditure. SQL: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    // inserting base set of expenditures for new user
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Boolean insertExpendituresFast(Long mId, Long uId, Long cId) {
        String stringQuery;
        String t = new Timestamp(System.currentTimeMillis()).toString();
        Map<String, String> map = commonUtilites.translateForUser(mId, new String[]{
                "'exp_rent'","'exp_return'","'exp_salary'","'exp_banking_srvcs'","'exp_taxes'","'exp_pay_goods_srvcs'","'exp_pay_wh_company'","'payroll_taxes'","'accounting_srvcs'"});
        stringQuery = "insert into sprav_expenditure_items (master_id,creator_id,company_id,date_time_created,name,type,is_deleted,is_completed,is_default) values "+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("exp_rent")+"','other_opex',false,false,false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("exp_return")+"','return',false,false,false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("exp_salary")+"','other_opex',false,false,false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("exp_banking_srvcs")+"','other_opex',false,false,false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("exp_taxes")+"','taxes',false,false,false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("exp_pay_goods_srvcs")+"','purchases',false,false,true),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("payroll_taxes")+"','other_opex',false,false,false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("accounting_srvcs")+"','other_opex',false,false,false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("exp_pay_wh_company")+"','moving',false,false,false);";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method insertExpendituresFast. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
}