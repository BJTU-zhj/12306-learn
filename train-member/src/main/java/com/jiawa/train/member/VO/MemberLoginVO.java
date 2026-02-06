package com.jiawa.train.member.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberLoginVO {


    public Long id;
    public String mobile;
    public String token;

}
