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

import com.dokio.message.request.Sprav.SpravTaxesForm;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.Sprav.SpravTaxesJSON;
import com.dokio.message.response.Sprav.SpravTaxesListJSON;
import com.dokio.model.Companies;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class SpravTaxesRepository {

    Logger logger = Logger.getLogger("SpravTaxesRepository");

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
    CommonUtilites cu;

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("name","output_order","description","company","creator","date_time_created_sort")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    @Transactional
    public List<SpravTaxesJSON> getTaxesTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, Long companyId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(50L, "640,641"))// (см. файл Permissions Id)
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
                    "           p.description as description, " +
                    "           p.value as value, " +
                    "           p.multiplier as multiplier, " +
                    "           p.is_active as is_active, " +
                    "           p.is_deleted as is_deleted, " +
                    "           p.name_api_atol as name_api_atol, " +
                    "           p.output_order as output_order, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort  " +
                    "           from sprav_taxes p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(50L, "640")) //Если нет прав на "Меню - таблица - "Статусы документов" по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper(CONCAT('%',:sg,'%')) or " +
                        "upper(p.description) like upper(CONCAT('%',:sg,'%'))" + ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            if (VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) && VALID_COLUMNS_FOR_ASC.contains(sortAsc)) {
                stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            } else {
                throw new IllegalArgumentException("Invalid query parameters");
            }

            Query query = entityManager.createNativeQuery(stringQuery)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}

            List<Object[]> queryList = query.getResultList();
            List<SpravTaxesJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                SpravTaxesJSON doc = new SpravTaxesJSON();

                doc.setId(Long.parseLong(                           obj[0].toString()));
                doc.setMaster((String)                              obj[1]);
                doc.setCreator((String)                             obj[2]);
                doc.setChanger((String)                             obj[3]);
                doc.setMaster_id(Long.parseLong(                    obj[4].toString()));
                doc.setCreator_id(Long.parseLong(                   obj[5].toString()));
                doc.setChanger_id(obj[6] != null ? Long.parseLong(  obj[6].toString()) : null);
                doc.setCompany_id(Long.parseLong(                   obj[7].toString()));
                doc.setCompany((String)                             obj[8]);
                doc.setDate_time_created((String)                   obj[9]);
                doc.setDate_time_changed((String)                   obj[10]);
                doc.setName((String)                                obj[11]);
                doc.setDescription((String)                         obj[12]);
                doc.setValue((BigDecimal)                           obj[13]);
                doc.setMultiplier((BigDecimal)                      obj[14]);
                doc.setIs_active((Boolean)                          obj[15]);
                doc.setIs_deleted((Boolean)                         obj[16]);
                doc.setName_api_atol((String)                       obj[17]);
                doc.setOutput_order((Integer)                       obj[18]);
                returnList.add(doc);
            }
            return returnList;
        } else return null;
    }

    @Transactional
    public int getTaxesSize(String searchString, Long companyId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(50L, "640,641"))//"Статусы документов" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery = "select  p.id as id " +
                    "           from sprav_taxes p " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(50L, "640")) //Если нет прав на "Меню - таблица - "Статусы документов" по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper(CONCAT('%',:sg,'%')) or " +
                        "upper(p.description) like upper(CONCAT('%',:sg,'%'))" + ")";
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
    public SpravTaxesJSON getTaxesValues(Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(50L,"640,641"))//"Статусы документов" (см. файл Permissions Id)
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
                    "           p.description as description, " +
                    "           p.value as value, " +
                    "           p.multiplier as multiplier, " +
                    "           p.is_active as is_active, " +
                    "           p.is_deleted as is_deleted, " +
                    "           p.name_api_atol as name_api_atol, " +
                    "           p.output_order as output_order " +
                    "           from sprav_taxes p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(50L, "640")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (641)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                List<Object[]> queryList = query.getResultList();

                SpravTaxesJSON doc = new SpravTaxesJSON();

                for (Object[] obj : queryList) {

                    doc.setId(Long.parseLong(                           obj[0].toString()));
                    doc.setMaster((String)                              obj[1]);
                    doc.setCreator((String)                             obj[2]);
                    doc.setChanger((String)                             obj[3]);
                    doc.setMaster_id(Long.parseLong(                    obj[4].toString()));
                    doc.setCreator_id(Long.parseLong(                   obj[5].toString()));
                    doc.setChanger_id(obj[6] != null ? Long.parseLong(  obj[6].toString()) : null);
                    doc.setCompany_id(Long.parseLong(                   obj[7].toString()));
                    doc.setCompany((String)                             obj[8]);
                    doc.setDate_time_created((String)                   obj[9]);
                    doc.setDate_time_changed((String)                   obj[10]);
                    doc.setName((String)                                obj[11]);
                    doc.setDescription((String)                         obj[12]);
                    doc.setValue((BigDecimal)                           obj[13]);
                    doc.setMultiplier((BigDecimal)                      obj[14]);
                    doc.setIs_active((Boolean)                          obj[15]);
                    doc.setIs_deleted((Boolean)                         obj[16]);
                    doc.setName_api_atol((String)                       obj[17]);
                    doc.setOutput_order((Integer)                       obj[18]);
                }
                return doc;
            } catch (Exception e) {
                logger.error("Exception in method getTaxesValues on selecting from sprav_taxes. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    @Transactional
    public Integer updateTaxes(SpravTaxesForm request) {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(50L,"642") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_taxes",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(50L,"643") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_taxes",request.getId().toString())))
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            Long myId=userRepository.getUserId();
            String stringQuery;
            stringQuery =   " update " +
                    " sprav_taxes " +
                    " set " +
                    " changer_id = " + myId + ", "+
                    " date_time_changed= now()," +
                    " description = :description, " +
                    " name = :name, " +
                    " value = " + request.getValue() + ", "+
                    " multiplier = " + request.getMultiplier() + ", "+
                    " is_active = " + request.isIs_active() + ", "+
                    " name_api_atol = :name_api_atol " +
                    " where " +
                    " master_id = " + myMasterId +
                    " and id= "+request.getId();
            try
            {
                try
                {//сохранение порядка вывода налогов
                    if (request.getTaxesIdsInOrderOfList().size() > 1) {
                        int c = 0;
                        for (Long field : request.getTaxesIdsInOrderOfList()) {
                            c++;
                            if (!saveChangesStatusesOrder(field, c, myMasterId)) {
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Exception in method insertTaxes on trying to save taxes output order:", e);
                    e.printStackTrace();
                    return null;
                }
                //сохранение полей документа
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name", (request.getName() == null ? "" : request.getName()));
                query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
                query.setParameter("name_api_atol", (request.getName_api_atol() == null ? "" : request.getName_api_atol()));
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method insertTaxes on updating sprav_taxes. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @Transactional
    public Long insertTaxes(SpravTaxesForm request) {
        EntityManager emgr = emf.createEntityManager();
        Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
        Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
        Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        if ((   //если есть право на создание по всем предприятиям, или
                (securityRepositoryJPA.userHasPermissions_OR(50L, "636")) ||
                //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                (securityRepositoryJPA.userHasPermissions_OR(50L, "637") && myCompanyId.equals(request.getCompany_id()))) &&
                //создается документ для предприятия моего владельца (т.е. под юрисдикцией главного аккаунта)
                DocumentMasterId.equals(myMasterId))
        {
            Long myId = userRepository.getUserId();
            String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            String stringQuery =
                "insert into sprav_taxes (" +
                " master_id," +             // мастер-аккаунт
                " creator_id," +            // создатель
                " company_id," +            // предприятие, для которого создается документ
                " date_time_created," +     // дата и время создания
                " name," +                  // наименование налога
                " description," +           // доп. информация по налогу
                " value," +                 // значение налога в процентах
                " multiplier," +            // множитель налога
                " is_active," +             // налог активен в данный момент
                " name_api_atol," +         // наименование налога в API Атол (актуально только для России)
                " is_deleted," +            // налог удалён
                " output_order" +           // порядок вывода
                ") values ("+
                myMasterId + ", "+//мастер-аккаунт
                myId + ", "+ //создатель
                request.getCompany_id() + ", "+//предприятие, для которого создается документ
                "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                ":name,"+
                ":description,"+
                request.getValue() + ", " +
                request.getMultiplier() + ", " +
                request.isIs_active() + ", " +
                ":name_api_atol" + ", " +
                false + ", " +
                getNextOutputOrder(request.getCompany_id()) +
                ")";// уникальный идентификатор документа

            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                query.setParameter("description",request.getDescription());
                query.setParameter("name_api_atol",request.getName_api_atol());
                query.executeUpdate();
                stringQuery="select id from sprav_taxes where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                Query query2 = entityManager.createNativeQuery(stringQuery);
                return Long.valueOf(query2.getSingleResult().toString());
            } catch (Exception e) {
                logger.error("Exception in method insertTaxes on inserting into sprav_taxes. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1L;
    }

    @Transactional
    public Integer deleteTaxes(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(50L, "638") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_taxes", delNumbers)) ||
            //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
            (securityRepositoryJPA.userHasPermissions_OR(50L, "639") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_taxes", delNumbers)))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery = "update sprav_taxes p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=true " +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method deleteTaxes on updating sprav_taxes. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @Transactional
    public Integer undeleteTaxes(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(50L, "638") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_taxes", delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(50L, "639") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_taxes", delNumbers)))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery =
                    "update sprav_taxes p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method undeleteTaxes on updating sprav_taxes. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }
//*****************************************************************************************************************************************************
//*******************************************************************  U T I L S **********************************************************************
//*****************************************************************************************************************************************************

    private Integer getNextOutputOrder(Long companyId) {
        String stringQuery = "select coalesce(max(output_order)+1,1) from sprav_taxes where company_id =  " + companyId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            int output_order;
            output_order= (int) query.getSingleResult();
            return output_order;
        } catch (Exception e) {
            logger.error("Exception in method getNextOutputOrder. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    private boolean saveChangesStatusesOrder(Long statusId, int order, Long masterId) {
        String stringQuery;

            stringQuery =   " update sprav_taxes set " +
                            " output_order = " + order +
                            " where id = " + statusId + " and master_id = " + masterId ;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method saveChangesStatusesOrder. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    @Transactional//отдает список налогов по id предприятия
    @SuppressWarnings("Duplicates")
    public List<SpravTaxesListJSON> getTaxesList(Long companyId) {

        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select" +
                "           p.id as id, " +
                "           p.name as name, " +
                "           p.description as description,  " +
                "           p.value as value, " +
                "           p.multiplier as multiplier, " +
                "           p.name_api_atol as name_api_atol " +
                "           from sprav_taxes p " +
                "           where  p.master_id=" + myMasterId +
                "           and p.company_id=" + companyId +
                "           and coalesce(p.is_deleted,false)=false" +
                "           and coalesce(p.is_active,false)=true" +
                "           order by p.output_order asc";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();
            List<SpravTaxesListJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                SpravTaxesListJSON doc = new SpravTaxesListJSON();

                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setName((String) obj[1]);
                doc.setDescription((String) obj[2]);
                doc.setValue((BigDecimal) obj[3]);
                doc.setMultiplier((BigDecimal) obj[4]);
                doc.setName_api_atol((String) obj[5]);
                doc.setCalculated(false);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            logger.error("Exception in method getTaxesList. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    // inserting base set taxes of new user
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Boolean insertTaxesFast(Long mId, Long uId, Long cId) {
        String stringQuery;
        String t = new Timestamp(System.currentTimeMillis()).toString();
        Map<String, String> map = cu.translateForUser(mId, new String[]{"'tax_no_tax'","'tax_tax'"});
        stringQuery = "insert into sprav_taxes ( master_id,creator_id,company_id,date_time_created,name,is_active,value,multiplier,output_order,is_deleted) values "+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("tax_no_tax")+"',true,0, 1,  1,false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("tax_tax")+" 0%', true,0, 1,  2,false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("tax_tax")+" 10%', true,10,1.1,3,false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("tax_tax")+" 20%', true,20,1.2,4,false)";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method insertTaxesFast. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
}