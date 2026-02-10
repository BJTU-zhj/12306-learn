package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.TrainSeatQueryDTO;
import com.jiawa.train.business.DTO.TrainSeatSaveDTO;
import com.jiawa.train.business.VO.TrainSeatQueryVO;
import com.jiawa.train.business.domain.TrainCarriage;
import com.jiawa.train.business.domain.TrainSeat;
import com.jiawa.train.business.domain.TrainSeatExample;
import com.jiawa.train.business.enums.SeatColEnum;
import com.jiawa.train.business.mapper.TrainSeatMapper;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainSeatService {

    private static final Logger LOG= LoggerFactory.getLogger(TrainSeatService.class);

    @Resource
    private TrainSeatMapper trainSeatMapper;

    @Resource
    private TrainCarriageService trainCarriageService;

    //保存
    public void save(TrainSeatSaveDTO trainSeatSaveDTO){
        DateTime now=DateTime.now();
        TrainSeat trainSeat = BeanUtil.copyProperties(trainSeatSaveDTO, TrainSeat.class);
        if(ObjectUtil.isEmpty(trainSeat.getId())) {
            trainSeat.setId(SnowUtil.getSnowflakeId());
            trainSeat.setCreateTime(now);
            trainSeat.setUpdateTime(now);
            trainSeatMapper.insert(trainSeat);
        }else{
            trainSeat.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", trainSeat.getId());
            trainSeatMapper.updateByPrimaryKey( trainSeat);
        }
    }

    //查询列表
    public PageVO<TrainSeatQueryVO> queryList(TrainSeatQueryDTO trainSeatQueryDTO){
        TrainSeatExample trainSeatExample = new TrainSeatExample();
        trainSeatExample.setOrderByClause("train_code asc,`carriage_index` asc");
        TrainSeatExample.Criteria criteria = trainSeatExample.createCriteria();
        if(ObjectUtil.isNotEmpty(trainSeatQueryDTO.getTrainCode())){
            criteria.andTrainCodeEqualTo(trainSeatQueryDTO.getTrainCode());
        }
        PageHelper.startPage(trainSeatQueryDTO.getPage(),trainSeatQueryDTO.getSize());
        List<TrainSeat> trainSeatList =trainSeatMapper.selectByExample(trainSeatExample);
        ;
        //固定用插件获取查询总数
        PageInfo<TrainSeat> pageInfo = new PageInfo<>(trainSeatList);

        PageVO<TrainSeatQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(trainSeatList, TrainSeatQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        trainSeatMapper.deleteByPrimaryKey(id);
    }

    //根据车次编号自动生成座位
    @Transactional
    public void genTrainSeat(String trainCode){
        DateTime now = DateTime.now();
        //先删除原先的座位
        TrainSeatExample trainSeatExample = new TrainSeatExample();
        trainSeatExample.createCriteria().andTrainCodeEqualTo(trainCode);
        trainSeatMapper.deleteByExample(trainSeatExample);

        //获取该车次的所有车厢
        List<TrainCarriage> trainCarriageList = trainCarriageService.selectByTrainCode(trainCode);
        //循环添加车厢的座位
        for (TrainCarriage trainCarriage : trainCarriageList){
            //行数
            Integer rowCount=trainCarriage.getRowCount();
            //座位类型
            String seatType=trainCarriage.getSeatType();
            //座位计数
            int seatIndex=1;
            //根据座位类型获取列数
            List<SeatColEnum> colList = SeatColEnum.getColsByType(seatType);
            for (int row=1;row<=rowCount;row++){
                for (SeatColEnum col : colList){
                    TrainSeat trainSeat = new TrainSeat();
                    trainSeat.setId(SnowUtil.getSnowflakeId());
                    trainSeat.setCarriageSeatIndex(seatIndex++);
                    trainSeat.setCreateTime(now);
                    trainSeat.setUpdateTime(now);
                    trainSeat.setTrainCode(trainCode);
                    trainSeat.setSeatType(seatType);
                    trainSeat.setCarriageIndex(trainCarriage.getIndex());
                    trainSeat.setRow(StrUtil.fillBefore(String.valueOf(row), '0', 2));
                    trainSeat.setCol(col.getCode());
                    trainSeatMapper.insert(trainSeat);
                }
            }
        }

    }

}
