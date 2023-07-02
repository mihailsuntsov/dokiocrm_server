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

package com.dokio.controller;

import com.dokio.message.request.SearchForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.request.UserGroupForm;
import com.dokio.message.response.UserGroupJSON;
import com.dokio.message.response.UserGroupListJSON;
import com.dokio.message.response.UserGroupTableJSON;
import com.dokio.model.Documents;
import com.dokio.repository.DepartmentRepositoryJPA;
import com.dokio.repository.UserGroupRepositoryJPA;
import com.dokio.repository.DocumentsRepositoryJPA;
import com.dokio.repository.UserRepository;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.repository.CompanyRepositoryJPA;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


@Controller
public class UserGroupController {
    Logger logger = Logger.getLogger("UserGroupController");

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    DocumentsRepositoryJPA documentsRepositoryJPA;
    @Autowired
    DepartmentRepositoryJPA departmentRepositoryJPA;
    @Autowired
    UserDetailsServiceImpl userRepository2;
    @Autowired
    UserGroupRepositoryJPA userGroupRepositoryJPA;


    @PostMapping("/api/auth/getDocumentsWithPermissionList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getDocumentsWithPermissionList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path api/auth/getDocumentsWithPermissionList: " + searchRequest.toString());
        String searchString = searchRequest.getSearchString();
        try {return new ResponseEntity<>(documentsRepositoryJPA.getDocumentsWithPermissionList(searchString), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();return new ResponseEntity<>("Error documents and permissions requesting", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/getUserGroupTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getUserGroupTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path api/auth/getUserGroupTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
//        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
//        int companyId;//по какому предприятию показывать группы/ 0 - по всем
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<UserGroupTableJSON> returnList;

        if (searchRequest.getSortColumn() != null && !searchRequest.getSortColumn().isEmpty() && searchRequest.getSortColumn().trim().length() > 0) {
            sortAsc = searchRequest.getSortAsc();// если SortColumn определена, значит и sortAsc есть.
        } else {
            sortColumn = "name";
            sortAsc = "asc";
        }
        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;
        }
//        if (searchRequest.getCompanyId() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
//            companyId = Integer.parseInt(searchRequest.getCompanyId());
//        } else {
//            companyId = 0;
//        }
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;
        }
        int offsetreal = offset * result;//создана переменная с номером страницы
        returnList = userGroupRepositoryJPA.getUserGroupTable(result, offsetreal, searchString, sortColumn, sortAsc, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getUserGroupPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getUserGroupPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path api/auth/getUserGroupPagesList: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
//        int companyId;//по какому предприятию показывать отделения/ 0 - по всем
//        int disabledLINK;// номер страницы на паджинейшене, на которой мы сейчас. Изначально это 1.
        String searchString = searchRequest.getSearchString();
//        companyId = Integer.parseInt(searchRequest.getCompanyId());
//        String sortColumn = searchRequest.getSortColumn();

        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;}
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;}
        pagenum = offset + 1;
        int size = userGroupRepositoryJPA.getUserGroupSize(searchString, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
//        int offsetreal = offset * result;//создана переменная с номером страницы
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
        ResponseEntity<List> responseEntity = new ResponseEntity<>(pageList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/updateUserGroup")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateUserGroup(@RequestBody UserGroupForm request){
        logger.info("Processing post request for path api/auth/updateUserGroup: " + request.toString());

        if(userGroupRepositoryJPA.updateUserGroup(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }


    @PostMapping("/api/auth/insertUserGroup")
    public  ResponseEntity<?> insertUserGroup(@RequestBody UserGroupForm request) {
        logger.info("Processing post request for path /api/auth/insertUserGroup: " + request.toString());
        try {return new ResponseEntity<>(userGroupRepositoryJPA.insertUserGroup(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller insertUserGroup error", e);
            return new ResponseEntity<>("Error of creating", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/getUserGroupValuesById")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getUserGroupValuesById(@RequestBody UserGroupForm request) {
        logger.info("Processing post request for path api/auth/getUserGroupValuesById: " + request.toString());

        UserGroupJSON usergroup;
        int id = request.getId();
        usergroup=userGroupRepositoryJPA.getUserGroupValuesById(id);//результат запроса помещается в экземпляр класса
        // нужен try catch т.к. если usergroup возвратит null, то usergroup.setUserGroupPe... вызовет NullPointerException
        try
        {
            List<Integer> permListId =userGroupRepositoryJPA.getUserGroupPermissionsIdsByUserGroupId(id);
            usergroup.setUserGroupPermissionsId(permListId);

            ResponseEntity<UserGroupJSON> responseEntity = new ResponseEntity<>(usergroup, HttpStatus.OK);
            return responseEntity;
        }
        catch(NullPointerException npe){return null;}
    }

    @RequestMapping(value = "/api/auth/getUserGroupList",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")//возвращает список групп пользователей (ролей)
    public ResponseEntity<?> getUserGroupList() {
        logger.info("Processing get request for path api/auth/getUserGroupList");
        try {return new ResponseEntity<>(userGroupRepositoryJPA.getUserGroupList(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Controller getUserGroupList error", e);
            return new ResponseEntity<>("Error query of getting roles list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/deleteUserGroups")
    public  ResponseEntity<?> deleteUserGroups(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteUserGroups: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(userGroupRepositoryJPA.deleteUserGroups(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller deleteUserGroups error", e);
            return new ResponseEntity<>("Error of deleting", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/undeleteUserGroups")
    public  ResponseEntity<?> undeleteUserGroups(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteUserGroups: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        try {return new ResponseEntity<>(userGroupRepositoryJPA.undeleteUserGroups(checked), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller undeleteUserGroups error", e);
            return new ResponseEntity<>("Error of recovering", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
