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
package com.dokio.controller;

        import com.dokio.message.request.*;
        import com.dokio.message.request.Settings.SettingsOrdersupForm;
        import com.dokio.message.response.OrdersupJSON;
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
public class OrdersupController {

    Logger logger = Logger.getLogger("OrdersupController");

    @Autowired
    OrdersupRepositoryJPA ordersupRepository;

    @PostMapping("/api/auth/getOrdersupTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getOrdersupTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getOrdersupTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        int departmentId;//по какому отделению показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<OrdersupJSON> returnList;

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
        returnList = ordersupRepository.getOrdersupTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId,departmentId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getOrdersupPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getOrdersupPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getOrdersupPagesList: " + searchRequest.toString());

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
        int size = ordersupRepository.getOrdersupSize(searchString,companyId,departmentId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getOrdersupProductTable",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getOrdersupProductTable( @RequestParam("id") Long docId) {
        logger.info("Processing get request for path /api/auth/getOrdersupProductTable with Ordersup id=" + docId.toString());
        return  new ResponseEntity<>(ordersupRepository.getOrdersupProductTable(docId), HttpStatus.OK);
    }

    @PostMapping("/api/auth/insertOrdersup")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertOrdersup(@RequestBody OrdersupForm request){
        logger.info("Processing post request for path /api/auth/insertOrdersup: " + request.toString());
        return new ResponseEntity<>(ordersupRepository.insertOrdersup(request), HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/auth/getOrdersupValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getOrdersupValuesById(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/getOrdersupValuesById with parameters: " + "id: " + id);
        return new ResponseEntity<>(ordersupRepository.getOrdersupValuesById(id), HttpStatus.OK);
    }

    @PostMapping("/api/auth/updateOrdersup")
    public ResponseEntity<?> updateOrdersup(@RequestBody OrdersupForm request){
        logger.info("Processing post request for path /api/auth/updateOrdersup: " + request.toString());
        return new ResponseEntity<>(ordersupRepository.updateOrdersup(request), HttpStatus.OK);
    }

    @PostMapping("/api/auth/saveSettingsOrdersup")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveSettingsOrdersup(@RequestBody SettingsOrdersupForm request){
        logger.info("Processing post request for path /api/auth/saveSettingsOrdersup: " + request.toString());
        if(ordersupRepository.saveSettingsOrdersup(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка сохранения настроек для документа Розничная продажа", HttpStatus.BAD_REQUEST);
        }
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getSettingsOrdersup",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsOrdersup(){
        logger.info("Processing get request for path /api/auth/getSettingsOrdersup without request parameters");
        return new ResponseEntity<>(ordersupRepository.getSettingsOrdersup(), HttpStatus.OK);
    }


    @PostMapping("/api/auth/deleteOrdersup")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteOrdersup(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteOrdersup: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        return new ResponseEntity<>(ordersupRepository.deleteOrdersup(checked), HttpStatus.OK);
    }

    @PostMapping("/api/auth/undeleteOrdersup")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteOrdersup(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteOrdersup: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        return new ResponseEntity<>(ordersupRepository.undeleteOrdersup(checked), HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/auth/getListOfOrdersupFiles",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getListOfOrdersupFiles(
            @RequestParam("id") Long id)
    {
        logger.info("Processing post request for path api/auth/getListOfOrdersupFiles: " + id);
        try {
            return new ResponseEntity<>(ordersupRepository.getListOfOrdersupFiles(id), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>("Ошибка запроса списка файлов", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/deleteOrdersupFile")
    public ResponseEntity<?> deleteOrdersupFile(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/deleteOrdersupFile: " + request.toString());
        try {
            return new ResponseEntity<>(ordersupRepository.deleteOrdersupFile(request), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>("Ошибка удаления файлов", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/addFilesToOrdersup")
    public ResponseEntity<?> addFilesToOrdersup(@RequestBody UniversalForm request) {
        logger.info("Processing post request for path api/auth/addFilesToOrdersup: " + request.toString());
        try{
            return new ResponseEntity<>(ordersupRepository.addFilesToOrdersup(request), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>("Ошибка добавления файлов", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}