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
package com.dokio.util;
import com.dokio.repository.Exceptions.CantSetHistoryCauseNegativeSumException;
import com.dokio.repository.SecurityRepositoryJPA;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import javax.persistence.*;
import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class CommonUtilites {

    Logger logger = Logger.getLogger("CommonUtilites");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    private UserDetailsServiceImpl userRepository;
    @Autowired
    private SecurityRepositoryJPA securityRepository;


    @SuppressWarnings("Duplicates")
    //возвращает id статуса, установленного по-умолчанию
    public Long getDocumentsDefaultStatus(Long companyId, int documentId){
        try {
            String stringQuery;
            stringQuery = "select id from sprav_status_dock where company_id=" + companyId + " and dock_id=" + documentId + " and is_default=true";
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    @SuppressWarnings("Duplicates")
    //возвращает id типа цены, установленного по-умолчанию
    public Long getPriceTypeDefault(Long companyId){
        try {
            String stringQuery;
            stringQuery = "select id from sprav_type_prices where company_id=" + companyId + " and is_default=true ";
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("Duplicates")  //генератор номера документа
    public Long generateDocNumberCode(Long company_id, String docTableName)
    {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "select coalesce(max(doc_number)+1,1) from "+docTableName+" where company_id="+company_id+" and master_id="+myMasterId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString(),10);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method generateDocNumberCode. SQL query:" + stringQuery, e);
            return 0L;
        }
    }

    @SuppressWarnings("Duplicates") // проверка на уникальность номера документа
    public Boolean isDocumentNumberUnical(Long company_id, Integer code, Long doc_id, String docTableName)
    {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "" +
                "select id from "+docTableName+" where " +
                "company_id="+company_id+
                " and master_id="+myMasterId+
                " and doc_number="+code;
        if(doc_id>0) stringQuery=stringQuery+" and id !="+doc_id; // чтобы он не срабатывал сам на себя
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return !(query.getResultList().size()>0); // >0 - false, номер не уникальный, !>0 - true, уникальный
        }
        catch (Exception e) {
            logger.error("Exception in method isDocumentNumberUnical. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return true;
        }
    }

    //превращает сет Long в строку с заданным делимитером, началом и концом. Например (1,2,3,4,5)
    public String SetOfLongToString(Set<Long> longList, String delimitter, String prefix, String suffix) {
        String result = longList.stream()
                .map(n -> String.valueOf(n))
                .collect(Collectors.joining(delimitter, prefix, suffix));
        return result;
    }
    //превращает список Long в строку с заданным делимитером, началом и концом. Например (1,2,3,4,5)
    public String ListOfLongToString(List<Long> longList, String delimitter, String prefix, String suffix) {
        String result = longList.stream()
                .map(n -> String.valueOf(n))
                .collect(Collectors.joining(delimitter, prefix, suffix));
        return result;
    }
    //превращает список Integer в строку с заданным делимитером, началом и концом. Например (1,2,3,4,5)
    public String ListOfIntToString(List<Integer> longList, String delimitter, String prefix, String suffix) {
        String result = longList.stream()
                .map(n -> String.valueOf(n))
                .collect(Collectors.joining(delimitter, prefix, suffix));
        return result;
    }
    //превращает список Integer в строку с заданным делимитером, началом и концом. Например (1,2,3,4,5)
    public String SetOfIntToString(Set<Integer> intSet, String delimitter, String prefix, String suffix) {
        String result = intSet.stream()
                .map(n -> String.valueOf(n))
                .collect(Collectors.joining(delimitter, prefix, suffix));
        return result;
    }
    //есть ли запись с идентичной UID в таблице? UID используется, чтобы исключить дубли при создании документов с использованием медленного интернета, когда браузер может дублировать POST-запросы
    public Boolean isDocumentUidUnical(String uid, String docTableName){
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "" +
                "select id from "+docTableName+" where " +
                " master_id="+myMasterId+
                " and uid=:uid";
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("uid",uid);
            return !(query.getResultList().size()>0); // >0 - false, номер не уникальный, ==0 - true, уникальный
        }
        catch (Exception e) {
            logger.error("Exception in method isDocumentUidUnical. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return true;
        }
    }

    private static final Set VALID_TABLE_NAMES
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of(    "shipment",
                    "acceptance",
                    "retail_sales",
                    "paymentout",
                    "paymentin",
                    "orderout",
                    "orderin",
                    "returnsup",
                    "return",
                    "cagent",
                    "payment_account",
                    "boxoffice",
                    "kassa",
                    "withdrawal", // изъятие из кассы ККТ
                    "depositing", // внесение в кассу ККТ
                    "correction")
            .collect(Collectors.toCollection(HashSet::new)));

    private static final Set NEGATIVE_ALLOWED_TABLE_NAMES // таблицы, исторические данные которых могут содержать отрицательные значения (например баланс контрагента может быть отрицательным, а сумма в кассе нет)
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of(    "cagent")
            .collect(Collectors.toCollection(HashSet::new)));


    public Boolean addDocumentHistory(String docAlias, Long companyId, Long objectId, String docTableName, String docPageName, Long docId, BigDecimal summIn, BigDecimal summOut, boolean isCompleted, String doc_number, Long doc_status_id) throws Exception {
        // docAlias - alias таблицы объекта, по которому идет запись. Данная таблица хранит историю изменений по этому объекту. Может быть: cagent, payment_account и др (см. VALID_TABLE_NAMES)
        // objectId - id объекта, к которому относится изменение. Например, id контрагента в случае docAlias='cagent', или кассы предприятия в случае docAlias='boxoffice'
        // docTableName - таблица документа, который влияет на сумму (из которого производится запись) - например shipment для отгрузки
        // docId - id документа, из которого производится запись (в таблице docTableName)
        // summIn, summOut - суммы, на которые изменится значение в истории. Примеры:
        // - из кассы изъяли 100 р.: summIn = 0, summOut = 100.00)
        // - произвели отгрузку на 200 р.: summIn = 0, summOut = 200.00 (Отрицательный баланс - Нам должны)
        // - произвели приёмку на 300 р.: summIn = 300, summOut = 0 (Положительный баланс - Мы должны)
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        BigDecimal summBefore = getSummFromHistory(docAlias, companyId, objectId);
        // если документ не относится к тем, для которых разрешена отрицательная сумма, и сумма отрицательная
        // (например, в кассе проедприятия, или на расчетном счету не может быть отрицательная сумма), ...
        if(!NEGATIVE_ALLOWED_TABLE_NAMES.contains(docAlias) && summIn.subtract(summOut).add(summBefore).compareTo(new BigDecimal(0)) < 0)
            throw new CantSetHistoryCauseNegativeSumException();// то кидаем исключение 'Невозможно записать отрицательную сумму', чтобы произошла отмена транзакции
        if(     securityRepository.companyBelongsToMyMastersAccount(companyId) &&
                !Objects.isNull(summBefore) &&
                VALID_TABLE_NAMES.contains(docAlias) &&
                VALID_TABLE_NAMES.contains(docTableName)) {
            String stringQuery;
            stringQuery = "" +
                    " insert into history_" + docAlias + "_summ (" +
                    " master_id," +
                    " company_id," +
                    " date_time_created," +
                    " object_id," +
                    " doc_table_name," +
                    " doc_id," +
                    " summ_in," +
                    " summ_out," +
                    " doc_number," +
                    " doc_page_name," +
                    " is_completed," +
                    " doc_status_id" +
                    ") values (" +
                    myMasterId + ", " +
                    companyId + ", " +
                    "now()," +
                    objectId + ", " +
                    "'"+docTableName+"', " + // тут не используем setParameter, т.к. выше уже проверили эти таблицы на валидность
                    docId + ", " +
                    summIn + ", " +
                    summOut + ", " +
                    doc_number + ", " +
                    "'"+docPageName+"', " +
                    isCompleted+", " +
                    doc_status_id +
                    " ) " +// при отмене проведения или повторном проведении
                    " ON CONFLICT " +// "upsert"
                    " DO update set " +
                    " summ_in = " + summIn +", " +
                    " summ_out = " + summOut +", " +
//                    " doc_number = " + doc_number  +", " + // на будущее, когда будет можно менять номер документа
                    " is_completed = " + isCompleted +", " +
                    " doc_status_id = " + doc_status_id;
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return true;
            }catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method addDocumentHistory. SQL: "+stringQuery, e);
                throw new Exception(); // отмена всей транзакции по причине ошибки записи
            }

        } else throw new Exception(); // отмена всей транзакции по причине попытки создать запись по не своему предприятию или не по правильным таблицам
    }

