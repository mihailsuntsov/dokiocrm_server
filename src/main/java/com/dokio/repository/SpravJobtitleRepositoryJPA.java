/*
        DokioCRM - server part. Sales, finance and warehouse management system
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

import com.dokio.message.request.Sprav.SpravJobtitleForm;
import com.dokio.message.request.additional.JobtitleProductsForm;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.Sprav.SpravJobtitleJSON;
import com.dokio.message.response.additional.JobtitleListJSON;
import com.dokio.message.response.additional.JobtitleProductsJSON;
import com.dokio.model.Companies;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class SpravJobtitleRepositoryJPA {

    Logger logger = Logger.getLogger("SpravJobtitleRepositoryJPA");

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
    @Autowired
    DepartmentRepositoryJPA departmentRepository;
    @Autowired
    private CommonUtilites commonUtilites;

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("name","description","cagent","company","creator","date_time_created_sort")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    public List<SpravJobtitleJSON> getJobtitleTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, long companyId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(57L, "697,698"))//(см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery = "select  p.id as id, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.name as name, " +
                    "           p.description as description, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort  " +
                    "           from sprav_jobtitles p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;


            if (!securityRepositoryJPA.userHasPermissions_OR(57L, "697")) //Если нет прав на "Просмотр по всем предприятиям"
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
                List<SpravJobtitleJSON> returnList = new ArrayList<>();
                for (Object[] obj : queryList) {
                    SpravJobtitleJSON doc = new SpravJobtitleJSON();

                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setCreator((String)                                 obj[1]);
                    doc.setChanger((String)                                 obj[2]);
                    doc.setCreator_id(obj[3] != null ? Long.parseLong(      obj[3].toString()) : null);
                    doc.setChanger_id(obj[4] != null ? Long.parseLong(      obj[4].toString()) : null);
                    doc.setCompany_id(Long.parseLong(                       obj[5].toString()));
                    doc.setCompany((String)                                 obj[6]);
                    doc.setDate_time_created((String)                       obj[7]);
                    doc.setDate_time_changed((String)                       obj[8]);
                    doc.setName((String)                                    obj[9]);
                    doc.setDescription((String)                             obj[10]);
                    returnList.add(doc);
                }
                return returnList;

            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getJobtitleTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public int getJobtitleSize(String searchString, long companyId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(57L, "697,698"))// (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery = "select  p.id as id " +
                    "           from sprav_jobtitles p " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(57L, "697")) //Если нет прав на "Меню - таблица -  по всем предприятиям"
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
    public SpravJobtitleJSON getJobtitleValues(Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(57L, "697,698"))// (см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long myMasterId = userRepositoryJPA.getMyMasterId();

            stringQuery = "select  p.id as id, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.name as name, " +
                    "           p.description as description " +
                    "           from sprav_jobtitles p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(57L, "697")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (698)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            SpravJobtitleJSON doc = new SpravJobtitleJSON();

            for (Object[] obj : queryList) {

                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setCreator((String) obj[1]);
                doc.setChanger((String) obj[2]);
                doc.setCreator_id(obj[3] != null ? Long.parseLong(obj[3].toString()) : null);
                doc.setChanger_id(obj[4] != null ? Long.parseLong(obj[4].toString()) : null);
                doc.setCompany_id(Long.parseLong(obj[5].toString()));
                doc.setCompany((String) obj[6]);
                doc.setDate_time_created((String) obj[7]);
                doc.setDate_time_changed((String) obj[8]);
                doc.setName((String) obj[9]);
                doc.setDescription((String) obj[10]);
            }
            return doc;
        } else return null;

    }


    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public Integer updateJobtitle(SpravJobtitleForm request) {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(57L,"699") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_jobtitles",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(57L,"700") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_jobtitles",request.getId().toString())))
        {
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getMyMasterId();

            String stringQuery;
            stringQuery =
                    " update sprav_jobtitles set " +
                            " changer_id = " + myId + ", "+
                            " date_time_changed= now()," +
                            " name = :name, " +
                            " description = :description " +
                            " where " +
                            " id= "+request.getId()+
                            " and master_id="+myMasterId;
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                query.setParameter("description",request.getDescription());
                query.executeUpdate();
                return 1;

            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updateJobtitle. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; //недостаточно прав

    }

    // Возвращаем id в случае успешного создания
    // Возвращаем null в случае ошибки
    // Возвращаем -1 в случае отсутствия прав
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class,Exception.class})
    public Long insertJobtitle(SpravJobtitleForm request) {
        EntityManager emgr = emf.createEntityManager();
        Long myCompanyId=userRepositoryJPA.getMyCompanyId_();
        Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
        Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
        Long myMasterId = userRepositoryJPA.getMyMasterId();

        if ((   //если есть право на создание по всем предприятиям, или
                (securityRepositoryJPA.userHasPermissions_OR(57L, "693")) ||
                        //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                        (securityRepositoryJPA.userHasPermissions_OR(57L, "694") && myCompanyId.equals(request.getCompany_id()))) &&
                //создается документ для предприятия моего владельца (т.е. под юрисдикцией главного аккаунта)
                DocumentMasterId.equals(myMasterId))
        {
            String stringQuery;
            Long myId = userRepository.getUserId();

            String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            stringQuery = "insert into sprav_jobtitles (" +
                    " master_id," + //мастер-аккаунт
                    " creator_id," + //создатель
                    " company_id," + //предприятие, для которого создается документ
                    " date_time_created," + //дата и время создания
                    " name," +//наименование
                    " description" +// тип
                    ") values ("+
                    myMasterId + ", "+//мастер-аккаунт
                    myId + ", "+ //создатель
                    request.getCompany_id() + ", "+//предприятие, для которого создается документ
                    " to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                    " :name, " +
                    " :description)";
            try{
                cu.idBelongsMyMaster("companies", request.getCompany_id(), myMasterId);

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                query.setParameter("description",request.getDescription());
                query.executeUpdate();
                stringQuery="select id from sprav_jobtitles where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                Query query2 = entityManager.createNativeQuery(stringQuery);
                Long newDocId = Long.valueOf(query2.getSingleResult().toString());
                return newDocId;
            } catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertJobtitle on inserting into sprav_jobtitles. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else {
            return -1L;
        }
    }

    @Transactional
    public Integer deleteJobtitle(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(57L, "695") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_jobtitles", delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(57L, "696") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_jobtitles", delNumbers))) {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update sprav_jobtitles p" +
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
                logger.error("Exception in method deleteJobtitle. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }

        } else return -1;
    }

    @Transactional
    public Integer undeleteJobtitle(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(57L, "695") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_jobtitles", delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(57L, "696") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_jobtitles", delNumbers))) {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update sprav_jobtitles p" +
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
                logger.error("Exception in method undeleteJobtitle. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }

        } else return -1;
    }

    public List<JobtitleListJSON> getJobtitleList(long companyId) {
//        if (securityRepositoryJPA.userHasPermissions_OR(57L, "697,698"))//(см. файл Permissions Id)
//        {
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getMyMasterId();
        stringQuery = "select   p.id as id, " +
                "               p.name as name, " +
                "               p.description as description" +
                "               from sprav_jobtitles p " +
                "               where  p.master_id=" + myMasterId +
                "               and p.company_id=" + companyId +
                "               and coalesce(p.is_deleted,false) = false";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<JobtitleListJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                JobtitleListJSON doc = new JobtitleListJSON();
                doc.setJobtitle_id(Long.parseLong(                      obj[0].toString()));
                doc.setName((String)                                    obj[1]);
                doc.setDescription((String)                             obj[2]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getJobtitleList. SQL query:" + stringQuery, e);
            return null;
        }
    }

    // inserting base set of job titles for new user
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Long insertJobtitleFast(Long mId, Long masterId, Long cId) {
        String stringQuery;
        String t = new Timestamp(System.currentTimeMillis()).toString();
        Map<String, String> map = cu.translateForUser(mId, new String[]{"'provides_services'"});
        stringQuery = "insert into sprav_jobtitles ( master_id,creator_id,company_id,date_time_created,name,is_deleted) values "+
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("provides_services")+"',false)";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            stringQuery="select id from sprav_jobtitles where date_time_created=(to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+mId;
            Query query2 = entityManager.createNativeQuery(stringQuery);
            return Long.valueOf(query2.getSingleResult().toString());
        } catch (Exception e) {
            logger.error("Exception in method insertJobtitleFast. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public void createJobtitlesFast(Long mId, Long uId, Long cId) throws Exception {
        String stringQuery;
        String t = new Timestamp(System.currentTimeMillis()).toString();
        Map<String, String> map = commonUtilites.translateForUser(mId, new String[]{
                "'jt_administrator'","'jt_manager'","'jt_manicurist'","'jt_hairdresser-stylist'","'jt_eyebrow master'",
                "'jt_cosmetologist'","'jt_visagiste'","'jt_masseur'","'jt_instructor'","'jt_trainer'",
                "'jt_dentist'","'jt_orthodontist'","'jt_surgeon'","'jt_therapist'","'jt_neurologist'",
                "'jt_cardiologist'","'jt_usd_doctor'","'jt_family_doctor'","'jt_photographer'","'jt_teacher'"});
        stringQuery = "insert into sprav_jobtitles (master_id,creator_id,company_id,date_time_created,name,description,is_deleted) values "+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_administrator")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_manager")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_manicurist")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_hairdresser-stylist")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_eyebrow master")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_cosmetologist")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_visagiste")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_masseur")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_instructor")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_trainer")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_dentist")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_orthodontist")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_surgeon")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_therapist")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_neurologist")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_cardiologist")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_usd_doctor")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_family_doctor")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_photographer")+"','',false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("jt_teacher")+"','',false);";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method createJobtitlesFast. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

}