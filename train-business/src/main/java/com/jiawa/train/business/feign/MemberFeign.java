package com.jiawa.train.business.feign;

import com.jiawa.train.common.DTO.MemberTicketDTO;
import com.jiawa.train.common.VO.CommonResp;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "train-member",url = "http://localhost:8001/member")
public interface MemberFeign {

    @PostMapping("/feign/ticket/save-confiem")
    CommonResp<Object> saveConfirm(@Valid @RequestBody MemberTicketDTO memberTicketDTO);

}
