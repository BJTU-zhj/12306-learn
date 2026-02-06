package com.jiawa.train.common.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberLoginResp {
    public Long id;
    public String mobile;
    public String token;

}
