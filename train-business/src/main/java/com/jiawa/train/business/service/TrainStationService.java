package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.TrainStationQueryDTO;
import com.jiawa.train.business.DTO.TrainStationSaveDTO;
import com.jiawa.train.business.VO.TrainStationQueryVO;
import com.jiawa.train.business.domain.TrainStation;
import com.jiawa.train.business.domain.TrainStationExample;
import com.jiawa.train.business.mapper.TrainStationMapper;
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
public class TrainStationService {

    private static final Logger LOG= LoggerFactory.getLogger(TrainStationService.class);

    @Resource
    private TrainStationMapper trainStationMapper;

    //保存--存在性校验
    public void save(TrainStationSaveDTO trainStationSaveDTO){
        DateTime now=DateTime.now();
        TrainStation trainStation = BeanUtil.copyProperties(trainStationSaveDTO, TrainStation.class);
        //存在性校验
        TrainStation trainStationDBIndex = queryByUniqueIndex(trainStation.getTrainCode(), trainStation.getIndex());
        if(ObjectUtil.isNotEmpty(trainStationDBIndex)&&ObjectUtil.isEmpty(trainStation.getId())){
            throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_STATION_INDEX_ALREADY_EXIST);
        }
        TrainStation trainStationDBName = queryByUniqueName(trainStation.getTrainCode(), trainStation.getName());
        if(ObjectUtil.isNotEmpty(trainStationDBName)&&ObjectUtil.isEmpty(trainStation.getId())){
            throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_STATION_NAME_ALREADY_EXIST);
        }
        if(ObjectUtil.isEmpty(trainStation.getId())) {
            trainStation.setId(SnowUtil.getSnowflakeId());
            trainStation.setCreateTime(now);
            trainStation.setUpdateTime(now);
            trainStationMapper.insert(trainStation);
        }else{
            trainStation.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", trainStation.getId());
            trainStationMapper.updateByPrimaryKey( trainStation);
        }
    }

    //查询列表
    public PageVO<TrainStationQueryVO> queryList(TrainStationQueryDTO trainStationQueryDTO){
        TrainStationExample trainStationExample = new TrainStationExample();
        trainStationExample.setOrderByClause("train_code asc,`index` asc");
        TrainStationExample.Criteria criteria = trainStationExample.createCriteria();
        if(ObjectUtil.isNotEmpty(trainStationQueryDTO.getTrainCode())){
            criteria.andTrainCodeEqualTo(trainStationQueryDTO.getTrainCode());
        }
        PageHelper.startPage(trainStationQueryDTO.getPage(),trainStationQueryDTO.getSize());
        List<TrainStation> trainStationList =trainStationMapper.selectByExample(trainStationExample);
        ;
        //固定用插件获取查询总数
        PageInfo<TrainStation> pageInfo = new PageInfo<>(trainStationList);

        PageVO<TrainStationQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(trainStationList, TrainStationQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        trainStationMapper.deleteByPrimaryKey(id);
    }

    //根据唯一键traincode+index查询记录
    public TrainStation queryByUniqueIndex(String trainCode,int index){
        TrainStationExample trainStationExample=new TrainStationExample();
        trainStationExample.createCriteria().andTrainCodeEqualTo(trainCode).andIndexEqualTo(index);
        List<TrainStation> trainStationList=trainStationMapper.selectByExample(trainStationExample);
        if(CollUtil.isNotEmpty(trainStationList)){
            return trainStationList.get(0);
        }else {
            return null;
        }
    }

    //根据唯一键traincode+name查询记录
    public TrainStation queryByUniqueName(String trainCode,String name){
        TrainStationExample trainStationExample=new TrainStationExample();
        trainStationExample.createCriteria().andTrainCodeEqualTo(trainCode).andNameEqualTo(name);
        List<TrainStation> trainStationList=trainStationMapper.selectByExample(trainStationExample);
        if(CollUtil.isNotEmpty(trainStationList)){
            return trainStationList.get(0);
        }else {
            return null;
        }
    }

    //feign自动生成每日数据用
    public List<TrainStation> selectByTrainCode(String trainCode){
        TrainStationExample trainStationExample=new TrainStationExample();
        trainStationExample.setOrderByClause("`index` asc");
        trainStationExample.createCriteria().andTrainCodeEqualTo(trainCode);
        return trainStationMapper.selectByExample(trainStationExample);
    }


}
