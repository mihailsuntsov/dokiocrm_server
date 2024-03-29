/*
Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
программного обеспечения;
Эта программа распространяется в расчете на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу:
<http://www.gnu.org/licenses/>
 */
package com.dokio.controller.Sprav;

import com.dokio.message.request.SearchForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.request.Sprav.SpravSysEdizmForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.Sprav.SpravSysEdizmJSON;
import com.dokio.message.response.Sprav.SpravSysEdizmTableJSON;
import com.dokio.repository.CompanyRepositoryJPA;
import com.dokio.repository.SpravSysEdizmJPA;
import com.dokio.repository.UserRepository;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SpravSysEdizmController {
    Logger logger = Logger.getLogger("SpravSysEdizmController");

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    UserDetailsServiceImpl userRepository2;
    @Autowired
    SpravSysEdizmJPA spravSysEdizmRepositoryJPA;

    @PostMapping("/api/auth/getSpravSysEdizmTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSpravSysEdizmTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getSpravSysEdizmTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int companyId;//по какому предприятию показывать / 0 - по всем
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<SpravSysEdizmTableJSON> returnList;

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
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;
        }
        int offsetreal = offset * result;//создана переменная с номером страницы
        returnList = spravSysEdizmRepositoryJPA.getSpravSysEdizmTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }
    @PostMapping("/api/auth/getSpravSysEdizmPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSpravSysEdizmPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getSpravSysEdizmPagesList: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать документы/ 0 - по всем
        String searchString = searchRequest.getSearchString();
        companyId = Integer.parseInt(searchRequest.getCompanyId());

        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;}
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;}
        pagenum = offset + 1;
        int size = spravSysEdizmRepositoryJPA.getSpravSysEdizmSize(searchString,companyId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @RequestMapping(
            value = "/api/auth/setDefaultEdizm",
            params = {"edizm_id", "company_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> setDefaultEdizm( @RequestParam("company_id") Long company_id, @RequestParam("edizm_id") Long edizm_id) {
        logger.info("Processing get request for path /api/auth/setDefaultEdizm with edizm_id=" + edizm_id.toString() + ", company_id = " + company_id);
        try {return new ResponseEntity<>(spravSysEdizmRepositoryJPA.setDefaultEdizm(edizm_id,company_id), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller setDefaultEdizm error with edizm_id=" + edizm_id.toString() + ", company_id = " + company_id, e);
            return new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/getSpravSysEdizmValuesById")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSpravSysEdizmValuesById(@RequestBody SearchForm request) {
        logger.info("Processing post request for path /api/auth/getSpravSysEdizmValuesById: " + request.toString());

        SpravSysEdizmJSON response;
        int id = request.getId();
        response=spravSysEdizmRepositoryJPA.getSpravSysEdizmValuesById(id);//результат запроса помещается в экземпляр класса
        ResponseEntity<SpravSysEdizmJSON> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        return responseEntity;
    }
    @PostMapping("/api/auth/insertSpravSysEdizm")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertSpravSysEdizm(@RequestBody SpravSysEdizmForm request){
        logger.info("Processing post request for path /api/auth/insertSpravSysEdizm: " + request.toString());

        Long newDocument = spravSysEdizmRepositoryJPA.insertSpravSysEdizm(request);
        if(newDocument!=null && newDocument>0){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + String.valueOf(newDocument)+"\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when inserting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }
    @PostMapping("/api/auth/updateSpravSysEdizm")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateSpravSysEdizm(@RequestBody SpravSysEdizmForm request){
        logger.info("Processing post request for path /api/auth/updateSpravSysEdizm: " + request.toString());

        if(spravSysEdizmRepositoryJPA.updateSpravSysEdizm(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/deleteEdizm")
    public  ResponseEntity<?> deleteEdizm(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteEdizm: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(spravSysEdizmRepositoryJPA.deleteEdizmById(checked), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Deletion error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/undeleteEdizm")
    public  ResponseEntity<?> undeleteEdizm(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteEdizm: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        try {return new ResponseEntity<>(spravSysEdizmRepositoryJPA.undeleteEdizm(checked), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller undeleteEdizm error", e);
            return new ResponseEntity<>("Restore error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/getSpravSysEdizm")
    public ResponseEntity<?> getSpravSysEdizm(@RequestBody UniversalForm request){
        logger.info("Processing post request for path /api/auth/getSpravSysEdizm: " + request.toString());

        List<SpravSysEdizmTableJSON> returnList;
        try {
            returnList = spravSysEdizmRepositoryJPA.getSpravSysEdizm(request);
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            e.printStackTrace();
            ResponseEntity responseEntity = new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

}
