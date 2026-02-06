package com.jiawa.train.member.controller;

import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.resp.CommonResp;
import com.jiawa.train.member.DTO.PaseengerQueryDTO;
import com.jiawa.train.member.DTO.PassengerSaveDTO;
import com.jiawa.train.member.VO.PassengerQueryVO;
import com.jiawa.train.member.service.PassengerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public CommonResp<List<PassengerQueryVO>> queryList(@Valid PaseengerQueryDTO paseengerQueryDTO){
        paseengerQueryDTO.setMemberId(LoginMemberContext.getId());
        List<PassengerQueryVO> list = passengerService.queryList(paseengerQueryDTO);
        return new CommonResp<>(list);
    }

}
