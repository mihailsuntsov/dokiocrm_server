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

import com.dokio.message.request.*;
import com.dokio.message.request.Settings.SettingsWriteoffForm;
import com.dokio.message.response.Settings.SettingsWriteoffJSON;
import com.dokio.message.response.WriteoffJSON;
import com.dokio.message.response.additional.FilesWriteoffJSON;
import com.dokio.repository.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.service.StorageService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class WriteoffController {
    Logger logger = Logger.getLogger("WriteoffController");

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    UserDetailsServiceImpl userRepository2;
    @Autowired
    UserGroupRepositoryJPA userGroupRepositoryJPA;
    @Autowired
    WriteoffRepositoryJPA writeoffRepositoryJPA;
    @Autowired
    StorageService storageService;

    @PostMapping("/api/auth/getWriteoffTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getWriteoffTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path api/auth/getWriteoffTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        int departmentId;//по какому отделению показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<WriteoffJSON> returnList;

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
        if (searchRequest.getCompanyId() != null && !searchRequest.getCompanyId().isEmpty() && searchRequest.getCompanyId().trim().length() > 0) {
            companyId = Integer.parseInt(searchRequest.getCompanyId());
        } else {
            companyId = 0;
        }
        if (searchRequest.getDepartmentId() != null && !searchRequest.getDepartmentId().isEmpty() && searchRequest.getDepartmentId().trim().length() > 0) {
            departmentId = Integer.parseInt(searchRequest.getDepartmentId());
        } else {
            departmentId = 0;
        }
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;
        }
        int offsetreal = offset * result;//создана переменная с номером страницы
        returnList = writeoffRepositoryJPA.getWriteoffTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId,departmentId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getWriteoffProductTable",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getWriteoffProductTable( @RequestParam("id") Long docId) {
        logger.info("Processing get request for path /api/auth/getWriteoffProductTable with Writeoff id=" + docId.toString());
        List<WriteoffProductForm> returnList;
        try {
            returnList = writeoffRepositoryJPA.getWriteoffProductTable(docId);
            return  new ResponseEntity<>(returnList, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка при загрузке таблицы с товарами", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/getWriteoffPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getWriteoffPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path api/auth/getWriteoffPagesList: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать документы/ 0 - по всем
        int departmentId;//по какой категории товаров показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        companyId = Integer.parseInt(searchRequest.getCompanyId());
        if (searchRequest.getDepartmentId() != null && !searchRequest.getDepartmentId().isEmpty() && searchRequest.getDepartmentId().trim().length() > 0) {
            departmentId = Integer.parseInt(searchRequest.getDepartmentId());
        } else {
            departmentId = 0;}
        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;}
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;}
        pagenum = offset + 1;
        int size = writeoffRepositoryJPA.getWriteoffSize(searchString,companyId,departmentId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @PostMapping("/api/auth/insertWriteoff")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertWriteoff(@RequestBody WriteoffForm request){
        logger.info("Processing post request for path api/auth/insertWriteoff: " + request.toString());
        try {return new ResponseEntity<>(writeoffRepositoryJPA.insertWriteoff(request), HttpStatus.OK);}
        catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }

    @PostMapping("/api/auth/isWriteoffNumberUnical")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> isWriteoffNumberUnical(@RequestBody UniversalForm request) { // id1 - document_id, id2 - company_id
        logger.info("Processing post request for path api/auth/isWriteoffNumberUnical: " + request.toString());

        try {
            Boolean ret = writeoffRepositoryJPA.isWriteoffNumberUnical(request);
            return new ResponseEntity<>(ret, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            value = "/api/auth/getWriteoffValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getWriteoffValuesById(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/getWriteoffValuesById with parameters: " + "id: " + id);
        WriteoffJSON response;
        try {
            response=writeoffRepositoryJPA.getWriteoffValuesById(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            logger.error("Exception in method getWriteoffValuesById. id = " + id, e);
            e.printStackTrace();
            return new ResponseEntity<>("Error loading document values", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/updateWriteoff")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateWriteoff(@RequestBody WriteoffForm request){
        logger.info("Processing post request for path api/auth/updateWriteoff: " + request.toString());
        return new ResponseEntity<>(writeoffRepositoryJPA.updateWriteoff(request), HttpStatus.OK);//   1 = все ок,  0 = недостаточно товара на складе, null = ошибка, -1 = недостаточно прав
    }

    @PostMapping("/api/auth/deleteWriteoff")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteWriteoff(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteWriteoff: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        return new ResponseEntity<>(writeoffRepositoryJPA.deleteWriteoff(checked), HttpStatus.OK);
    }

    @PostMapping("/api/auth/undeleteWriteoff")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteWriteoff(@RequestBody SignUpForm request){
        logger.info("Processing post request for path /api/auth/undeleteWriteoff: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        return new ResponseEntity<>(writeoffRepositoryJPA.undeleteWriteoff(checked), HttpStatus.OK);
    }

    @PostMapping("/api/auth/getListOfWriteoffFiles")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getListOfWriteoffFiles(@RequestBody SearchForm request)  {
        logger.info("Processing post request for path api/auth/getListOfWriteoffFiles: " + request.toString());

        Long productId=Long.valueOf(request.getId());
        List<FilesWriteoffJSON> returnList;
        try {
            returnList = writeoffRepositoryJPA.getListOfWriteoffFiles(productId);
            return new ResponseEntity<>(returnList, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/deleteWriteoffFile")
    public ResponseEntity<?> deleteWriteoffFile(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/deleteWriteoffFile: " + request.toString());

        if(writeoffRepositoryJPA.deleteWriteoffFile(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/addFilesToWriteoff")
    public ResponseEntity<?> addFilesToWriteoff(@RequestBody UniversalForm request) {
        logger.info("Processing post request for path api/auth/addFilesToWriteoff: " + request.toString());

        if(writeoffRepositoryJPA.addFilesToWriteoff(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/saveSettingsWriteoff")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveSettingsWriteoff(@RequestBody SettingsWriteoffForm request){
        logger.info("Processing post request for path /api/auth/saveSettingsWriteoff: " + request.toString());

        if(writeoffRepositoryJPA.saveSettingsWriteoff(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка сохранения настроек для документа", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getSettingsWriteoff",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsWriteoff()
    {
        logger.info("Processing get request for path /api/auth/getSettingsWriteoff without request parameters");
        SettingsWriteoffJSON response;
        try {
            response=writeoffRepositoryJPA.getSettingsWriteoff();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка загрузки настроек для документа", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/api/auth/setWriteoffAsDecompleted")
    public ResponseEntity<?> setWriteoffAsDecompleted(@RequestBody WriteoffForm request){
        logger.info("Processing post request for path /api/auth/setWriteoffAsDecompleted: " + request.toString());
        try {return new ResponseEntity<>(writeoffRepositoryJPA.setWriteoffAsDecompleted(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller setWriteoffAsDecompleted error", e);
            return new ResponseEntity<>("Ошибка запроса на снятие с проведения", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
