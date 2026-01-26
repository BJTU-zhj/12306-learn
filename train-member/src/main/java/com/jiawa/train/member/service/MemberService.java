package com.jiawa.train.member.service;

import cn.hutool.core.collection.CollUtil;
import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.exception.BusinessExceptionEnum;
import com.jiawa.train.member.DTO.MemberRegisterDTO;
import com.jiawa.train.member.domain.Member;
import com.jiawa.train.member.domain.MemberExample;
import com.jiawa.train.member.mapper.MemberMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    @Resource
    private MemberMapper memberMapper;

    public int count(){
        return Math.toIntExact(memberMapper.countByExample(null));
    }

    public long register(MemberRegisterDTO memberRegisterDTO){
        //先看下有没有
        MemberExample memberExample=new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(memberRegisterDTO.getMobile());
        List<Member> memberList = memberMapper.selectByExample(memberExample);
        if(CollUtil.isNotEmpty(memberList)){
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_EXIST);
        }
        Member member = new Member();
        member.setId(System.currentTimeMillis());
        member .setMobile(memberRegisterDTO.getMobile());
        memberMapper.insert(member);
        return member.getId();
    }

}
