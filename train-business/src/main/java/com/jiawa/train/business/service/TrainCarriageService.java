package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.TrainCarriageQueryDTO;
import com.jiawa.train.business.DTO.TrainCarriageSaveDTO;
import com.jiawa.train.business.VO.TrainCarriageQueryVO;
import com.jiawa.train.business.domain.TrainCarriage;
import com.jiawa.train.business.domain.TrainCarriageExample;
import com.jiawa.train.business.enums.SeatColEnum;
import com.jiawa.train.business.mapper.TrainCarriageMapper;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.exception.BusinessExceptionEnum;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainCarriageService {

    private static final Logger LOG= LoggerFactory.getLogger(TrainCarriageService.class);

    @Resource
    private TrainCarriageMapper trainCarriageMapper;

    //保存--存在性校验
    public void save(TrainCarriageSaveDTO trainCarriageSaveDTO){
        DateTime now=DateTime.now();
        TrainCarriage trainCarriage = BeanUtil.copyProperties(trainCarriageSaveDTO, TrainCarriage.class);
        //自动计算车厢座位总数还有根据车座类型获取列数
        List<SeatColEnum> seatColEnums = SeatColEnum.getColsByType(trainCarriage.getSeatType());
        int seatCount=seatColEnums.size()*trainCarriage.getRowCount();
        trainCarriage.setSeatCount(seatCount);
        trainCarriage.setColCount(seatColEnums.size());
        //存在性校验
        TrainCarriage trainCarriageDB=queryByUniqueTrainIndex(trainCarriage.getTrainCode(), trainCarriage.getIndex());
        if(ObjectUtil.isNotEmpty(trainCarriageDB)){
            throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_CARRIAGE_ALREADY_EXIST);
        }
        if(ObjectUtil.isEmpty(trainCarriage.getId())) {
            trainCarriage.setId(SnowUtil.getSnowflakeId());
            trainCarriage.setCreateTime(now);
            trainCarriage.setUpdateTime(now);
            trainCarriageMapper.insert(trainCarriage);
        }else{
            trainCarriage.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", trainCarriage.getId());
            trainCarriageMapper.updateByPrimaryKey( trainCarriage);
        }
    }

    //查询列表
    public PageVO<TrainCarriageQueryVO> queryList(TrainCarriageQueryDTO trainCarriageQueryDTO){
        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
        trainCarriageExample.setOrderByClause("train_code asc,`index` asc");
        TrainCarriageExample.Criteria criteria = trainCarriageExample.createCriteria();
        if (ObjectUtil.isNotEmpty(trainCarriageQueryDTO.getTrainCode())){
            criteria.andTrainCodeEqualTo(trainCarriageQueryDTO.getTrainCode());
        }
        PageHelper.startPage(trainCarriageQueryDTO.getPage(),trainCarriageQueryDTO.getSize());
        List<TrainCarriage> trainCarriageList =trainCarriageMapper.selectByExample(trainCarriageExample);
        ;
        //固定用插件获取查询总数
        PageInfo<TrainCarriage> pageInfo = new PageInfo<>(trainCarriageList);

        PageVO<TrainCarriageQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(trainCarriageList, TrainCarriageQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        trainCarriageMapper.deleteByPrimaryKey(id);
    }

    //根据车次查询该车次所有的车厢信息
    public List<TrainCarriage> selectByTrainCode(String trainCode){
        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
        trainCarriageExample.setOrderByClause("`index` asc");
        trainCarriageExample.createCriteria().andTrainCodeEqualTo(trainCode);
        return trainCarriageMapper.selectByExample(trainCarriageExample);
    }

    //根据唯一键traincode+index查询记录
    public TrainCarriage queryByUniqueTrainIndex(String trainCode,int index){
        TrainCarriageExample trainCarriageExample=new TrainCarriageExample();
        trainCarriageExample.createCriteria().andTrainCodeEqualTo(trainCode).andIndexEqualTo(index);
        List<TrainCarriage> trainCarriageList=trainCarriageMapper.selectByExample(trainCarriageExample);
        if(CollUtil.isNotEmpty(trainCarriageList)){
            return trainCarriageList.get(0);
        }else {
            return null;
        }
    }

}
