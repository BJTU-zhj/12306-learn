package com.jiawa.train.member.controller.admin;

import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.member.DTO.TicketQueryDTO;
import com.jiawa.train.member.DTO.TicketSaveDTO;
import com.jiawa.train.member.VO.TicketQueryVO;
import com.jiawa.train.member.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/ticket")
public class TicketAdminController {

    @Autowired
    private TicketService ticketService;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody TicketSaveDTO ticketSaveDTO){
        ticketService.save(ticketSaveDTO);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageVO<TicketQueryVO>> queryList(@Valid TicketQueryDTO paseengerQueryDTO){

        PageVO<TicketQueryVO> pageVO = ticketService.queryList(paseengerQueryDTO);
        return new CommonResp<>(pageVO);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        ticketService.delete(id);
        return new CommonResp<>();
    }

}
