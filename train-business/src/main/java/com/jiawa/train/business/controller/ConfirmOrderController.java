package com.jiawa.train.business.controller;

import com.jiawa.train.business.DTO.ConfirmOrderDoDTO;
import com.jiawa.train.business.service.ConfirmOrderService;
import com.jiawa.train.common.VO.CommonResp;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/confirm-order")
public class ConfirmOrderController {

    @Autowired
    private ConfirmOrderService confirmOrderService;

    @PostMapping("/save")
    public CommonResp doConfirm(@Valid @RequestBody ConfirmOrderDoDTO confirmOrderDoDTO){
        confirmOrderService.doConfirm(confirmOrderDoDTO);
        return new CommonResp<>();
    }

}
