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
import com.dokio.message.request.Settings.SettingsInventoryForm;
import com.dokio.message.response.additional.FilesInventoryJSON;
import com.dokio.message.response.InventoryJSON;
import com.dokio.message.response.InventoryProductTableJSON;
import com.dokio.message.response.Settings.SettingsInventoryJSON;
import com.dokio.repository.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Controller
public class InventoryController {


    Logger logger = Logger.getLogger("InventoryController");

    @Autowired
    InventoryRepository inventoryRepository;


    @PostMapping("/api/auth/getInventoryTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getInventoryTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getInventoryTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        int departmentId;//по какому отделению показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<InventoryJSON> returnList;

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
        returnList = inventoryRepository.getInventoryTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId,departmentId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getInventoryProductTable",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getInventoryProductTable( @RequestParam("id") Long docId) {
        logger.info("Processing get request for path /api/auth/getInventoryProductTable with Inventory id=" + docId.toString());
        List<InventoryProductTableJSON> returnList;
        try {
            returnList = inventoryRepository.getInventoryProductTable(docId);
            return  new ResponseEntity<>(returnList, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка при загрузке таблицы с товарами", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/getInventoryPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getInventoryPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getInventoryPagesList: " + searchRequest.toString());

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
        int size = inventoryRepository.getInventorySize(searchString,companyId,departmentId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @PostMapping("/api/auth/insertInventory")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertInventory(@RequestBody InventoryForm request) {
        logger.info("Processing post request for path /api/auth/insertInventory: " + request.toString());

        Long newDocument = inventoryRepository.insertInventory(request);
        if(newDocument!=null){//вернет id созданного документа либо 0, если недостаточно прав
            return new ResponseEntity<>(String.valueOf(newDocument), HttpStatus.OK);
        } else {//если null - значит на одной из стадий сохранения произошла ошибка
            return new ResponseEntity<>("Document creation error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            value = "/api/auth/getInventoryValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getInventoryValuesById(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/getInventoryValuesById with parameters: " + "id: " + id);
        InventoryJSON response;
        try {
            response=inventoryRepository.getInventoryValuesById(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            logger.error("Exception in method getInventoryValuesById. id = " + id, e);
            e.printStackTrace();
            return new ResponseEntity<>("Error loading document values", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @RequestMapping(
//            value = "/api/auth/getInventoryLinkedDocsList",
//            params = {"id","docName"},
//            method = RequestMethod.GET, produces = "application/json;charset=utf8")
//    public ResponseEntity<?> getInventoryLinkedDocsList(
//            @RequestParam("id") Long id, @RequestParam("docName") String docName) {//передали сюда id инвентаризации и имя таблицы
//        logger.info("Processing get request for path api/auth/getInventoryLinkedDocsList with parameters: " + "id: " + id+ ", docName: "+docName);
//        List<LinkedDocsJSON> returnList;
//        returnList = inventoryRepository.getInventoryLinkedDocsList(id,docName);
//        if(!Objects.isNull(returnList)){
//            return new ResponseEntity<>(returnList, HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("Ошибка при загрузке списка связанных документов", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//    @RequestMapping(
//            value = "/api/auth/getPostingDocsList",
//            params = {"id"},
//            method = RequestMethod.GET, produces = "application/json;charset=utf8")
//    public ResponseEntity<?> getPostingDocsList(
//            @RequestParam("id") Long id) {//передали сюда id инвентаризации
//        logger.info("Processing get request for path api/auth/getPostingDocsList with parameters: " + "id: " + id);
//        List<LinkedDocsJSON> returnList;
//        returnList = inventoryRepository.getPostingDocsList(id);
//        if(!Objects.isNull(returnList)){
//            return new ResponseEntity<>(returnList, HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("Ошибка при загрузке списка связанных Оприходований", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @PostMapping("/api/auth/updateInventory")
    public ResponseEntity<?> updateInventory(@RequestBody InventoryForm request){
        logger.info("Processing post request for path /api/auth/updateInventory: " + request.toString());
        try {return new ResponseEntity<>(inventoryRepository.updateInventory(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Exception in method updateInventory. " + request.toString(), e);
            e.printStackTrace();
            return new ResponseEntity<>("Error saving document", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/saveSettingsInventory")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveSettingsInventory(@RequestBody SettingsInventoryForm request){
        logger.info("Processing post request for path /api/auth/saveSettingsInventory: " + request.toString());

        if(inventoryRepository.saveSettingsInventory(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка сохранения настроек для документа Инвентаризация", HttpStatus.BAD_REQUEST);
        }
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getSettingsInventory",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsInventory()
    {
        logger.info("Processing get request for path /api/auth/getSettingsInventory without request parameters");
        SettingsInventoryJSON response;
        try {
            response=inventoryRepository.getSettingsInventory();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка загрузки настроек для документа Инвентаризация", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/deleteInventory")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteInventory(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteInventory: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        return new ResponseEntity<>(inventoryRepository.deleteInventory(checked), HttpStatus.OK);
    }

    @PostMapping("/api/auth/undeleteInventory")
    public  ResponseEntity<?> undeleteInventory(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteInventory: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        try {return new ResponseEntity<>(inventoryRepository.undeleteInventory(checked), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller undeleteInventory error", e);
            return new ResponseEntity<>("Error of restoring", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/api/auth/getInventoryProductsList",
            params = {"searchString", "companyId", "departmentId", "priceTypeId"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getInventoryProductsList(
            @RequestParam("searchString")   String searchString,
            @RequestParam("companyId")      Long companyId,
            @RequestParam("departmentId")   Long departmentId,
            @RequestParam("priceTypeId")    Long priceTypeId)
    {
        logger.info("Processing post request for path /api/auth/getInventoryProductsList with parameters: " +
                "  searchString: "  + searchString +
                ", companyId: "     + companyId.toString() +
                ", departmentId: "  + departmentId.toString() +
                ", priceTypeId: "   + priceTypeId.toString());
        List returnList;
        returnList = inventoryRepository.getInventoryProductsList(searchString, companyId, departmentId, priceTypeId);
        return new ResponseEntity<>(returnList, HttpStatus.OK);
    }

    //удаление 1 строки из таблицы товаров
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/deleteInventoryProductTableRow",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> deleteCustomersOrdersProductTableRow(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/deleteInventoryProductTableRow with parameters: " +
                "id: " + id);
        boolean result;
        try {
            result=inventoryRepository.deleteInventoryProductTableRow(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/getListOfInventoryFiles")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getListOfInventoryFiles(@RequestBody SearchForm request)  {
        logger.info("Processing post request for path api/auth/getListOfInventoryFiles: " + request.toString());

        Long productId=Long.valueOf(request.getId());
        List<FilesInventoryJSON> returnList;
        try {
            returnList = inventoryRepository.getListOfInventoryFiles(productId);
            return new ResponseEntity<>(returnList, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>("Ошибка запроса списка файлов", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/deleteInventoryFile")
    public ResponseEntity<?> deleteInventoryFile(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/deleteInventoryFile: " + request.toString());

        if(inventoryRepository.deleteInventoryFile(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("File deletion error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/addFilesToInventory")
    public ResponseEntity<?> addFilesToInventory(@RequestBody UniversalForm request) {
        logger.info("Processing post request for path api/auth/addFilesToInventory: " + request.toString());

        if(inventoryRepository.addFilesToInventory(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Ошибка добавления файлов", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/setInventoryAsDecompleted")
    public ResponseEntity<?> setInventoryAsDecompleted(@RequestBody InventoryForm request){
        logger.info("Processing post request for path /api/auth/setInventoryAsDecompleted: " + request.toString());
        try {return new ResponseEntity<>(inventoryRepository.setInventoryAsDecompleted(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller setInventoryAsDecompleted error", e);
            return new ResponseEntity<>("Ошибка запроса на снятие с проведения", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

}




