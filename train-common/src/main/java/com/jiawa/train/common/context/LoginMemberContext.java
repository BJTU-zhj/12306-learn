package com.jiawa.train.common.context;


import com.jiawa.train.common.resp.MemberLoginResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginMemberContext {

    private static final Logger LOG = LoggerFactory.getLogger(LoginMemberContext.class);

    private static final ThreadLocal<MemberLoginResp> member=new ThreadLocal<>();

    public static MemberLoginResp getMember() {
        return member.get();
    }

    public static void setMember(MemberLoginResp member) {
        LoginMemberContext.member.set(member);
    }

    public static Long getId() {
        try {
            return member.get().getId();
        } catch (Exception e) {
            LOG.error("获取登录会员id失败", e);
            throw e;
        }
    }

    public static String getMobile() {
        try {
            return member.get().getMobile();
        } catch (Exception e) {
            LOG.error("获取登录会员手机号失败", e);
            throw e;
        }
    }

    public static String getToken() {
        try {
            return member.get().getToken();
        } catch (Exception e) {
            LOG.error("获取登录会员token失败", e);
            throw e;
        }
    }


}



