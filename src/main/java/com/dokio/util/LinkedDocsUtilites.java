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
import com.dokio.message.response.additional.LinkedDocsJSON;
import com.dokio.message.response.additional.LinkedDocsLinksJSON;
import com.dokio.message.response.additional.LinkedDocsSchemeJSON;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class LinkedDocsUtilites {

    Logger logger = Logger.getLogger("CommonUtilites");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    private UserDetailsServiceImpl userRepository;
    @Autowired
    private CommonUtilites cu;

    private static final Map<String,String> COLUMN_BY_TABLENAME = createMap();
    private static Map<String,String> createMap() {
        Map<String, String> result = new HashMap<>();
        result.put("customers_orders","customers_orders");
        result.put("acceptance","acceptance");
        result.put("return","return");
        result.put("returnsup","returnsup");
        result.put("shipment","shipment");
        result.put("retail_sales","retail_sales");
        result.put("products","products");
        result.put("inventory","inventory");
        result.put("writeoff","writeoff");
        result.put("posting","posting");
        result.put("moving","moving");
        result.put("ordersup","ordersup");
        result.put("invoiceout","invoiceout");
        result.put("invoicein","invoicein");
        result.put("paymentin","paymentin");
        result.put("paymentout","paymentout");
        result.put("shifts","shifts");
        result.put("orderin","orderin");
        result.put("orderout","orderout");
        result.put("vatinvoiceout","vatinvoiceout");
        result.put("correction","correction");
        result.put("scdl_appointments","appointment");
        result.put("withdrawal","withdrawal");
        result.put("depositing","depositing");
        result.put("vatinvoicein","vatinvoicein");
        return Collections.unmodifiableMap(result);
    }

    private static final Set VALID_TABLENAMES
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of(    "customers_orders",
                    "acceptance",
                    "return",
                    "returnsup",
                    "shipment",
                    "retail_sales",
                    "products",
                    "inventory",
                    "writeoff",
                    "posting",
                    "moving",
                    "ordersup",
                    "invoiceout",
                    "invoicein",
                    "paymentin",
                    "paymentout",
                    "shifts",
                    "orderin",
                    "orderout",
                    "vatinvoiceout",
                    "correction",
                    "scdl_appointments",
                    "withdrawal",//выемка из кассы ККМ
                    "depositing", // внесение средств в кассу ККМ
                    "vatinvoicein")
            .collect(Collectors.toCollection(HashSet::new)));

    public static final Set DOCS_WITH_PRODUCT_SUMPRICE // таблицы документов, у которых (в их таблице <tablename>_product) есть колонка product_sumprice, по которой можно посчитать сумму стоимости товаров в отдельном документе
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of(    "acceptance",
                    "return",
                    "returnsup",
                    "shipment",
                    "retail_sales",
                    "writeoff",
                    "posting",
                    "moving",
                    "customers_orders",
                    "inventory",
                    "ordersup",
                    "scdl_appointments",
                    "invoiceout",
                    "invoicein")
            .collect(Collectors.toCollection(HashSet::new)));

    private static final Set DOCS_WITH_PAY_SUMM // таблицы документов, у которых (в их таблице <tablename>) есть колонка summ
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of(    "paymentin",
                    "paymentout",
                    "orderin",
                    "correction",
                    "withdrawal",//выемка из кассы ККМ
                    "depositing", // внесение средств в кассу ККМ
                    "orderout")
            .collect(Collectors.toCollection(HashSet::new)));

    private static final Set DOCS_WITHOUT_PAY_SUMM // таблицы документов, у которых (в их таблице <tablename>) нет колонки summ (эти документы берут summ у родительского документа)
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of(    "vatinvoiceout",
                    "vatinvoicein")
            .collect(Collectors.toCollection(HashSet::new)));

    // Если у документа linked_doc_name с id = linked_doc_id есть группа связанных документов (т.е. linked_docs_group_id в его таблице != null)
    // то возвращаем id этой группы, иначе:
    // 1. Создаём новую группу (createLinkedGruoup)
    // 2. Прописываем ее id в таблице документа (setLinkedGroup)
    // 3. Возвращаем её id
