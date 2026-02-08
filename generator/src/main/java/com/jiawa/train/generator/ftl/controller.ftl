package com.jiawa.train.${module}.controller;

import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.${module}.DTO.${Domain}QueryDTO;
import com.jiawa.train.${module}.DTO.${Domain}SaveDTO;
import com.jiawa.train.${module}.VO.${Domain}QueryVO;
import com.jiawa.train.${module}.service.${Domain}Service;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/${do_main}")
public class ${Domain}Controller {

    @Autowired
    private ${Domain}Service ${domain}Service;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody ${Domain}SaveDTO ${domain}SaveDTO){
        ${domain}Service.save(${domain}SaveDTO);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageVO<${Domain}QueryVO>> queryList(@Valid ${Domain}QueryDTO paseengerQueryDTO){

        PageVO<${Domain}QueryVO> pageVO = ${domain}Service.queryList(paseengerQueryDTO);
        return new CommonResp<>(pageVO);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        ${domain}Service.delete(id);
        return new CommonResp<>();
    }

}