//    private Boolean isDocInHistor

    public BigDecimal getSummFromHistory(String objectName, Long companyId, Long objectId) {
        if(VALID_TABLE_NAMES.contains(objectName)) {
            String stringQuery =
                    " select SUM(summ_in-summ_out) from history_"+objectName+"_summ where " +
                    " company_id= " + companyId +
                    " and object_id= " + objectId +
                    " and is_completed=true " +
                    " order by id desc limit 1";
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                return (BigDecimal) query.getSingleResult();
            } catch (NoResultException nre) {
                return new BigDecimal(0);
            } catch (Exception e) {
                logger.error("Exception in method getSummFromHistory. SQL: " + stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return null; // попытка запроса не по правильным таблицам
    }

    private static final Set VALID_OUTCOME_PAYMENTS_TABLE_NAMES
        = Collections.unmodifiableSet((Set<? extends String>) Stream.of("paymentout","orderout","withdrawal").collect(Collectors.toCollection(HashSet::new)));
//    private static final Set VALID_INCOME_PAYMENTS_TABLE_NAMES
//            = Collections.unmodifiableSet((Set<? extends String>) Stream.of("paymentout","orderout","withdrawal").collect(Collectors.toCollection(HashSet::new)));
    // устанавливает доставлено=true для исходящего внутреннего платежа (например Выемки, либо внутреннего Исходящего платежа, или внутреннего Расходного ордера)
    public boolean setDelivered(String tableName, Long id) throws Exception {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery = "Update "+tableName+" p" +
                " set is_delivered=true " +
                " where p.id = " + id + " and p.master_id=" + myMasterId;
        if (!VALID_OUTCOME_PAYMENTS_TABLE_NAMES.contains(tableName))
            throw new IllegalArgumentException("Недопустимые параметры запроса. Таблицы нет в списке разрешённых: "+tableName); // отмена всей транзакции из вызывающего метода
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method setDelivered. SQL query:" + stringQuery, e);
            e.printStackTrace();
            throw new Exception(); // отмена всей транзакции из вызывающего метода
        }
    }

    @SuppressWarnings("Duplicates")
    // возвращает список страниц (первые 3 параметра - Найдено: p1, страница p2 из p3)
    // size - общее количество выборки
    // result - количество записей, отображаемых на странице
    // pagenum - отображаемый в пагинации номер страницы
    public List<Integer> getPagesList(int pagenum, int size, int result){
        int listsize;//количество страниц пагинации
        if((size%result) == 0){//общее количество выборки делим на количество записей на странице
            listsize= size/result;//если делится без остатка
        }else{
            listsize= (size/result)+1;}
        int maxPagenumInBegin;//
        List<Integer> pageList = new ArrayList<Integer>();//список, в котором первые 3 места - "всего найдено", "страница", "всего страниц", остальное - номера страниц для пагинации
        pageList.add(size);
        pageList.add(pagenum);
        pageList.add(listsize);

        if (listsize<=5){
            maxPagenumInBegin=listsize;//
        }else{
            maxPagenumInBegin=5;
        }
        if(pagenum >=3) {
            if((pagenum==listsize)||(pagenum+1)==listsize){
                for(int i=(pagenum-(4-(listsize-pagenum))); i<=pagenum-3; i++){
                    if(i>0) {
                        pageList.add(i);  //создается список пагинации за - 4 шага до номера страницы (для конца списка пагинации)
                    }}}
            for(int i=(pagenum-2); i<=pagenum; i++){
                pageList.add(i);  //создается список пагинации за -2 шага до номера страницы
            }
            if((pagenum+2) <=listsize) {
                for(int i=(pagenum+1); i<=(pagenum+2); i++){
                    pageList.add(i);  //создается список пагинации  на +2 шага от номера страницы
                }
            }else{
                if(pagenum<listsize) {
                    for (int i = (pagenum + (listsize - pagenum)); i <= listsize; i++) {
                        pageList.add(i);  //создается список пагинации от номера страницы до конца
                    }}}
        }else{//номер страницы меньше 3
            for(int i=1; i<=pagenum; i++){
                pageList.add(i);  //создается список пагинации от 1 до номера страницы
            }
            for(int i=(pagenum+1); i<=maxPagenumInBegin; i++){
                pageList.add(i);  //создаются дополнительные номера пагинации, но не более 5 в сумме
            }}
        return pageList;
    }

    public boolean isDateValid(String s) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        sdf.setLenient(false);
        return sdf.parse(s, new ParsePosition(0)) != null;
    }
}
