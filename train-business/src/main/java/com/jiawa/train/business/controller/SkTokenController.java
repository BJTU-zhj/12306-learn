package com.jiawa.train.business.controller;

import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.business.DTO.SkTokenQueryDTO;
import com.jiawa.train.business.DTO.SkTokenSaveDTO;
import com.jiawa.train.business.VO.SkTokenQueryVO;
import com.jiawa.train.business.service.SkTokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/sk-token")
public class SkTokenController {

    @Autowired
    private SkTokenService skTokenService;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody SkTokenSaveDTO skTokenSaveDTO){
        skTokenService.save(skTokenSaveDTO);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageVO<SkTokenQueryVO>> queryList(@Valid SkTokenQueryDTO paseengerQueryDTO){

        PageVO<SkTokenQueryVO> pageVO = skTokenService.queryList(paseengerQueryDTO);
        return new CommonResp<>(pageVO);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        skTokenService.delete(id);
        return new CommonResp<>();
    }

}
