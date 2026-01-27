package com.jiawa.train.member.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.exception.BusinessExceptionEnum;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.member.DTO.MemberRegisterDTO;
import com.jiawa.train.member.DTO.MemberSendCodeDTO;
import com.jiawa.train.member.domain.Member;
import com.jiawa.train.member.domain.MemberExample;
import com.jiawa.train.member.mapper.MemberMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    private static final Logger LOG= LoggerFactory.getLogger(MemberService.class);

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
        member.setId(SnowUtil.getSnowflakeId());
        member .setMobile(memberRegisterDTO.getMobile());
        memberMapper.insert(member);
        return member.getId();
    }

    //发送验证码
    public void sendCode(MemberSendCodeDTO memberSendCodeDTO){
        //先看下有没有
        MemberExample memberExample=new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(memberSendCodeDTO.getMobile());
        List<Member> memberList = memberMapper.selectByExample(memberExample);
        //如果没有,就注册
        if(CollUtil.isEmpty(memberList)){
            LOG.info("手机号不存在，进行注册");
            Member member = new Member();
            member.setId(SnowUtil.getSnowflakeId());
            member .setMobile(memberSendCodeDTO.getMobile());
            memberMapper.insert(member);
        }

        //生成验证码
        String code= RandomUtil.randomNumbers(4);
        LOG.info("生成验证码：{}",code);
        //Todo 保存信息验证信息表，验证码，手机号，有效期，创建时间，更新时间，是否使用，业务类型

        //Todo 对接短信通道，发送验证码
    }

}
