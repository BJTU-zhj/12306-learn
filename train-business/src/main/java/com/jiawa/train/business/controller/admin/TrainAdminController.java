package com.jiawa.train.business.controller.admin;

import com.jiawa.train.business.DTO.TrainQueryDTO;
import com.jiawa.train.business.DTO.TrainSaveDTO;
import com.jiawa.train.business.VO.TrainQueryVO;
import com.jiawa.train.business.service.TrainSeatService;
import com.jiawa.train.business.service.TrainService;
import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.common.VO.PageVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/train")
public class TrainAdminController {

    @Autowired
    private TrainService trainService;

    @Autowired
    private TrainSeatService trainSeatService;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody TrainSaveDTO trainSaveDTO){
        trainService.save(trainSaveDTO);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageVO<TrainQueryVO>> queryList(@Valid TrainQueryDTO paseengerQueryDTO){

        PageVO<TrainQueryVO> pageVO = trainService.queryList(paseengerQueryDTO);
        return new CommonResp<>(pageVO);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        trainService.delete(id);
        return new CommonResp<>();
    }

    //查询当前所有车次
    @GetMapping("/query-all")
    public CommonResp<List<TrainQueryVO>> queryList(){
        return new CommonResp<>(trainService.queryALL());
    }

    //自动生成火车所有座位
    @GetMapping("/gen-seat/{trainCode}")
    public CommonResp<Object> genSeat(@PathVariable String trainCode){
        trainSeatService.genTrainSeat(trainCode);
        return new CommonResp<>();
    }

}
