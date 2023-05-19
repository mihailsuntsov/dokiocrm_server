package com.dokio.repository.store.woo.v3;
import com.dokio.message.request.CagentsForm;
import com.dokio.message.request.CustomersOrdersProductTableForm;
import com.dokio.message.request.store.woo.v3.orders.OrderForm;
import com.dokio.message.request.store.woo.v3.orders.OrdersForm;
import com.dokio.message.request.store.woo.v3.orders.ProductForm;
import com.dokio.message.response.ProductsJSON;
import com.dokio.message.response.Settings.CompanySettingsJSON;
import com.dokio.message.response.Sprav.StoresJSON;
import com.dokio.repository.CagentRepositoryJPA;
import com.dokio.repository.Exceptions.CantInsertProductRowCauseErrorException;
import com.dokio.repository.Exceptions.StoreDefaultCustomerIsNotSet;
import com.dokio.repository.Exceptions.StoreDepartmentIsNotSet;
import com.dokio.repository.Exceptions.WrongCrmSecretKeyException;
import com.dokio.repository.ProductsRepositoryJPA;
import com.dokio.repository.StoreRepository;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import java.util.UUID;
import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

@Repository
public class StoreOrdersRepository {

    private Logger logger = Logger.getLogger(StoreOrdersRepository.class);

    @Value("${apiserver.host}")
    private String apiserver;

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    CommonUtilites cu;
    @Autowired
    CagentRepositoryJPA cagentRepository;
    @Autowired
    ProductsRepositoryJPA productsRepository;
    @Autowired
    StoreRepository storeRepository;

