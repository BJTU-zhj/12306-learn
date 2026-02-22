package com.jiawa.train.business.controller.admin;

import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.business.DTO.ConfirmOrderQueryDTO;
import com.jiawa.train.business.DTO.ConfirmOrderSaveDTO;
import com.jiawa.train.business.VO.ConfirmOrderQueryVO;
import com.jiawa.train.business.service.ConfirmOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/confirm-order")
public class ConfirmOrderAdminController {

    @Autowired
    private ConfirmOrderService confirmOrderService;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody ConfirmOrderSaveDTO confirmOrderSaveDTO){
        confirmOrderService.save(confirmOrderSaveDTO);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageVO<ConfirmOrderQueryVO>> queryList(@Valid ConfirmOrderQueryDTO paseengerQueryDTO){

        PageVO<ConfirmOrderQueryVO> pageVO = confirmOrderService.queryList(paseengerQueryDTO);
        return new CommonResp<>(pageVO);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        confirmOrderService.delete(id);
        return new CommonResp<>();
    }

}
