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

package com.dokio.model;
import com.dokio.model.Geo.Cities;
import com.dokio.model.Geo.Countries;
import com.dokio.model.Geo.Regions;
import com.dokio.model.Sprav.SpravStatusDocs;
import com.dokio.model.Sprav.SpravTypePrices;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.dokio.model.Sprav.SpravSysOPF;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="cagents")
public class Cagents {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="cagents_id_seq", sequenceName="cagents_id_seq", allocationSize=1)
    @GeneratedValue(generator="cagents_id_seq")
    private Long id;

    @Size(max = 512)
    @Column(name = "name")
    private String name;

    @Size(max = 2048)
    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "opf_id")
    private SpravSysOPF cagentOpf;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Companies company;

    @ManyToOne
    @JoinColumn(name = "master_id", nullable = false)
    private User master;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne
    @JoinColumn(name = "changer_id")
    private User changer;

    @Column(name="date_time_created", nullable = false)
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Timestamp date_time_created;

    @Column(name="date_time_changed")
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Timestamp date_time_changed;

    @Column(name = "is_deleted")//Удалён
    private Boolean is_deleted;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "cagent_cagentcategories",
            joinColumns = @JoinColumn(name = "cagent_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<CagentCategories> cagentCategories = new HashSet<>();

    @JsonIgnore// Если контрагент - поставщик товара
    @ManyToMany(mappedBy = "cagents")
    private Set<Products> products;

// Апдейт Контрагентов:

    @Column(name = "code")
    @Size(max = 30)
    private String code;

    @Column(name = "telephone")
    @Size(max = 60)
    private String telephone;

    @Column(name = "site")
    @Size(max = 120)
    private String site;


    @Column(name = "email")
    @Size(max = 254)
    private String email;

    @Column(name = "zip_code")
    @Size(max = 40)
    private String zip_code;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Countries country;

    @ManyToOne
    @JoinColumn(name = "region_id")
    private Regions region;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private Cities city;

    @Column(name = "street")
    @Size(max = 128)
    private String street;

    @Column(name = "home")
    @Size(max = 16)
    private String home;

    @Column(name = "flat")
    @Size(max = 8)
    private String flat;

    @Column(name = "additional_address")
    @Size(max = 240)
    private String additional_address;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private SpravStatusDocs status;

    @ManyToOne
    @JoinColumn(name = "price_type_id")
    private SpravTypePrices priceType;//основной тип цены, назначенный для этого контрагента

    @Column(name = "discount_card")
    @Size(max = 30)
    private String discount_card;

    @Column(name = "jr_jur_full_name")
    @Size(max = 512)
    private String jr_jur_full_name;

    @Column(name = "jr_jur_kpp")
    private String jr_jur_kpp;

    @Column(name = "jr_jur_ogrn")
    private String jr_jur_ogrn;

    @Column(name = "jr_zip_code")
    @Size(max = 40)
    private String jr_zip_code;

    @ManyToOne
    @JoinColumn(name = "jr_country_id")
    private Countries jr_country;

    @ManyToOne
    @JoinColumn(name = "jr_region_id")
    private Regions jr_region;

    @ManyToOne
    @JoinColumn(name = "jr_city_id")
    private Cities jr_city;

    @Column(name = "jr_street")
    @Size(max = 128)
    private String jr_street;

    @Column(name = "jr_home")
    @Size(max = 16)
    private String jr_home;

    @Column(name = "jr_flat")
    @Size(max = 8)
    private String jr_flat;

    @Column(name = "jr_additional_address")
    @Size(max = 240)
    private String jr_additional_address;

    @Column(name = "jr_inn")
    private String jr_inn;

    @Column(name = "jr_okpo")
    private String jr_okpo;

    @Column(name = "jr_fio_family")
    @Size(max = 127)
    private String jr_fio_family;

    @Column(name = "jr_fio_name")
    @Size(max = 127)
    private String jr_fio_name;

    @Column(name = "jr_fio_otchestvo")
    @Size(max = 127)
    private String jr_fio_otchestvo;

    @Column(name = "jr_ip_ogrnip")
    private String jr_ip_ogrnip;

    @Column(name = "jr_ip_svid_num")
    @Size(max = 30)
    private String jr_ip_svid_num;

    @Column(name="jr_ip_reg_date")
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)// Дата регистрации ИП
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Date jr_ip_reg_date;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SpravSysOPF getCagentOpf() {
        return cagentOpf;
    }

    public void setCagentOpf(SpravSysOPF cagentOpf) {
        this.cagentOpf = cagentOpf;
    }

    public Companies getCompany() {
        return company;
    }

    public void setCompany(Companies company) {
        this.company = company;
    }

    public User getMaster() {
        return master;
    }

    public void setMaster(User master) {
        this.master = master;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public User getChanger() {
        return changer;
    }

    public void setChanger(User changer) {
        this.changer = changer;
    }

    public Timestamp getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(Timestamp date_time_created) {
        this.date_time_created = date_time_created;
    }

    public Timestamp getDate_time_changed() {
        return date_time_changed;
    }

    public void setDate_time_changed(Timestamp date_time_changed) {
        this.date_time_changed = date_time_changed;
    }

    public Boolean getIs_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(Boolean is_deleted) {
        this.is_deleted = is_deleted;
    }

    public Set<CagentCategories> getCagentCategories() {
        return cagentCategories;
    }

    public void setCagentCategories(Set<CagentCategories> cagentCategories) {
        this.cagentCategories = cagentCategories;
    }

    public Set<Products> getProducts() {
        return products;
    }

    public void setProducts(Set<Products> products) {
        this.products = products;
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

    public Countries getCountry() {
        return country;
    }

    public void setCountry(Countries country) {
        this.country = country;
    }

    public Regions getRegion() {
        return region;
    }

    public void setRegion(Regions region) {
        this.region = region;
    }

    public Cities getCity() {
        return city;
    }

    public void setCity(Cities city) {
        this.city = city;
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

    public SpravStatusDocs getStatus() {
        return status;
    }

    public void setStatus(SpravStatusDocs status) {
        this.status = status;
    }

    public SpravTypePrices getPriceType() {
        return priceType;
    }

    public void setPriceType(SpravTypePrices priceType) {
        this.priceType = priceType;
    }

    public String getDiscount_card() {
        return discount_card;
    }

    public void setDiscount_card(String discount_card) {
        this.discount_card = discount_card;
    }

    public String getJr_jur_full_name() {
        return jr_jur_full_name;
    }

    public void setJr_jur_full_name(String jr_jur_full_name) {
        this.jr_jur_full_name = jr_jur_full_name;
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

    public String getJr_zip_code() {
        return jr_zip_code;
    }

    public void setJr_zip_code(String jr_zip_code) {
        this.jr_zip_code = jr_zip_code;
    }

    public Countries getJr_country() {
        return jr_country;
    }

    public void setJr_country(Countries jr_country) {
        this.jr_country = jr_country;
    }

    public Regions getJr_region() {
        return jr_region;
    }

    public void setJr_region(Regions jr_region) {
        this.jr_region = jr_region;
    }

    public Cities getJr_city() {
        return jr_city;
    }

    public void setJr_city(Cities jr_city) {
        this.jr_city = jr_city;
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

    public String getJr_ip_ogrnip() {
        return jr_ip_ogrnip;
    }

    public void setJr_ip_ogrnip(String jr_ip_ogrnip) {
        this.jr_ip_ogrnip = jr_ip_ogrnip;
    }

    public String getJr_ip_svid_num() {
        return jr_ip_svid_num;
    }

    public void setJr_ip_svid_num(String jr_ip_svid_num) {
        this.jr_ip_svid_num = jr_ip_svid_num;
    }

    public Date getJr_ip_reg_date() {
        return jr_ip_reg_date;
    }

    public void setJr_ip_reg_date(Date jr_ip_reg_date) {
        this.jr_ip_reg_date = jr_ip_reg_date;
    }
}