    public String getLastSynchronizedOrderTime(String key) {
        String stringQuery="";
        try {
            Long companyId = Long.valueOf(cu.getByCrmSecretKey("company_id",key).toString());
            Long storeId = Long.valueOf(cu.getByCrmSecretKey("id",key).toString());

            stringQuery=" select p.woo_gmt_date from customers_orders p  " +
                        " where p.company_id = " + companyId +
                        " and coalesce(p.is_deleted, false) = false " +
                        " and p.store_id = " + storeId +
                        " and p.woo_gmt_date is not null " +
                        " order by p.woo_gmt_date desc limit 1";
            Query query = entityManager.createNativeQuery(stringQuery);
            String result = (String)query.getSingleResult();
            if(Objects.isNull(result)) result="2000-01-01T00:00:00";
            return result;
        } catch (NoResultException nre) {
            return "2000-01-01T00:00:00";
        } catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreOrdersRepository/getLastSynchronizedOrderTime. Key:"+key, e);
            e.printStackTrace();
            return "-200";
        } catch (Exception e) {
            logger.error("Exception in method woo/v3/StoreOrdersRepository/getLastSynchronizedOrderTime. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class, CantInsertProductRowCauseErrorException.class,StoreDefaultCustomerIsNotSet.class,StoreDepartmentIsNotSet.class})
    public Integer putOrdersIntoCRM(OrdersForm request){
        String stringQuery = "";
        try {
            Long companyId = Long.valueOf(cu.getByCrmSecretKey("company_id",request.getCrmSecretKey()).toString());
            Long masterId = Long.valueOf(cu.getByCrmSecretKey("master_id",request.getCrmSecretKey()).toString());
            Long storeId = Long.valueOf(cu.getByCrmSecretKey("id",request.getCrmSecretKey()).toString());

            StoresJSON settings = storeRepository.getStoreBaseValues(storeId);

            if (Objects.isNull(settings.getStore_orders_department_id()))
                throw new StoreDepartmentIsNotSet();
            if (settings.getStore_if_customer_not_found().equals("use_default") && Objects.isNull(settings.getStore_default_customer_id()))
                throw new StoreDefaultCustomerIsNotSet();
            if (Objects.isNull(settings.getStore_default_creator_id()))
                settings.setStore_default_creator_id(masterId);
            for (OrderForm row : request.getOrders()) {
                insertOrder(row, masterId, companyId, storeId, settings);
            }
            return 1;
        }catch (StoreDefaultCustomerIsNotSet e) {
            logger.error("StoreDefaultCustomerIsNotSet in method woo/v3/StoreOrdersRepository/putOrdersIntoCRM. ", e);
            e.printStackTrace();
            return -222;
        }catch (StoreDepartmentIsNotSet e) {
            logger.error("StoreDepartmentIsNotSet in method woo/v3/StoreOrdersRepository/putOrdersIntoCRM. ", e);
            e.printStackTrace();
            return -220;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreOrdersRepository/putOrdersIntoCRM. Key:"+request.getCrmSecretKey(), e);
            e.printStackTrace();
            return -200;
        }catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Exception in method woo/v3/StoreOrdersRepository/putOrdersIntoCRM. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    private void insertOrder(OrderForm row, Long masterId, Long companyId, Long storeId, StoresJSON settings) throws Exception {
        Long    store_orders_department_id  = settings.getStore_orders_department_id();
        String  store_if_customer_not_found = settings.getStore_if_customer_not_found();
        Long    store_default_customer_id   = settings.getStore_default_customer_id();
        Long    store_default_creator_id    = settings.getStore_default_creator_id();
        int     store_days_for_esd          = settings.getStore_days_for_esd();
        String  customerTelephone = row.getBilling().getPhone().replace(" ","");
        String  customerEmail = row.getBilling().getEmail().replace(" ","");
        Integer customerWooId = row.getCustomer_id();
        CompanySettingsJSON companySettings = cu.getCompanySettings(companyId);
        boolean vat = companySettings.isVat();
        boolean vatIncluded = companySettings.isVat_included();

        try{
            Long customerId = cagentRepository.getCustomerIdByStoreCustomerData(companyId,customerWooId,customerEmail,customerTelephone);

            //генерируем номер документа
            Long doc_number=cu.generateDocNumberCode(companyId,"customers_orders",masterId);
            if (
               Objects.isNull(customerId) &&
                       (
                       store_if_customer_not_found.equals("create_new") ||
                       (store_if_customer_not_found.equals("use_default") && Objects.isNull(store_default_customer_id))
                )
            )
            {
                try{
                    CagentsForm cagentForm = new CagentsForm();
                    cagentForm.setName(row.getBilling().getFirst_name());
                    cagentForm.setCompany_id(companyId);
                    cagentForm.setOpf_id(2);//ставим по-умолчанию Физ. лицо
                    cagentForm.setStatus_id(cu.getDocumentsDefaultStatus(companyId,12));
                    cagentForm.setDescription(row.getBilling().getAddress_1());
                    cagentForm.setPrice_type_id(cu.getPriceTypeDefault(companyId));
                    cagentForm.setTelephone(row.getBilling().getPhone());
                    cagentForm.setEmail((row.getBilling().getEmail()));
                    cagentForm.setZip_code("");
                    cagentForm.setCountry_id(null);
                    cagentForm.setRegion("");
                    cagentForm.setCity("");
                    cagentForm.setStreet("");
                    cagentForm.setHome("");
                    cagentForm.setFlat("");
                    cagentForm.setAdditional_address(row.getBilling().getAddress_1());
                    customerId=(cagentRepository.insertCagentBaseFields(cagentForm, masterId));
                    if(Objects.isNull(customerId))
                        throw new Exception("Can't create counterparty when calling cagentRepository.insertCagentBaseFields(cagentForm, masterId) ");
                }
                catch (Exception e) {
                    logger.error("Exception in method insertCustomersOrders on creating Counterparty (customer of the store order).", e);
                    e.printStackTrace();
                    throw new Exception();
                }
            } else
                if(Objects.isNull(customerId) && !Objects.isNull(store_default_customer_id))
                        customerId = store_default_customer_id;

            String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            String stringQuery =
                " insert into customers_orders (" +
                " master_id," + //мастер-аккаунт
                " creator_id," + //создатель
                " company_id," + //предприятие, для которого создается документ
                " department_id," + //отделение, из(для) которого создается документ
                " cagent_id," +//контрагент
                " date_time_created," + //дата и время создания
                " doc_number," + //номер заказа
                " name," + //наименование заказа
                " description," +//доп. информация по заказу
                " shipment_date," +//план. дата отгрузки
                " nds," +// НДС
                " nds_included," +// НДС включен в цену
                " telephone,"+//телефон
                " email,"+//емейл
                " zip_code,"+// почтовый индекс
                " country_id,"+//страна
                " region,"+//область
                " city,"+//город/нас.пункт
                " street,"+//улица
                " home,"+//дом
                " flat,"+//квартира
                " additional_address,"+//дополнение к адресу
                " track_number," + //трек-номер отправленного заказа
                " status_id,"+//статус заказа
                " uid,"+// уникальный идентификатор документа
                " store_id, " + // id интернет-магазина, из которого поступил заказ
                " woo_gmt_date" + // дата в текстовом виде в формате ISO8601: YYYY-MM-DDTHH:MM:SS
                ") values ("+
                masterId + ", "+//мастер-аккаунт
                store_default_creator_id + ", "+ //создатель
                companyId + ", "+//предприятие, для которого создается документ
                store_orders_department_id + ", "+//отделение, из(для) которого создается документ
                customerId + ", "+//контрагент
                "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                doc_number + ", "+//номер заказа
                "'WooCommerce order #"+doc_number.toString()+"', " +//наименование
                "CONCAT(:description,' ',(select name from stores where id="+storeId+")), " +//описание
                "now() + interval '"+store_days_for_esd+"' day, " +// план. дата и время отгрузки
                vat + ", "+// НДС
                vatIncluded + ", "+// НДС включен в цену
                ":telephone, " +//телефон
                ":email, " +//емейл
                ":zip_code, " +//почтовый индекс
                null + ", " +//страна
                ":region, " +//область
                ":city, " +//город/нас.пункт
                ":street, " +//улица
                ":home, " +//дом
                ":flat, " +//квартира
                ":additional_address, " +//дополнение к адресу
                ":track_number, " +//трек-номер отправленного заказа
                cu.getDocumentsDefaultStatus(companyId,23) + "," +//статус заказа
                "'"+UUID.randomUUID().toString()+"',"+// уникальный идентификатор документа
                storeId + ", " + // id интернет-магазина, из которого поступил заказ
                ":woo_gmt_date"+
                ")";

            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("description",row.getBilling().getAddress_1());
                query.setParameter("telephone",(row.getBilling().getPhone() == null ? "": row.getBilling().getPhone()));
                query.setParameter("email",(row.getBilling().getEmail() == null ? "": row.getBilling().getEmail()));
                query.setParameter("zip_code",(row.getBilling().getPostcode() == null ? "": row.getBilling().getPostcode()));
                query.setParameter("region",(row.getBilling().getState() == null ? "": row.getBilling().getState()));
                query.setParameter("city",(row.getBilling().getCity() == null ? "": row.getBilling().getCity()));
                query.setParameter("street",(row.getBilling().getAddress_1() == null ? "": row.getBilling().getAddress_1()));
                query.setParameter("home",(row.getBilling().getAddress_2() == null ? "": row.getBilling().getAddress_2()));
                query.setParameter("flat",(""));
                query.setParameter("additional_address",(""));
                query.setParameter("track_number",(""));
                query.setParameter("woo_gmt_date", row.getDate_created_gmt());
                query.executeUpdate();
                stringQuery="select id from customers_orders where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and doc_number = "+doc_number+" and creator_id="+store_default_creator_id;
                Query query2 = entityManager.createNativeQuery(stringQuery);
                Long newDocId=Long.valueOf(query2.getSingleResult().toString());
                //сохранение таблицы товаров
                insertCustomersOrdersProducts(row.getLine_items(), newDocId, masterId, companyId, store_orders_department_id, storeId, settings);

            } catch (CantInsertProductRowCauseErrorException e) {
                logger.error("Exception in method insertCustomersOrders on inserting into customers_orders_products. ", e);
                e.printStackTrace();
                throw new Exception();
            } catch (Exception e) {
                logger.error("Exception in method insertCustomersOrders on querying of created document id. SQL query:"+stringQuery, e);
                e.printStackTrace();
                throw new Exception();
            }
        } catch (Exception e) {
            logger.error("Exception in method insertCustomersOrders on inserting into customers_orders.", e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    //сохранение таблицы товаров
    private void insertCustomersOrdersProducts(List<ProductForm> productsList, Long parentDocId, Long myMasterId, Long companyId, Long departmentId, Long storeId, StoresJSON settings) throws Exception {
        if (productsList!=null && productsList.size() > 0) {//если есть что сохранять

            boolean reserve = settings.getStore_auto_reserve();
            boolean productIsVariation = false;
            Set<Long> productsIdsToSyncWoo = new HashSet<>(); // Set IDs of products that will have reserves and need to be synchronised,
                                                              // as a reserve is decrease available quantity of product in department

            try{

                for (ProductForm wooProductRow : productsList) {
                    CustomersOrdersProductTableForm crmProductRow = new CustomersOrdersProductTableForm();
                    productIsVariation = false;
                    if(wooProductRow.getProduct_id()>0){ // if there is woo_id (woo_id can be = 0 if product that there is in a order was manually deleted in WooCommerce)
                        if(wooProductRow.getVariation_id()>0) // if variation_id>0 then this is variation of variable product, and should use variation_id
                            productIsVariation=true;
                        ProductsJSON currentProductInfo = getProductInfoByWooId(productIsVariation?wooProductRow.getVariation_id():wooProductRow.getProduct_id(), companyId, storeId, productIsVariation);
                        if(!Objects.isNull(currentProductInfo)){ //if the product is found
                            crmProductRow.setProduct_id(currentProductInfo.getId());
                            crmProductRow.setCustomers_orders_id(parentDocId);
                            crmProductRow.setEdizm_id(Long.valueOf(currentProductInfo.getEdizm_id()));
                            crmProductRow.setProduct_count(new BigDecimal(wooProductRow.getQuantity()));
                            crmProductRow.setProduct_price(new BigDecimal(wooProductRow.getPrice()));
                            crmProductRow.setProduct_sumprice(crmProductRow.getProduct_price().multiply(crmProductRow.getProduct_count()).setScale(2,BigDecimal.ROUND_HALF_UP));
                            crmProductRow.setPrice_type_id(null);
                            crmProductRow.setNds_id(Long.valueOf(currentProductInfo.getNds_id()));
                            crmProductRow.setDepartment_id(departmentId);
                            crmProductRow.setProduct_price_of_type_price(new BigDecimal(0));
                            if(!reserve)
                                crmProductRow.setReserved_current(new BigDecimal(0));
                            else{// Если в настройках предприятия есть Авторезерв товаров из заказов интернет-магазина
                                // Ставим в резерв заказываемое количество товаров
                                crmProductRow.setReserved_current(crmProductRow.getProduct_count());
                                // узнаём, есть ли свободные товары
                                BigDecimal available = productsRepository.getAvailableExceptMyDoc(crmProductRow.getProduct_id(), departmentId, parentDocId);
                                // Если резерв превышает доступное кол-во товара - уменьшаем резерв до доступного количества товара на складе
                                if (crmProductRow.getReserved_current().compareTo(available) > 0) {
                                    crmProductRow.setReserved_current(available);// уменьшаем резерв до величины, равной доступному количеству товара на складе
                                }
                                // После постановки в резерв доступное количество товара уменьшается, и этот товар подлежит синхронизации с WooCommerce
                                productsIdsToSyncWoo.add(crmProductRow.getProduct_id());
                            }
                            saveCustomersOrdersProductTable(crmProductRow, companyId, myMasterId, reserve);
                        }
                    }
                }
                if(productsIdsToSyncWoo.size()>0) productsRepository.markProductsAsNeedToSyncWoo(productsIdsToSyncWoo,myMasterId);
            } catch (Exception e) {
                logger.error("Exception in method toreOrdersRepository/insertCustomersOrdersProducts", e);
                e.printStackTrace();
                throw new Exception();
            }
        }
    }

    @SuppressWarnings("Duplicates")
    private void saveCustomersOrdersProductTable(CustomersOrdersProductTableForm row, Long company_id, Long master_id, boolean reserve) throws Exception {
        String stringQuery="";
        try {
            stringQuery =
                    " insert into customers_orders_product (" +
                            "master_id, " +
                            "company_id, " +
                            "product_id, " +
                            "customers_orders_id, " +
                            "product_count, " +
                            "product_price, " +
                            "product_sumprice, " +
                            "edizm_id, " +
                            "price_type_id, " +
                            "nds_id, " +
                            "department_id, " +
                            "product_price_of_type_price, " +
                            "reserved_current " +
                            ") values (" +
                            master_id + "," +
                            company_id + "," +
                            row.getProduct_id() + "," +
                            row.getCustomers_orders_id() + "," +
                            row.getProduct_count() + "," +
                            row.getProduct_price() + "," +
                            row.getProduct_sumprice() + "," +
                            row.getEdizm_id() + "," +
                            row.getPrice_type_id() + "," +
                            row.getNds_id() + ", " +
                            row.getDepartment_id() + ", " +
                            row.getProduct_price_of_type_price() + ", " +
                            row.getReserved_current() +
                            " ) " +
                            "ON CONFLICT ON CONSTRAINT customers_orders_product_uq " +// "upsert"
                            " DO update set " +
                            " product_id = " + row.getProduct_id() + ","+
                            " customers_orders_id = " + row.getCustomers_orders_id() + ","+
                            " product_count = " + row.getProduct_count() + ","+
                            " product_price = " + row.getProduct_price() + ","+
                            " product_sumprice = " + row.getProduct_sumprice() + ","+
                            " edizm_id = " + row.getEdizm_id() + ","+
                            " price_type_id = " + row.getPrice_type_id() + ","+
                            " nds_id = " + row.getNds_id() + ","+
                            " department_id = " + row.getDepartment_id() + ","+
                            " product_price_of_type_price = " + row.getProduct_price_of_type_price() + ","+
                            " reserved_current = " + row.getReserved_current();
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        }
        catch (Exception e) {
            logger.error("Exception in method StoreOrdersRepository/saveCustomersOrdersProductTable. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    private ProductsJSON getProductInfoByWooId(int wooId, Long companyId, Long storeId, boolean productIsVariation) throws Exception {
        String stringQuery="select " +
                " p.id as id, " +
                " p.edizm_id as edizm_id," +
                " p.nds_id as nds_id" +
                " from products p" +
                (productIsVariation?(" inner join stores_variations sp on sp.product_id=p.id"):(" inner join stores_products sp on sp.product_id=p.id")) +
                " where p.company_id="+companyId+" and sp.woo_id="+wooId+" and sp.store_id="+storeId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            ProductsJSON doc = new ProductsJSON();
            if(queryList.size()>0) {
                doc.setId(Long.parseLong( queryList.get(0)[0].toString()));
                doc.setEdizm_id((Integer) queryList.get(0)[1]);
                doc.setNds_id((Integer)   queryList.get(0)[2]);
            } else throw new NoResultException();
            return doc;
        } catch (NoResultException nre) {
            logger.error("Exception in method StoreOrdersRepository/getProductIdByWooId. Product ID is not found by Woo_id = "+wooId+", SQL query:"+stringQuery, nre);
            nre.printStackTrace();
            return null; // There is no product found by it's woo id. It can be if, for example, since the last time of orders synchronization
                         // product in the online store was manually deleted, and then resynchronized (or not resynchronized)
        }
        catch (Exception e) {
            logger.error("Exception in method StoreOrdersRepository/getProductIdByWooId. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }
}
