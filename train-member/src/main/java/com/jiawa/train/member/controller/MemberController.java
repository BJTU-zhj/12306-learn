package com.jiawa.train.member.controller;

import com.jiawa.train.common.resp.CommonResp;
import com.jiawa.train.member.DTO.MemberLoginDTO;
import com.jiawa.train.member.DTO.MemberRegisterDTO;
import com.jiawa.train.member.DTO.MemberSendCodeDTO;
import com.jiawa.train.member.VO.MemberLoginVO;
import com.jiawa.train.member.service.MemberService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/sendcode")
    public CommonResp sendCode(@Valid MemberSendCodeDTO memberSendCodeDTO){
        memberService.sendCode(memberSendCodeDTO);
        return new CommonResp<>();
    }

    @PostMapping("/login")
    public CommonResp sendCode(@Valid MemberLoginDTO memberLoginDTO){
        MemberLoginVO memberLoginVO =memberService.login(memberLoginDTO);
        return new CommonResp<>(memberLoginVO);
    }
}
