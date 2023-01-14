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

package com.dokio.message.request;

import java.util.List;
import java.util.Set;

public class CompaniesForm {
    private Long id;
    private Integer currency_id;
    private Integer opf_id;
    private String checked;//для удаления
    private String name;
    private String code;
    private String telephone;
    private String site;
    private String email;
    private String zip_code;
    private Integer country_id;
    private Integer region_id;
    private Integer city_id;
    private String street;
    private String home;
    private String flat;
    private String additional_address;
    private Long status_id;
    private String jr_jur_full_name;
    private String jr_jur_kpp;
    private String jr_jur_ogrn;
    private String jr_zip_code;
    private Integer jr_country_id;
    private Integer jr_region_id;
    private Integer jr_city_id;
    private String jr_street;
    private String jr_home;
    private String jr_flat;
    private String jr_additional_address;
    private String jr_inn;
    private String jr_okpo;
    private String jr_fio_family;
    private String jr_fio_name;
    private String jr_fio_otchestvo;
    private String jr_ip_ogrnip;
    private String jr_ip_svid_num; // string т.к. оно может быть типа "серия 77 №42343232"
    private String jr_ip_reg_date;
    private Set<CompaniesPaymentAccountsForm> companiesPaymentAccountsTable;//банковские счета
    private Boolean nds_payer;
    private String fio_director;
    private String director_position;
    private String fio_glavbuh;
    private Long director_signature_id;
    private Long glavbuh_signature_id;
    private Long stamp_id;
    private Long card_template_id;
    // Settings
    private Integer st_prefix_barcode_pieced;   // prefix of barcode for pieced product
    private Integer st_prefix_barcode_packed;   // prefix of barcode for packed product
    private String  st_netcost_policy;          // policy of netcost calculation by all company or by each department separately

    private String region;
    private String city;
    private String jr_region;
    private String jr_city;

    private String type;                        // entity or individual
    private String legal_form;
//    private Integer reg_country_id;             // country of registration
//    private String tax_number;                  // tax number assigned to the taxpayer in the country of registration (like INN in Russia)
//    private String reg_number;                  // registration number assigned to the taxpayer in the country of registration (like OGRN or OGRNIP in Russia)

    private Boolean is_store;           // on off the store
    private String  store_site_address;  // e.g. http://localhost/DokioShop
    private String  store_key;           // consumer key
    private String  store_secret;        // consumer secret
    private String  store_type;          // e.g. woo
    private String  store_api_version;   // e.g. v3
    private String  crm_secret_key;      // like UUID generated
    private Long    store_price_type_regular;    // id of regular type price
    private Long    store_price_type_sale;       // id of sale type price

    private Boolean nds_included;                // used with nds_payer as default values for Customers orders fields "Tax" and "Tax included"
    private Long    store_orders_department_id;  // department for creation Customer order from store
    private String  store_if_customer_not_found; // "create_new" or "use_default". Default is "create_new"
    private Long    store_default_customer_id;   // counterparty id if store_if_customer_not_found=use_default
    private Long    store_default_creator_id;    // default user that will be marked as a creator of store order. Default is master user
    private Integer store_days_for_esd;          // number of days for ESD of created store order. Default is 0
    private List<Long> companyStoreDepartments;  // ID of the departments in which calculated the amount of products for the online store
    private Boolean store_auto_reserve;          // auto reserve product after getting internet store order
    private String store_ip;                     // internet-store ip address

    public String getStore_ip() {
        return store_ip;
    }

    public void setStore_ip(String store_ip) {
        this.store_ip = store_ip;
    }

    public Boolean getStore_auto_reserve() {
        return store_auto_reserve;
    }

    public void setStore_auto_reserve(Boolean store_auto_reserve) {
        this.store_auto_reserve = store_auto_reserve;
    }

    public List<Long> getCompanyStoreDepartments() {
        return companyStoreDepartments;
    }

