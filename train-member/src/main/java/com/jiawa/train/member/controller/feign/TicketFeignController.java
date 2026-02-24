package com.jiawa.train.member.controller.feign;

import com.jiawa.train.common.DTO.MemberTicketDTO;
import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.member.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feign/ticket")
public class TicketFeignController {

    @Autowired
    private TicketService ticketService;

    @PostMapping("/save-confiem")
    public CommonResp<Object> saveConfirm(@Valid @RequestBody MemberTicketDTO memberTicketDTO){
        ticketService.saveConfirm(memberTicketDTO);
        return new CommonResp<>();
    }
}
