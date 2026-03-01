package com.jiawa.train.business.controller;

import com.jiawa.train.business.DTO.ConfirmOrderDoDTO;
import com.jiawa.train.business.service.BeforeConfirmOrderService;
import com.jiawa.train.business.service.ConfirmOrderService;
import com.jiawa.train.common.VO.CommonResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/confirm-order")
public class ConfirmOrderController {

    @Autowired
    private BeforeConfirmOrderService beforeConfirmOrderService;

    @Resource
    private ConfirmOrderService confirmOrderService;

    @PostMapping("/do")
    public CommonResp<Object> doConfirm(@Valid @RequestBody ConfirmOrderDoDTO confirmOrderDoDTO){
        Long orderId=beforeConfirmOrderService.beforeConfirmOrder(confirmOrderDoDTO);
        return new CommonResp<>(String.valueOf(orderId));
    }

    @GetMapping("/query-line-count/{orderId}")
    public CommonResp<Integer> queryLineCount(@PathVariable Long orderId){
        Integer count=beforeConfirmOrderService.queryLineQueue(orderId);
        return new CommonResp<>(count);
    }

    @GetMapping("/cancel/{orderId}")
    public CommonResp<Object> cancel(@PathVariable Long orderId){
        int cancelCount=confirmOrderService.cancelOrderLine(orderId);
        return new CommonResp<>(cancelCount);
    }

}