    public void setCompanyStoreDepartments(List<Long> companyStoreDepartments) {
        this.companyStoreDepartments = companyStoreDepartments;
    }

    public Long getStore_default_creator_id() {
        return store_default_creator_id;
    }

    public void setStore_default_creator_id(Long store_default_creator_id) {
        this.store_default_creator_id = store_default_creator_id;
    }

    public Integer getStore_days_for_esd() {
        return store_days_for_esd;
    }

    public void setStore_days_for_esd(Integer store_days_for_esd) {
        this.store_days_for_esd = store_days_for_esd;
    }

    public Boolean getNds_included() {
        return nds_included;
    }

    public void setNds_included(Boolean nds_included) {
        this.nds_included = nds_included;
    }

    public Long getStore_orders_department_id() {
        return store_orders_department_id;
    }

    public void setStore_orders_department_id(Long store_orders_department_id) {
        this.store_orders_department_id = store_orders_department_id;
    }

    public String getStore_if_customer_not_found() {
        return store_if_customer_not_found;
    }

    public void setStore_if_customer_not_found(String store_if_customer_not_found) {
        this.store_if_customer_not_found = store_if_customer_not_found;
    }

    public Long getStore_default_customer_id() {
        return store_default_customer_id;
    }

    public void setStore_default_customer_id(Long store_default_customer_id) {
        this.store_default_customer_id = store_default_customer_id;
    }

    public Long getStore_price_type_regular() {
        return store_price_type_regular;
    }

    public void setStore_price_type_regular(Long store_price_type_regular) {
        this.store_price_type_regular = store_price_type_regular;
    }

    public Long getStore_price_type_sale() {
        return store_price_type_sale;
    }

