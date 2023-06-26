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

import com.dokio.message.request.TypePricesForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.PriceTypesListJSON;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.TypePricesTableJSON;
import com.dokio.model.Sprav.SpravSysPriceRole;
import com.dokio.model.Sprav.SpravTypePrices;
import com.dokio.model.Sprav.SpravTypePricesJSON;
import com.dokio.model.User;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class TypePricesRepositoryJPA {


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
    UserRepository userService;
    @Autowired
    private CommonUtilites commonUtilites;

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("p.name","description","company","creator","date_time_created_sort")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    Logger logger = Logger.getLogger("TypePricesRepositoryJPA");

    @SuppressWarnings("Duplicates")
    public List<TypePricesTableJSON> getTypePricesTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(9L, "95,96"))//типы цен (см. файл Permissions Id)
             {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные

            stringQuery = "select " +
                    "           p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           pr.name as pricerole, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           p.pricerole_id as pricerole_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.name as name, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_default, false) as is_default " +
                    "           from sprav_type_prices p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_sys_pricerole pr ON p.pricerole_id=pr.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(9L, "95")) //Если нет прав на "Меню - таблица - Типы цен по всем предприятиям"
            {
                //остается только на своё предприятие (96)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and upper(p.name) like upper(CONCAT('%',:sg,'%'))";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            if (VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) && VALID_COLUMNS_FOR_ASC.contains(sortAsc)) {
                stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            } else {
                throw new IllegalArgumentException("Invalid query parameters");
            }

            Query query = entityManager.createNativeQuery(stringQuery, TypePricesTableJSON.class)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}

            return query.getResultList();
        } else return null;
    }
    @SuppressWarnings("Duplicates")
    public int getTypePricesSize(String searchString, int companyId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(9L, "95,96"))//типы цен (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные

            stringQuery = "select " +
                    "           p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           p.name as name, " +
                    "           p.description as description " +
                    "           from sprav_type_prices p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;



            if (!securityRepositoryJPA.userHasPermissions_OR(9L, "95")) //Если нет прав на "Меню - таблица - Типы цен по всем предприятиям"
            {
                //остается только на своё предприятие (96)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and upper(p.name) like upper(CONCAT('%',:sg,'%'))";
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

    @SuppressWarnings("Duplicates")
    public SpravTypePricesJSON getTypePricesValuesById (int id) {
        if (securityRepositoryJPA.userHasPermissions_OR(9L, "95,96"))//Типы цен: см. _Permissions Id.txt
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select " +
                    "           p.id as id, " +
                    "           u.name as master, " +
                    "           p.name as name, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           pr.name as pricerole, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           p.pricerole_id as pricerole_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.description as description " +
                    "           from sprav_type_prices p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_sys_pricerole pr ON p.pricerole_id=pr.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(9L, "95")) //Если нет прав на просм или редакт. по всем предприятиям
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            Query query = entityManager.createNativeQuery(stringQuery, SpravTypePricesJSON.class);

            try {// если ничего не найдено, то javax.persistence.NoResultException: No entity found for query
                SpravTypePricesJSON response = (SpravTypePricesJSON) query.getSingleResult();
                return response;}
            catch(NoResultException nre){return null;}
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public Integer updateTypePrices(TypePricesForm request) {
        boolean perm_AllCompaniesUpdate=securityRepositoryJPA.userHasPermissions_OR(9L, "97"); // Типы цен:"Редактирование документов по всем предприятиям" (в пределах родительского аккаунта)
        boolean perm_MyCompanyUpdate=securityRepositoryJPA.userHasPermissions_OR(9L, "98"); // Типы цен:"Редактирование документов своего предприятия"

        boolean itIsDocumentOfMyMasters=securityRepositoryJPA.isItMyMastersTypePrices(Long.valueOf(request.getId()));//документ под юрисдикцией главного аккаунта
        boolean itIsDocumentOfMyCompany=userRepositoryJPA.getMyCompanyId()==Integer.parseInt(request.getCompany_id());//сохраняется документ моего предприятия

        if
        (
                (perm_AllCompaniesUpdate ||                                     //если есть права изменять доки всех предприятий
                        (itIsDocumentOfMyCompany && perm_MyCompanyUpdate)            //или это мое предприятие и есть права изменять доки своего предприятия
                        )
                        && itIsDocumentOfMyMasters                                      //+документ под юрисдикцией главного (родительского) аккаунта
        ){
            try
            {
            EntityManager emgr = emf.createEntityManager();
            emgr.getTransaction().begin();
            Long id=Long.valueOf(request.getId());
            SpravTypePrices updateDocument = emgr.find(SpravTypePrices.class, id);
            //id
            updateDocument.setId(id);
            //кто изменил
            User changer = userRepository.getUserByUsername(userRepository.getUserName());
            updateDocument.setChanger(changer);
            //дату изменения
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            updateDocument.setDate_time_changed(timestamp);
            //Наименование
            updateDocument.setName (request.getName() == null ? "": request.getName());
            //Роль цены
//            EntityManager em = emf.createEntityManager();
//            SpravSysPriceRole priceRole = em.find(SpravSysPriceRole.class, (Long.valueOf(Integer.parseInt(request.getPricerole_id()))));
//            updateDocument.setPricerole(priceRole);
            //дополнительная информация
            updateDocument.setDescription (request.getDescription() == null ? "": request.getDescription());

            emgr.getTransaction().commit();
            emgr.close();
            return 1;
            } catch (Exception e) {
                logger.error("Exception in method updateTypePrices.", e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertTypePrices(TypePricesForm request) {
        if(securityRepositoryJPA.userHasPermissions_OR(9L,"93"))//  Типы цен : "Создание"
        {
            SpravTypePrices newDocument = new SpravTypePrices();
            try
            {
                //создатель
                User creator = userRepository.getUserByUsername(userRepository.getUserName());
                newDocument.setCreator(creator);//создателя
                //владелец
                User master = userRepository.getUserByUsername(
                        userRepositoryJPA.getUsernameById(
                                userRepositoryJPA.getUserMasterIdByUsername(
                                        userRepository.getUserName() )));
                newDocument.setMaster(master);
                //дата и время создания
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                newDocument.setDate_time_created(timestamp);//
                //предприятие
                newDocument.setCompany(companyRepositoryJPA.getCompanyById(Long.valueOf(Integer.parseInt(request.getCompany_id()))));
                //Наименование
                newDocument.setName (request.getName() == null ? "": request.getName());
                //Роль цены
//                EntityManager em = emf.createEntityManager();
//                SpravSysPriceRole priceRole = em.find(SpravSysPriceRole.class, (Long.valueOf(Integer.parseInt(request.getPricerole_id()))));
//                newDocument.setPricerole(priceRole);
                //дополнительная информация
                newDocument.setDescription(request.getDescription());
                entityManager.persist(newDocument);
                entityManager.flush();
                return newDocument.getId();
            } catch (Exception e) {
                logger.error("Exception in method insertTypePrices.", e);
                e.printStackTrace();
                return null;
            }
        } else return -1L;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer deleteTypePrices(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(9L, "94") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_type_prices", delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(9L, "94") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_type_prices", delNumbers)))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery = "update sprav_type_prices p" +
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
                logger.error("Exception in method deleteTypePrices. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer undeleteTypePrices(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(9L,"94") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_type_prices",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(9L,"94") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_type_prices",delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update sprav_type_prices p" +
                    " set changer_id="+ myId + ", " + // кто изменил (восстановил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " + //не удалена
                    " where p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") +")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                    query.executeUpdate();
                    return 1;
                } else return null;
            }catch (Exception e) {
                logger.error("Exception in method undeleteTypePrices. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @SuppressWarnings("Duplicates")
    public List<PriceTypesListJSON> getPriceTypesList(Long companyId) {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery=
                "select " +
                        " p.id as id, " +
                        " p.name  as name, " +
                        " p.description as description, " +
                        " coalesce(p.is_default, false) " +
                        " from sprav_type_prices p " +
                        " where p.master_id=" + myMasterId +
                        " and p.company_id=" + companyId +
                        " and coalesce(p.is_deleted, false) = false"+
                        " order by p.name asc";
        Query query =  entityManager.createNativeQuery(stringQuery);
        List<Object[]> queryList = query.getResultList();
        List<PriceTypesListJSON> returnList = new ArrayList<>();
        for(Object[] obj:queryList) {
            PriceTypesListJSON doc=new PriceTypesListJSON();
            doc.setId(Long.parseLong(obj[0].toString()));
            doc.setName((String) obj[1]);
            doc.setDescription((String) obj[2]);
            doc.setIs_default((Boolean) obj[3]);
            returnList.add(doc);
        }
        return returnList;
    }
    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean setDefaultPriceType(UniversalForm request) {// id : предприятие, id3 : Тип цены
        EntityManager emgr = emf.createEntityManager();
        SpravTypePrices document = emgr.find(SpravTypePrices.class, request.getId3());//сохраняемый документ
        boolean userHasPermissions_OwnUpdate = securityRepositoryJPA.userHasPermissions_OR(9L, "98"); // "Редактирование док-тов своего предприятия"
        boolean userHasPermissions_AllUpdate = securityRepositoryJPA.userHasPermissions_OR(9L, "97"); // "Редактирование док-тов всех предприятий" (в пределах родительского аккаунта, конечно же)
        boolean updatingDocumentOfMyCompany = (Long.valueOf(userRepositoryJPA.getMyCompanyId()).equals(request.getId()));//сохраняется документ моего предприятия
        Long DocumentMasterId = document.getMaster().getId(); //владелец сохраняемого документа.
        Long myMasterId = userRepositoryJPA.getMyMasterId();//владелец моего аккаунта
        boolean isItMyMastersDoc = (DocumentMasterId.equals(myMasterId));

        if (((updatingDocumentOfMyCompany && (userHasPermissions_OwnUpdate || userHasPermissions_AllUpdate))//(если сохраняю документ своего предприятия и у меня есть на это права
                || (!updatingDocumentOfMyCompany && userHasPermissions_AllUpdate))//или если сохраняю документ не своего предприятия, и есть на это права)
                && isItMyMastersDoc) //и сохраняемый документ под юрисдикцией главного аккаунта
        {
            try
            {
                String stringQuery;
                stringQuery =   " update sprav_type_prices set is_default=(" +
                        " case when (id="+request.getId3()+") then true else false end) " +
                        " where " +
                        " company_id= "+request.getId();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    // inserting base set of types of prices for new user
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public List<Long> insertPriceTypesFast(Long mId, Long cId) {
        String stringQuery;
        Map<String, String> map = commonUtilites.translateForUser(mId, new String[]{"'basic_price'","'sale_price'"});
        String t = new Timestamp(System.currentTimeMillis()).toString();
        List<Long> retList = new ArrayList<>();
        stringQuery = "insert into sprav_type_prices ( master_id,creator_id,company_id,date_time_created,name,is_default,is_deleted) values "+
                "("+mId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("basic_price")+"',true, false)," +
                "("+mId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("sale_price")+"',false, false)";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            stringQuery="select id from sprav_type_prices where " +
                    " date_time_created = (to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS')) and " +
                    " creator_id = " + mId + " and " +
                    " is_default = true";
            query = entityManager.createNativeQuery(stringQuery);
            retList.add(Long.valueOf(query.getSingleResult().toString()));
            stringQuery="select id from sprav_type_prices where " +
                    " date_time_created = (to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS')) and " +
                    " creator_id = " + mId + " and " +
                    " is_default = false";
            query = entityManager.createNativeQuery(stringQuery);
            retList.add(Long.valueOf(query.getSingleResult().toString()));
            return retList;
        } catch (Exception e) {
            logger.error("Exception in method insertPriceTypesFast. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

}
