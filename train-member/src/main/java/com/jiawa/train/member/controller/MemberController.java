package com.jiawa.train.member.controller;

import com.jiawa.train.common.resp.CommonResp;
import com.jiawa.train.member.DTO.MemberLoginDTO;
import com.jiawa.train.member.DTO.MemberRegisterDTO;
import com.jiawa.train.member.DTO.MemberSendCodeDTO;
import com.jiawa.train.member.VO.MemberLoginVO;
import com.jiawa.train.member.service.MemberService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Resource
    private MemberService memberService;

    @GetMapping("/count")
    public CommonResp<Integer> count(){
        int count = memberService.count();
        return new CommonResp<>(count);
    }

    @PostMapping("/register")
    public CommonResp<Long> register(@Valid MemberRegisterDTO memberRegisterDTO){
        long register = memberService.register(memberRegisterDTO);
        return new CommonResp<>(register);
    }

    @PostMapping("/send-code")
    public CommonResp sendCode(@Valid @RequestBody MemberSendCodeDTO memberSendCodeDTO){
        memberService.sendCode(memberSendCodeDTO);
        return new CommonResp<>();
    }

    @PostMapping("/login")
    public CommonResp login(@Valid @RequestBody MemberLoginDTO memberLoginDTO){
        MemberLoginVO memberLoginVO =memberService.login(memberLoginDTO);
        return new CommonResp<>(memberLoginVO);
    }
}