    public void setStore_price_type_sale(Long store_price_type_sale) {
        this.store_price_type_sale = store_price_type_sale;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

//    public Integer getReg_country_id() {
//        return reg_country_id;
//    }
//
//    public void setReg_country_id(Integer reg_country_id) {
//        this.reg_country_id = reg_country_id;
//    }
//
//    public String getTax_number() {
//        return tax_number;
//    }
//
//    public void setTax_number(String tax_number) {
//        this.tax_number = tax_number;
//    }
//
//    public String getReg_number() {
//        return reg_number;
//    }
//
//    public void setReg_number(String reg_number) {
//        this.reg_number = reg_number;
//    }

    public String getLegal_form() {
        return legal_form;
    }

    public void setLegal_form(String legal_form) {
        this.legal_form = legal_form;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getJr_region() {
        return jr_region;
    }

    public void setJr_region(String jr_region) {
        this.jr_region = jr_region;
    }

    public String getJr_city() {
        return jr_city;
    }

    public void setJr_city(String jr_city) {
        this.jr_city = jr_city;
    }


    public Integer getSt_prefix_barcode_pieced() {
        return st_prefix_barcode_pieced;
    }

    public void setSt_prefix_barcode_pieced(Integer st_prefix_barcode_pieced) {
        this.st_prefix_barcode_pieced = st_prefix_barcode_pieced;
    }

    public Integer getSt_prefix_barcode_packed() {
        return st_prefix_barcode_packed;
    }

    public void setSt_prefix_barcode_packed(Integer st_prefix_barcode_packed) {
        this.st_prefix_barcode_packed = st_prefix_barcode_packed;
    }

    public String getSt_netcost_policy() {
        return st_netcost_policy;
    }

    public void setSt_netcost_policy(String st_netcost_policy) {
        this.st_netcost_policy = st_netcost_policy;
    }

    public Long getCard_template_id() {
        return card_template_id;
    }

    public void setCard_template_id(Long card_template_id) {
        this.card_template_id = card_template_id;
    }

    public Long getStamp_id() {
        return stamp_id;
    }

    public void setStamp_id(Long stamp_id) {
        this.stamp_id = stamp_id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCurrency_id() {
        return currency_id;
    }

    public void setCurrency_id(Integer currency_id) {
        this.currency_id = currency_id;
    }

    public Integer getOpf_id() {
        return opf_id;
    }

    public void setOpf_id(Integer opf_id) {
        this.opf_id = opf_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getChecked() {
        return checked;
    }

    public void setChecked(String checked) {
        this.checked = checked;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getZip_code() {
        return zip_code;
    }

    public void setZip_code(String zip_code) {
        this.zip_code = zip_code;
    }

    public Integer getCountry_id() {
        return country_id;
    }

    public void setCountry_id(Integer country_id) {
        this.country_id = country_id;
    }

    public Integer getRegion_id() {
        return region_id;
    }

    public void setRegion_id(Integer region_id) {
        this.region_id = region_id;
    }

    public Integer getCity_id() {
        return city_id;
    }

    public void setCity_id(Integer city_id) {
        this.city_id = city_id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getFlat() {
        return flat;
    }

    public void setFlat(String flat) {
        this.flat = flat;
    }

    public String getAdditional_address() {
        return additional_address;
    }

    public void setAdditional_address(String additional_address) {
        this.additional_address = additional_address;
    }

    public Long getStatus_id() {
        return status_id;
    }

    public void setStatus_id(Long status_id) {
        this.status_id = status_id;
    }

    public String getJr_jur_full_name() {
        return jr_jur_full_name;
    }

    public void setJr_jur_full_name(String jr_jur_full_name) {
        this.jr_jur_full_name = jr_jur_full_name;
    }

    public String getJr_zip_code() {
        return jr_zip_code;
    }

    public void setJr_zip_code(String jr_zip_code) {
        this.jr_zip_code = jr_zip_code;
    }

    public Integer getJr_country_id() {
        return jr_country_id;
    }

    public void setJr_country_id(Integer jr_country_id) {
        this.jr_country_id = jr_country_id;
    }

    public Integer getJr_region_id() {
        return jr_region_id;
    }

    public void setJr_region_id(Integer jr_region_id) {
        this.jr_region_id = jr_region_id;
    }

    public Integer getJr_city_id() {
        return jr_city_id;
    }

    public void setJr_city_id(Integer jr_city_id) {
        this.jr_city_id = jr_city_id;
    }

    public String getJr_street() {
        return jr_street;
    }

    public void setJr_street(String jr_street) {
        this.jr_street = jr_street;
    }

    public String getJr_home() {
        return jr_home;
    }

    public void setJr_home(String jr_home) {
        this.jr_home = jr_home;
    }

    public String getJr_flat() {
        return jr_flat;
    }

    public void setJr_flat(String jr_flat) {
        this.jr_flat = jr_flat;
    }

    public String getJr_additional_address() {
        return jr_additional_address;
    }

    public void setJr_additional_address(String jr_additional_address) {
        this.jr_additional_address = jr_additional_address;
    }

    public String getJr_fio_family() {
        return jr_fio_family;
    }

    public void setJr_fio_family(String jr_fio_family) {
        this.jr_fio_family = jr_fio_family;
    }

    public String getJr_fio_name() {
        return jr_fio_name;
    }

    public void setJr_fio_name(String jr_fio_name) {
        this.jr_fio_name = jr_fio_name;
    }

    public String getJr_fio_otchestvo() {
        return jr_fio_otchestvo;
    }

    public void setJr_fio_otchestvo(String jr_fio_otchestvo) {
        this.jr_fio_otchestvo = jr_fio_otchestvo;
    }

    public String getJr_ip_svid_num() {
        return jr_ip_svid_num;
    }

    public void setJr_ip_svid_num(String jr_ip_svid_num) {
        this.jr_ip_svid_num = jr_ip_svid_num;
    }

    public String getJr_ip_reg_date() {
        return jr_ip_reg_date;
    }

    public void setJr_ip_reg_date(String jr_ip_reg_date) {
        this.jr_ip_reg_date = jr_ip_reg_date;
    }

    public Set<CompaniesPaymentAccountsForm> getCompaniesPaymentAccountsTable() {
        return companiesPaymentAccountsTable;
    }

    public void setCompaniesPaymentAccountsTable(Set<CompaniesPaymentAccountsForm> companiesPaymentAccountsTable) {
        this.companiesPaymentAccountsTable = companiesPaymentAccountsTable;
    }

    public Boolean getNds_payer() {
        return nds_payer;
    }

    public void setNds_payer(Boolean nds_payer) {
        this.nds_payer = nds_payer;
    }

    public String getFio_director() {
        return fio_director;
    }

    public void setFio_director(String fio_director) {
        this.fio_director = fio_director;
    }

    public String getDirector_position() {
        return director_position;
    }

    public void setDirector_position(String director_position) {
        this.director_position = director_position;
    }

    public String getFio_glavbuh() {
        return fio_glavbuh;
    }

    public void setFio_glavbuh(String fio_glavbuh) {
        this.fio_glavbuh = fio_glavbuh;
    }

    public Long getDirector_signature_id() {
        return director_signature_id;
    }

    public void setDirector_signature_id(Long director_signature_id) {
        this.director_signature_id = director_signature_id;
    }

    public String getJr_jur_kpp() {
        return jr_jur_kpp;
    }

    public void setJr_jur_kpp(String jr_jur_kpp) {
        this.jr_jur_kpp = jr_jur_kpp;
    }

    public String getJr_jur_ogrn() {
        return jr_jur_ogrn;
    }

    public void setJr_jur_ogrn(String jr_jur_ogrn) {
        this.jr_jur_ogrn = jr_jur_ogrn;
    }

    public String getJr_inn() {
        return jr_inn;
    }

    public void setJr_inn(String jr_inn) {
        this.jr_inn = jr_inn;
    }

    public String getJr_okpo() {
        return jr_okpo;
    }

    public void setJr_okpo(String jr_okpo) {
        this.jr_okpo = jr_okpo;
    }

    public String getJr_ip_ogrnip() {
        return jr_ip_ogrnip;
    }

    public void setJr_ip_ogrnip(String jr_ip_ogrnip) {
        this.jr_ip_ogrnip = jr_ip_ogrnip;
    }

    public Long getGlavbuh_signature_id() {
        return glavbuh_signature_id;
    }

    public void setGlavbuh_signature_id(Long glavbuh_signature_id) {
        this.glavbuh_signature_id = glavbuh_signature_id;
    }

    public Boolean getIs_store() {
        return is_store;
    }

    public void setIs_store(Boolean is_store) {
        this.is_store = is_store;
    }

    public String getStore_site_address() {
        return store_site_address;
    }

    public void setStore_site_address(String store_site_address) {
        this.store_site_address = store_site_address;
    }

    public String getStore_key() {
        return store_key;
    }

    public void setStore_key(String store_key) {
        this.store_key = store_key;
    }

    public String getStore_secret() {
        return store_secret;
    }

    public void setStore_secret(String store_secret) {
        this.store_secret = store_secret;
    }

    public String getStore_type() {
        return store_type;
    }

    public void setStore_type(String store_type) {
        this.store_type = store_type;
    }

    public String getStore_api_version() {
        return store_api_version;
    }

    public void setStore_api_version(String store_api_version) {
        this.store_api_version = store_api_version;
    }

    public String getCrm_secret_key() {
        return crm_secret_key;
    }

    public void setCrm_secret_key(String crm_secret_key) {
        this.crm_secret_key = crm_secret_key;
    }

    @Override
    public String toString() {
        return "CompaniesForm: id=" + this.id + ", currency_id=" + this.currency_id +
                ", opf_id=" + this.opf_id + ", name=" + this.name + ", checked=" + this.checked;
    }
}
