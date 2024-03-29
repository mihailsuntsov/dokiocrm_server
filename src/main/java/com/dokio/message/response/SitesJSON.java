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

package com.dokio.message.response;

import java.util.Comparator;

public class SitesJSON implements Comparable<SitesJSON> {

    private Long        id;
    private String      name;
    private String      description;
    private String      domain;
    private Boolean     stopped;
    private Boolean     domain_associated;
    private Boolean     is_archive;
    // эти не выводятся в таблицу меню, по ним нет сортировки (comparable)
    private String      master;
    private String      creator;
    private String      changer;
    private String      company;
    private Long        master_id;
    private Long        creator_id;
    private Long        changer_id;
    private Long        company_id;
    private String      date_time_created;
    private String      date_time_changed;

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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Boolean getStopped() {
        return stopped;
    }

    public void setStopped(Boolean stopped) {
        this.stopped = stopped;
    }

    public Boolean getDomain_associated() {
        return domain_associated;
    }

    public void setDomain_associated(Boolean domain_associated) {
        this.domain_associated = domain_associated;
    }

    public Boolean getIs_archive() {
        return is_archive;
    }

    public void setIs_archive(Boolean is_archive) {
        this.is_archive = is_archive;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getChanger() {
        return changer;
    }

    public void setChanger(String changer) {
        this.changer = changer;
    }

    public Long getMaster_id() {
        return master_id;
    }

    public void setMaster_id(Long master_id) {
        this.master_id = master_id;
    }

    public Long getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(Long creator_id) {
        this.creator_id = creator_id;
    }

    public Long getChanger_id() {
        return changer_id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setChanger_id(Long changer_id) {
        this.changer_id = changer_id;
    }

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public String getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(String date_time_created) {
        this.date_time_created = date_time_created;
    }

    public String getDate_time_changed() {
        return date_time_changed;
    }

    public void setDate_time_changed(String date_time_changed) {
        this.date_time_changed = date_time_changed;
    }

    @Override
    public int compareTo(SitesJSON o) {
        return 0;
    }

    public static Comparator<SitesJSON> COMPARE_BY_NAME_ASC= new Comparator<SitesJSON>() {
        public int compare(SitesJSON one, SitesJSON other) {
            return one.name.compareTo(other.name);
        }
    };
    public static Comparator<SitesJSON> COMPARE_BY_NAME_DESC= new Comparator<SitesJSON>() {
        public int compare(SitesJSON one, SitesJSON other) {
            return other.name.compareTo(one.name);
        }
    };
    public static Comparator<SitesJSON> COMPARE_BY_DESCRIPTION_ASC= new Comparator<SitesJSON>() {
        public int compare(SitesJSON one, SitesJSON other) {
            return one.description.compareTo(other.description);
        }
    };
    public static Comparator<SitesJSON> COMPARE_BY_DESCRIPTION_DESC= new Comparator<SitesJSON>() {
        public int compare(SitesJSON one, SitesJSON other) {
            return other.description.compareTo(one.description);
        }
    };
    public static Comparator<SitesJSON> COMPARE_BY_DOMAIN_ASC= new Comparator<SitesJSON>() {
        public int compare(SitesJSON one, SitesJSON other) {
            return one.domain.compareTo(other.domain);
        }
    };
    public static Comparator<SitesJSON> COMPARE_BY_DOMAIN_DESC= new Comparator<SitesJSON>() {
        public int compare(SitesJSON one, SitesJSON other) {
            return other.domain.compareTo(one.domain);
        }
    };
    public static Comparator<SitesJSON> COMPARE_BY_STOPPED_ASC = new Comparator<SitesJSON>() {
        public int compare(SitesJSON one, SitesJSON other) {
            return one.stopped.compareTo(other.stopped);
        }
    };
    public static Comparator<SitesJSON> COMPARE_BY_STOPPED_DESC = new Comparator<SitesJSON>() {
        public int compare(SitesJSON one, SitesJSON other) {
            return other.stopped.compareTo(one.stopped);
        }
    };
    public static Comparator<SitesJSON> COMPARE_BY_DOMAIN_ASSOCIATED_ASC = new Comparator<SitesJSON>() {
        public int compare(SitesJSON one, SitesJSON other) {
            return one.domain_associated.compareTo(other.domain_associated);
        }
    };
    public static Comparator<SitesJSON> COMPARE_BY_DOMAIN_ASSOCIATED_DESC = new Comparator<SitesJSON>() {
        public int compare(SitesJSON one, SitesJSON other) {
            return other.domain_associated.compareTo(one.domain_associated);
        }
    };

    @Override
    public String toString() {
        return "SitesJSON: id=" + this.id + ", name=" + this.name;
    }
}
