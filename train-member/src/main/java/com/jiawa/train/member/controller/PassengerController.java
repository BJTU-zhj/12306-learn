package com.jiawa.train.member.controller;

import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.member.DTO.PassengerQueryDTO;
import com.jiawa.train.member.DTO.PassengerSaveDTO;
import com.jiawa.train.member.VO.PassengerQueryVO;
import com.jiawa.train.member.service.PassengerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/passenger")
public class PassengerController {

    @Autowired
    private PassengerService passengerService;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody PassengerSaveDTO passengerSaveDTO){
        passengerService.save(passengerSaveDTO);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageVO<PassengerQueryVO>> queryList(@Valid PassengerQueryDTO paseengerQueryDTO){
        paseengerQueryDTO.setMemberId(LoginMemberContext.getId());
        PageVO<PassengerQueryVO> pageVO = passengerService.queryList(paseengerQueryDTO);
        return new CommonResp<>(pageVO);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        passengerService.delete(id);
        return new CommonResp<>();
    }

}
