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
package com.dokio.controller.store.woo.v3;

import com.dokio.message.request.store.woo.v3.SyncIdsForm;
import com.dokio.message.request.store.woo.v3.TaxesForm;
import com.dokio.repository.store.woo.v3.StoreTaxesRepository;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/public/woo_v3")
public class TaxesController {
/*
    Logger logger = Logger.getLogger(TaxesController.class);

    @Autowired
    StoreTaxesRepository storeTaxesRepository;
    @Autowired
    CommonUtilites cu;

    @PostMapping("/syncTaxesFromStore")
    public ResponseEntity<?> syncTaxesFromStore(HttpServletRequest httpServletRequest, @RequestBody TaxesForm  request){
        logger.info("Processing post request for path /api/public/woo_v3/syncTaxesFromStore: " + request.toString());
        try {cu.checkStoreIp(httpServletRequest.getRemoteAddr(), request.getCrmSecretKey());
            return new ResponseEntity<>(storeTaxesRepository.syncTaxesFromStore(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller syncTaxesFromStore error", e);
        return new ResponseEntity<>("Operation of the synchronization error. " + e, HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/syncTaxesToStore",
            params = {"key"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> syncTaxesToStore(HttpServletRequest httpServletRequest,
            @RequestParam("key") String key){
        logger.info("Processing post request for path /api/public/woo_v3/syncTaxesToStore");
        try {cu.checkStoreIp(httpServletRequest.getRemoteAddr(), key);
        return new ResponseEntity<>(storeTaxesRepository.syncTaxesToStore(key), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller syncTaxesToStore error", e);
        return new ResponseEntity<>("Operation of the synchronization error. " + e, HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/syncTaxesIds")
    public ResponseEntity<?> syncTaxesIds(HttpServletRequest httpServletRequest, @RequestBody SyncIdsForm  request){
        logger.info("Processing post request for path /api/public/woo_v3/syncTaxesIds: " + request.toString());
        try {cu.checkStoreIp(httpServletRequest.getRemoteAddr(), request.getCrmSecretKey());
            return new ResponseEntity<>(storeTaxesRepository.syncTaxesIds(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller syncTaxesIds error", e);
        return new ResponseEntity<>("Operation of the synchronization ids error. " + e, HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    */
}
