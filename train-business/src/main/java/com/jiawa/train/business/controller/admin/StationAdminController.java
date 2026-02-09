package com.jiawa.train.business.controller.admin;

import com.jiawa.train.business.DTO.StationQueryDTO;
import com.jiawa.train.business.DTO.StationSaveDTO;
import com.jiawa.train.business.VO.StationQueryVO;
import com.jiawa.train.business.service.StationService;
import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.common.VO.PageVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/station")
public class StationAdminController {

    @Autowired
    private StationService stationService;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody StationSaveDTO stationSaveDTO){
        stationService.save(stationSaveDTO);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageVO<StationQueryVO>> queryList(@Valid StationQueryDTO paseengerQueryDTO){

        PageVO<StationQueryVO> pageVO = stationService.queryList(paseengerQueryDTO);
        return new CommonResp<>(pageVO);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        stationService.delete(id);
        return new CommonResp<>();
    }

}