//    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Long getOrCreateAndGetGroupId(Long linked_doc_id, String linked_doc_name, Long companyId, Long masterId) {

        Long result = getLinkedGroupId(masterId, linked_doc_name, linked_doc_id);

        if (!Objects.isNull(result)) {

            // если документ не состоит в группе связанных документов
            if (result == 0L) {

                // создаём эту группу
                Long groupId = createLinkedGruoup(companyId, masterId);

                if (!Objects.isNull(groupId)) {

                    //прописываем id группы в этом документе
                    if (setLinkedGroup(linked_doc_name, groupId, linked_doc_id, masterId)) {

                        return groupId;

                    } else return null; //ошибка при назначении id группы документу

                } else return null; // ошибка при создании группы

                // документ уже состоит в группе связанных.
            } else return result; // возвращаем id группы

        } else return null;//ошибка при проверке на наличие у документа id группы
    }

    // Если у документа уже есть группа - возвращаем ее id, нет - 0, ошибка - null
    private Long getLinkedGroupId(Long masterId, String docTable, Long docId) {

        String stringQuery = "select linked_docs_group_id from " + docTable + " where id = " + docId + " and master_id=" + masterId;

        if (!VALID_TABLENAMES.contains(docTable)) {
            throw new IllegalArgumentException("Invalid query parameters in isDocHaveLinkedGroup");
        }

        try {

            Query query = entityManager.createNativeQuery(stringQuery);
            Object obj = query.getSingleResult();

            if (Objects.isNull(obj)) {
                return 0L;
            } else {
                return Long.valueOf(obj.toString());
            }

        } catch (Exception e) {
            logger.error("Exception in method getLinkedGroupId. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    // создаёт новую группу для связанных документов, возвращает её id
    private Long createLinkedGruoup(Long companyId, Long masterId) {
        Long newDocId;
        String timestamp = new Timestamp(System.currentTimeMillis()).toString();
        String stringQuery = " insert into linked_docs_groups (" +
                " master_id," + //мастер-аккаунт
                " company_id," + //предприятие, для которого создается документ
                " date_time_created" + //дата и время создания
                ") values (" +
                masterId + ", " +//мастер-аккаунт
                companyId + ", " +//предприятие, для которого создается документ
                "to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')" +//дата и время создания
                ")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            stringQuery = "select id from linked_docs_groups where date_time_created=(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')) and master_id=" + masterId;
            Query query2 = entityManager.createNativeQuery(stringQuery);
            newDocId = Long.valueOf(query2.getSingleResult().toString());
            return newDocId;
        } catch (Exception e) {
            logger.error("Exception in method createLinkedGruoup on inserting into linked_docs_groups. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    // Назначает для документа группу связанных документов
    private Boolean setLinkedGroup(String docTable, Long groupId, Long docId, Long masterId) {
        String stringQuery = "update " + docTable + " set linked_docs_group_id =" + groupId + " where id = " + docId + " and master_id = " + masterId;

        if (!VALID_TABLENAMES.contains(docTable)) {
            throw new IllegalArgumentException("Invalid query parameters in setLinkedGroup");
        }

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method setLinkedGroup on updating " + docTable + ". SQL query:" + stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    // linked_doc_id - id документа, из которого создавали другой документ (например, Инвентаризация)
    // created_doc_id - id созданного документа (например, Списание)
    // linkedDocsGroupId - id группы связанных документов, в которую помещаем эти 2 документа
    // uid - UUID создаваемого документа. Сравнивая uid и parent_uid, можно понять, связь уже имеющегося и создаваемого документов будет прямая (сверху вниз по иерархии) или реверсная
    // parent_uid - UUID родительского документа (это не обязательно документ, из которого создавали, т.к. можно создать из дочернего родительский
    // child_uid - UUID дочернего документа (это не обязательно будет созданный документ. Например при создании из Отгрузки Счета покупателю - последний будет родительским (parent_uid и createdDocId)
    // linked_doc_name - имя таблицы документа, из которого создавали другой документ (например, inventory)
    // created_doc_name - имя таблицы созданного документа (например, writeoff)
    public Boolean addDocsToGroupAndLinkDocs(Long linked_doc_id, Long created_doc_id, Long linkedDocsGroupId, String parent_uid, String child_uid, String linked_doc_name, String created_doc_name, String uid, Long companyId, Long masterId) {

        // Добавляем оба документа в группу связанных документов. Если прошло успешно
        if (addDocsToGroup(linked_doc_id, created_doc_id, linkedDocsGroupId, parent_uid, child_uid, linked_doc_name, created_doc_name, uid, companyId, masterId)) {
            // ... то залинкуем их
            return addLinksBetweenLinkedDocs(linkedDocsGroupId, parent_uid, child_uid, companyId, masterId);
        } else return false;
    }


    // добавляем в группу связанных документов документ, из которого создавали (если он еще не добавлен), и созданный документ
    @SuppressWarnings("Duplicates")
    private Boolean addDocsToGroup(Long linked_doc_id, Long created_doc_id, Long linkedDocsGroupId, String parent_uid, String child_uid, String linked_doc_name, String created_table_name, String uid, Long companyId, Long masterId) {

        //если UID от документа, который создаем, является родительским - значит связь реверсная (снизу вверх по иерархии)
        boolean reverse = uid.equals(parent_uid);

        try {

            if (!VALID_TABLENAMES.contains(linked_doc_name)) {
                throw new IllegalArgumentException("Invalid query parameters in addDocsToGroup");
            }

            String stringQuery = " insert into linked_docs (" +
                    " master_id, " +
                    " company_id, " +
                    " group_id, " +
                    " doc_id, " +
                    " doc_uid, " +
                    " tablename, " +
                    (reverse?created_table_name:linked_doc_name) + "_id" +
                    ") values (" +
                    masterId + ", " +
                    companyId + ", " +
                    linkedDocsGroupId + ", " +
                    (reverse?created_doc_id:linked_doc_id) + ", " +
                    ":parent_uid " + ", " +
                    "'" + (reverse?created_table_name:linked_doc_name) + "', " +
                    (reverse?created_doc_id:linked_doc_id) + ")" +
                    "ON CONFLICT DO NOTHING";//значит он уже есть в данной группе

            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("parent_uid", (parent_uid == null ? "" : parent_uid));
            query.executeUpdate();

            stringQuery = " insert into linked_docs (" +
                    " master_id, " +
                    " company_id, " +
                    " group_id, " +
                    " doc_id, " +
                    " doc_uid, " +
                    " tablename, " +
                    (reverse?linked_doc_name:created_table_name) + "_id" +
                    ") values (" +
                    masterId + ", " +
                    companyId + ", " +
                    linkedDocsGroupId + ", " +
                    (reverse?linked_doc_id:created_doc_id) + ", " +
                    ":child_uid " + ", " +
                    "'" + (reverse?linked_doc_name:created_table_name) + "', " +
                    (reverse?linked_doc_id:created_doc_id) + ")" +
                    "ON CONFLICT DO NOTHING";

            Query query2 = entityManager.createNativeQuery(stringQuery);
            query2.setParameter("child_uid", (child_uid == null ? "" : child_uid));
            query2.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method addDocsToGroup on inserting into linked_docs.", e);
            e.printStackTrace();
            return false;
        }
    }

    //создание собственно связей между связанными документами
    private Boolean addLinksBetweenLinkedDocs(Long linkedDocsGroupId, String parent_uid, String child_uid, Long companyId, Long masterId) {

        String stringQuery = " insert into linked_docs_links (" +
                " master_id, " +
                " company_id, " +
                " group_id, " +
                " parent_uid, " +
                " child_uid " +
                ") values (" +
                masterId + ", " +
                companyId + ", " +
                linkedDocsGroupId + ", " +
                ":parent_uid " + ", " +
                ":child_uid)" +
                "ON CONFLICT DO NOTHING";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("parent_uid", (parent_uid == null ? "" : parent_uid));
            query.setParameter("child_uid", (child_uid == null ? "" : child_uid));
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method addLinksBetweenLinkedDocs on inserting into linked_docs_links.", e);
            e.printStackTrace();
            return false;
        }
    }


    public LinkedDocsSchemeJSON getLinkedDocsScheme(String uid) {

        LinkedDocsSchemeJSON linkedDocsScheme = new LinkedDocsSchemeJSON();
//
        try {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String shemeText;
            Integer count = 0; //кол-во докуменов в группе
            String bookingDocNameVariation = "";// variation's name of booking document: appointment, reservation
            String pageName = "";
            //узнаем id группы связанных документов по UID одного из документов
            Long groupId = getColumnValueByUid(uid, "group_id", myMasterId);
            if (!Objects.isNull(groupId) && groupId > 0) { //null - ошибка, 0 - нет результата, т.е. у документа нет связанных с ним других документов, и он не состоит в группе связанных

                // по UID группы достанем начальную информацию по документам группы
                // (начальную - потому что она не будет включать внутреннюю информацию документа,
                // такую как наименование статуса или сумму по товарам, т.к. наименования таблиц
                // документов пока неизвестны (они в linked_docs, и нельзя делать конструкции типа " from ld.tablename").
                // Данную информацию придется получать в цикле по полученным данным)

                List<LinkedDocsJSON> baseList = getBaseInfoOfLinkedDocs(groupId);
                if (!Objects.isNull(baseList)) {

                    //достаем полную информацию и инфо по связям документов
                    List<LinkedDocsJSON> returnList = getFullInfoOfLinkedDocs(baseList);
                    List<LinkedDocsLinksJSON> linksList = getLinks(groupId);
                    if (!Objects.isNull(returnList) && !Objects.isNull(linksList)) {
                        Map<String, String> map = cu.translateForMe(new String[]{});// download all translation dictionary
                        LinkedDocsSchemeJSON sheme = new LinkedDocsSchemeJSON();

                        shemeText = "digraph {" +
                                "              rankdir=TB;" +
                                "              node [ shape=record;" +
                                "              margin=0;" +
                                "              fixedsize = true;" +
                                "              width=2.3;" +
                                "              height=1.3;" +
                                "              fontsize=12;" +
                                "              fontname=\"Arial\";" +
                                "              style=filled;" +
                                "              fillcolor=\"#ededed\";" +
                                "              color=\"#2b2a2a\";" +
                                "              ]; ";

                        for (LinkedDocsJSON linkedDoc : returnList) {

                            // Document Appointment may has a variations of name. In accordance of type of business
                            // it can be Appointment or Reservation.
                            // In dictionary table tgere are two translations: 'scdl_appointments'-'Appointment' and 'reservation'-'Reservation'
                            if(linkedDoc.getPagename().equals("appointments")){
                                Long companyId = getColumnValueByUid(uid, "company_id", myMasterId);
                                bookingDocNameVariation = cu.getCompanySettings(companyId).getBooking_doc_name_variation();
                                pageName = bookingDocNameVariation.equals("appointment")?"appointment":"reservation";
                            } else pageName = linkedDoc.getPagename();
                            //перед UID добавляю букву, т.к. на фронте Graphviz некорректно работает с наименованиями node-ов, которые начинаются на цифры

                            shemeText = shemeText + "a" + linkedDoc.getUid().replace("-", "") + " [";
                            shemeText = shemeText + "URL=\"ui/" + linkedDoc.getPagename() + "doc/" + linkedDoc.getId() + "\";";
                            if (uid.equals(linkedDoc.getUid()))
                                shemeText = shemeText + " fillcolor=\"#acee00\";"; // если это node документа, из которого запрашивали схему - окрасим ноду в другой цвет
                            shemeText = shemeText + "label=\"{" + map.get(pageName) + "|№" + linkedDoc.getDoc_number() + "\\n" + linkedDoc.getDate_time_created() + "\\n";
                            if (!Objects.isNull(linkedDoc.getSumprice()))
                                shemeText = shemeText + linkedDoc.getSumprice() + "\\n";
                            shemeText = shemeText + map.get("completed") + ": " + (linkedDoc.isIs_completed() ? map.get("yes") : map.get("no")) + "\\n" + linkedDoc.getStatus() + "}\";";
                            shemeText = shemeText + "tooltip=\"" + map.get("open_in_new_window") + "\";";
                            shemeText = shemeText + "] ";

                            count++;

                        }
                        // сборка массива информации по связям документов. В данном цикле необходимо получить массив вида
                        //                    <UUID документа> -> <UUID документа>;
                        //                    <UUID документа> -> <UUID документа>;
                        //                    <UUID документа> -> <UUID документа>;
                        for (LinkedDocsLinksJSON link : linksList) {

                            shemeText = shemeText + "a" + link.getUid_from().replace("-", "") + " -> " + "a" + link.getUid_to().replace("-", "") + ";";

                        }
                        shemeText = shemeText + "}";

                        sheme.setText(shemeText);

                        sheme.setCount(count);
                        return sheme;

                    } else return null;

                } else return null;

            } else { // либо ошибка ( groupId = null),  либо нет связей (groupId = 0)

                if (Objects.isNull(groupId))
                    return null; // ошибка
                else { // groupId=0, т.е. нет связей
                    linkedDocsScheme.setErrorCode(0L);
                    return linkedDocsScheme;
                }
            }
        } catch (Exception e) {
            logger.error("Exception in method getLinkedDocsScheme for UID = " + uid, e);
            e.printStackTrace();
            return null;
        }
    }

    // возвращает лист линков по UID документов (от, к)
    private List<LinkedDocsLinksJSON> getLinks(Long groupId) {
        String stringQuery = "select " +
                "   ldl.parent_uid as parent_uid, " +
                "   ldl.child_uid as child_uid" +
                "   from " +
                "   linked_docs_links ldl" +
                "   where ldl.group_id = " + groupId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<LinkedDocsLinksJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                LinkedDocsLinksJSON doc = new LinkedDocsLinksJSON();
                doc.setUid_from((String) obj[0]);
                doc.setUid_to((String) obj[1]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            logger.error("Exception in method getLinks. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    private List<LinkedDocsJSON> getFullInfoOfLinkedDocs(List<LinkedDocsJSON> linkedDocs) {
        try {
            List<LinkedDocsJSON> returnList = new ArrayList<>();
            String dateFormat=userRepositoryJPA.getMyDateFormat();
            LinkedDocsJSON doc;

            for (LinkedDocsJSON linkedDoc : linkedDocs) {

                doc = getFullInfoOfLinkedDoc(linkedDoc.getTablename(), linkedDoc.getId(),dateFormat);

                doc.setId(linkedDoc.getId());
                doc.setTablename(linkedDoc.getTablename());
                doc.setGroup_id(linkedDoc.getGroup_id());
                doc.setUid(linkedDoc.getUid());
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getFullInfoOfLinkedDocs.", e);
            return null;
        }
    }

    private LinkedDocsJSON getFullInfoOfLinkedDoc(String tablename, Long id, String dateFormat) {

        String myTimeZone = userRepository.getUserTimeZone();
        String tableWithSumm="";
        Long   idInTableWithSumm=0L;
        // если у документа в его таблице <tablename> нет колонки summ, то берём summ у родительского документа)
        // для этого нужно знать его таблицу и id этого документа в ней
        if(DOCS_WITHOUT_PAY_SUMM.contains(tablename)) {
            tableWithSumm = getTablenameOfDocWithoutSumm(tablename, id);            //таблица родительского документа
            idInTableWithSumm=getIdOfDocWithoutSumm(tablename, tableWithSumm, id);  //id родительского документа
            //переназначим вводные параметры для данного метода
//            tablename=tableWithSumm;
//            id=idInTableWithSumm;
        }

        String stringQuery = "select " +
                "   d.doc_number as doc_number, " +
                "   to_char(d.date_time_created at time zone '" + myTimeZone + "', '"+dateFormat+" HH24:MI') as date_time_created, " +
                "   (select ds.doc_name_ru from documents ds where ds.table_name = '" + tablename + "') as doc_name," +
                "   coalesce(ssd.name,'-') as status_name," +
                (DOCS_WITH_PRODUCT_SUMPRICE.contains(tablename) ?
                        ("  coalesce((select sum(coalesce(product_sumprice,0)) from " + tablename + "_product where " + COLUMN_BY_TABLENAME.get(tablename) + "_id=" + id + "),0)") :
                        (DOCS_WITH_PAY_SUMM.contains(tablename) ?
                                ("  coalesce((select sum(coalesce(summ,0)) from " + tablename + " where id=" + id + "),0)") :
                                (DOCS_WITH_PRODUCT_SUMPRICE.contains(tableWithSumm) ?
                                        ("  coalesce((select sum(coalesce(product_sumprice,0)) from " + tableWithSumm + "_product where " + COLUMN_BY_TABLENAME.get(tableWithSumm) + "_id=" + idInTableWithSumm + "),0)") :
                                        (DOCS_WITH_PAY_SUMM.contains(tableWithSumm) ?
                                                ("  coalesce((select sum(coalesce(summ,0)) from " + tableWithSumm + " where id=" + idInTableWithSumm + "),0)") :
                                                0.00
                                        )
                                )
                        )
                ) + " as sum_price," +
                "   coalesce(d.is_completed,false) as is_completed," +
                "   (select ds.page_name from documents ds where ds.table_name = '" + tablename + "') as page_name" +
                "   from " + tablename + " d" +
                "   left outer join sprav_status_dock ssd on d.status_id = ssd.id " +
                "   where " +
                "   d.id = " + id;

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            LinkedDocsJSON returnObj = new LinkedDocsJSON();

            for (Object[] obj : queryList) {
//                returnObj.setDoc_number(Long.parseLong(obj[0].toString()));
                returnObj.setDoc_number(obj[0]!=null?Long.parseLong(obj[0].toString()):null);
                returnObj.setDate_time_created((String) obj[1]);
                returnObj.setName((String) obj[2]);
                returnObj.setStatus((String) obj[3]);
                returnObj.setSumprice((BigDecimal) obj[4]);
                returnObj.setIs_completed((Boolean) obj[5]);
//                returnObj.setName((String) obj[2]);
                returnObj.setPagename((String) obj[6]);
            }
            return returnObj;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getFullInfoOfLinkedDoc. stringQuery=" + stringQuery, e);
            return null;
        }
    }


    private List<LinkedDocsJSON> getBaseInfoOfLinkedDocs(Long groupId) {

        String stringQuery = "select " +
                "   ld.doc_id as doc_id, " +
                "   ld.tablename as tablename," +
                "   ld.group_id as group_id, " +
                "   ld.doc_uid as doc_uid" +
                "   from " +
                "   linked_docs ld" +
                "   where ld.group_id = " + groupId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();

            List<LinkedDocsJSON> returnList = new ArrayList<>();

            for (Object[] obj : queryList) {
                LinkedDocsJSON doc = new LinkedDocsJSON();
                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setTablename((String) obj[1]);
                doc.setGroup_id(Long.parseLong(obj[2].toString()));
                doc.setUid((String) obj[3]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            logger.error("Exception in method getBaseInfoOfLinkedDocs. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }


    private Long getColumnValueByUid(String uid, String columnName, Long myMasterId) {

        String stringQuery = "select "+columnName+" from linked_docs where doc_uid=:uid and master_id=" + myMasterId;

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("uid", uid);
            return Long.valueOf(query.getSingleResult().toString());
        } catch (NoResultException nre) {
//            logger.error("NoResultException in method getGroupIdByUid. Sql: " + stringQuery, nre);
            return 0L;
        } catch (Exception e) {
            logger.error("Exception in method getGroupIdByUid. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    // возвращает List документов, которые не могут быть удалены по причине наличия у них связанных с ними дочерних документов
    public List<LinkedDocsJSON> checkDocHasLinkedChilds(String ids, String docTableName) {
        if (!VALID_TABLENAMES.contains(docTableName)) {
            throw new IllegalArgumentException("Invalid query parameters in checkDocHasLinkedChilds");
        }
        List<LinkedDocsJSON> docs = getSetDocUidsByIds(ids, docTableName);
        if (!Objects.isNull(docs) && docs.size() > 0) {
            Set<String> uids = new HashSet<>();
            //из списка документов собрали сет UID'ов
            for (LinkedDocsJSON doc : docs) {
                uids.add(doc.getUid());
            }

            //сейчас нужно понять, какие из UID являются родительскими по отношению к другим документам

            String stringQuery = "select " +
                    "   ld.parent_uid as parent_uid, " +
                    "   ld.child_uid as child_uid " +
                    "   from linked_docs_links ld" +
                    "   where ld.parent_uid in ('" + StringUtils.join(uids, "','") + "')";
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                List<Object[]> queryList = query.getResultList();
                Set<String> parentUidsList = new HashSet<>();
                for (Object[] obj : queryList) {
                    parentUidsList.add((String) obj[0]);
                }

                List<LinkedDocsJSON> returnList = new ArrayList<>();
                //если в присланном списке документов есть те, которые являются родительскими, нужно вернуть инфо по ним ( id, номер док-та, uid)
                if (parentUidsList.size() > 0) {

                    for (LinkedDocsJSON doc : docs) {
                        if (parentUidsList.contains(doc.getUid()))
                            returnList.add(doc);
                    }
                    return returnList;
                } else
                    return returnList; // возвращаем пустой список, значит у ни один из присланных id документов не является родительским
            } catch (Exception e) {
                logger.error("Exception in method getSetDocUidsByIds. Sql: " + stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    // отдает информацию о документах таблицы docTableName по их id
    private List<LinkedDocsJSON> getSetDocUidsByIds(String ids, String docTableName) {

        String stringQuery = "select " +
                "   ld.id as id, " +
                "   ld.uid as uid, " +
                "   ld.doc_number as doc_number " +
                "   from " + docTableName + " ld" +
                "   where ld.id in (" + ids + ")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();

            List<LinkedDocsJSON> returnList = new ArrayList<>();

            for (Object[] obj : queryList) {
                LinkedDocsJSON doc = new LinkedDocsJSON();
                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setUid((String) obj[1]);
                doc.setDoc_number(Long.parseLong(obj[2].toString()));
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            logger.error("Exception in method getSetDocUidsByIds. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //удаляет документы из группы связанных документов (удаляя его из linked_docs и linked_docs_links). Необходимо при удалении документа
    @SuppressWarnings("Duplicates")
    public Boolean deleteFromLinkedDocs(String ids, String docTableName) {
        if (!VALID_TABLENAMES.contains(docTableName)) {
            throw new IllegalArgumentException("Invalid query parameters in deleteFromLinkedDocs");
        }
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        // сначала проверим, не имеет ли какой-либо из документов связанных с ним дочерних документов
        List<LinkedDocsJSON> checkChilds = checkDocHasLinkedChilds(ids, docTableName);

        if (!Objects.isNull(checkChilds) && checkChilds.size() == 0) { //если нет ошибки и если связи с дочерними документами отсутствуют

            //соберем информацию о документах
            List<LinkedDocsJSON> docs = getSetDocUidsByIds(ids, docTableName);
            if (!Objects.isNull(docs) && docs.size() > 0) { //если собрали
                Set<String> uids = new HashSet<>();
                //из этой информации выделим сет UID'ов
                for (LinkedDocsJSON doc : docs) {
                    uids.add(doc.getUid());
                }
                if(uids.size()>0) {//если в информации были uid
                    //удаляем все ссылки на документы и сами документы из группы связанных
                    if (deleteLinksByDocUid(uids, myMasterId) && deleteFromLinkedDocsByDocUid(uids, myMasterId)) {
                        return true;
                    } else return false;
                } else return true;//uid'ов нет, но это не повод для ошибки
            } else return false;
        } else return false;
    }

    // удаляет все ссылки на документы
    private Boolean deleteLinksByDocUid(Set<String> uids, Long myMasterId) {

        String stringQuery = "delete from linked_docs_links where master_id=" + myMasterId + " and child_uid in ('" + StringUtils.join(uids, "','") + "')";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method deleteLinksByDocUid. Sql: " + stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    // удаляет документы из группы связанных документов
    private Boolean deleteFromLinkedDocsByDocUid(Set<String> uids, Long myMasterId) {
        String stringQuery = "delete from linked_docs where master_id=" + myMasterId + " and doc_uid in ('" + StringUtils.join(uids, "','") + "')";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method deleteFromLinkedDocsByDocUid. Sql: " + stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    // для документов, которые не содержат summ, а берут ее из родительского документа (таких как Счёт-факутра выданный) вернёт название таблицы родительского документа, где можно взять эту summ
    public String getTablenameOfDocWithoutSumm(String tablename, Long id){
        String stringQuery = "select parent_tablename from "+tablename+" where id = "+id;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return query.getSingleResult().toString();
        } catch (NoResultException nre) {
            logger.error("NoResultException in method getTablenameOfDocWithoutSumm. Sql: " + stringQuery, nre);
            return null;
        } catch (Exception e) {
            logger.error("Exception in method getTablenameOfDocWithoutSumm. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    // для документов, которые не содержат summ, а берут ее из родительского документа (таких как Счёт-факутра выданный) вернёт id родительского документа в таблице родительского документа, где можно взять эту summ
    public Long getIdOfDocWithoutSumm(String tablename, String columnName, Long id) {

        String stringQuery = "select "+columnName+"_id from "+tablename+" where id = "+id;

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.valueOf(query.getSingleResult().toString());
        } catch (NoResultException nre) {
//            logger.error("NoResultException in method getIdOfDocWithoutSumm. Sql: " + stringQuery, nre);
            return 0L;
        } catch (Exception e) {
            logger.error("Exception in method getIdOfDocWithoutSumm. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    // отдает
    private String getUidById(String tablename, Long docId){
        String stringQuery = "select uid from "+tablename+" where id = "+docId+" and uid is not null";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return query.getSingleResult().toString();
        } catch (NoResultException nre) {
            logger.error("Can't find UID by ID in method getUidById. Sql: " + stringQuery, nre);
            return null;
        } catch (Exception e) {
            logger.error("Exception in method getUidById. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    // Некоторые документы, например "Счет Фактура" сами по себе не содержат товарных позиций, поэтому нужно по цепочке связанных документов
    // найти родителя, который содержит товарные позиции. Это может быть Заказ поставщику, Счёт поставщика, Приёмка
    // Такой родитель может быть выше на 1 или 2 (максимум) ступени, например:
    // Приёмка (тут товары) -> Счёт-фактура
    // Счёт поставщика (тут товары) -> Исходящий платёж -> Счёт-фактура

    // Метод вернет инфу (id, uid, group_id и tablename) по родительскому документу с товарами, для дочернего документа без оных (например, для Счёт-фактуры).
    // но только на 2 ступени вверх (больше обычно и не надо) - для 1й ступени этот документ уже может
    // являться искомым, т.к. инфа по нему (parent_tablename и id) уже содержится в дочернем документе,
    // просто мы не знаем, есть в нем товары или нет
    public LinkedDocsJSON getParentDocWithProducts(String parentTablename, Long parentDocId) throws Exception {
        //parentTablename -    имя таблицы родительского документа, прописанное в дочернем документе (например, в vatinvoicein), находящегося на 1 ступень над дочерним
        //parentDocId  -       id родительского документа, прописанное в дочернем документе



        // Сначала проверим 1ю ступень, вдруг этот документ уже содержит товарные позиции:
        if(DOCS_WITH_PRODUCT_SUMPRICE.contains(parentTablename)){
            // таблица родителя входит в таблицы, содержащие товарные позиции, значит
            // содержащий товары родитель находится на 1 ступень выше.
            LinkedDocsJSON info = new LinkedDocsJSON();
            info.setId(parentDocId);
            info.setTablename(parentTablename);
            return info;
        } else {// содержащий товары родитель находится на 2 ступени выше.
            try {
                Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
                // узнаем id группы связанных документов (по родителю с 1й ступени):
                Long groupId = getLinkedGroupId(myMasterId, parentTablename, parentDocId);
                if(Objects.isNull(groupId) || groupId==0L) return null;

                // узнаем UID родителя 1й ступени, чтобы узнать, какой UID на него ссылается в таблице linked_docs_links
                String childParentDocUid = getUidById(parentTablename, parentDocId);
                if (Objects.isNull(childParentDocUid)) return null;

                // загрузим список ссылок по данной группе связанных документов
                List<LinkedDocsLinksJSON> links = getLinks(groupId);
                if (Objects.isNull(links)) return null;

                // прочешем список и найдем UIDы документов, которые ссылаются на родителя 1й ступени (в 99% это будет 1 документ, но все же..)
                List<String> secondLevelParentUids = new ArrayList<>();
                for(LinkedDocsLinksJSON linkRow:links){
                    if(linkRow.getUid_to().equals(childParentDocUid)) secondLevelParentUids.add(linkRow.getUid_from());
                }
                if (secondLevelParentUids.isEmpty()) return null;

                // загрузим информацию (базовую - только id, uid и tablename) по документам данной группы связанных документов (groupId)
                List<LinkedDocsJSON> baseList = getBaseInfoOfLinkedDocs(groupId);
                if (Objects.isNull(baseList)) return null;

                // прочешем этот список, и оставим в нем только документы с UID которые есть в secondLevelParentUids,
                // и таблицами из списка DOCS_WITH_PRODUCT_SUMPRICE
                baseList.removeIf(e -> !DOCS_WITH_PRODUCT_SUMPRICE.contains(e.getTablename()) || !secondLevelParentUids.contains(e.getUid()));
//                Iterator<LinkedDocsJSON> iterator = baseList.iterator();
//                while (iterator.hasNext()) {
//                    LinkedDocsJSON e = iterator.next();
//                    if (!DOCS_WITH_PRODUCT_SUMPRICE.contains(e.getTablename()) || !secondLevelParentUids.contains(e.getUid())) {
//                        iterator.remove();
//                    }
//                }
                // если от списка ничего не осталось (маловероятно но вдруг) - это значит ничего не нашли
                if(baseList.isEmpty()) return null;
                // если осталось - это то что нужно. На случай - если осталось более 1 элемента (маловероятно очень) - просто возьмётся 1й
                return baseList.get(0);

            } catch (Exception e) {
                logger.error("Exception in method getParentDocWithProducts.", e);
                e.printStackTrace();
                return null;
            }

        }
    }




}
