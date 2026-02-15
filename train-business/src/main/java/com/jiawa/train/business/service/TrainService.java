package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.TrainQueryDTO;
import com.jiawa.train.business.DTO.TrainSaveDTO;
import com.jiawa.train.business.VO.TrainQueryVO;
import com.jiawa.train.business.domain.Train;
import com.jiawa.train.business.domain.TrainExample;
import com.jiawa.train.business.mapper.TrainMapper;
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
public class TrainService {

    private static final Logger LOG= LoggerFactory.getLogger(TrainService.class);

    @Resource
    private TrainMapper trainMapper;

    //保存--新增存在性校验
    public void save(TrainSaveDTO trainSaveDTO){
        DateTime now=DateTime.now();
        Train train = BeanUtil.copyProperties(trainSaveDTO, Train.class);
        //存在性校验
        Train dbTrain=queryByUnique(train.getCode());
        if(ObjectUtil.isNotEmpty(dbTrain)&&ObjectUtil.isEmpty(train.getId())){
            throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_ALREADY_EXIST);
        }
        if(ObjectUtil.isEmpty(train.getId())) {
            train.setId(SnowUtil.getSnowflakeId());
            train.setCreateTime(now);
            train.setUpdateTime(now);
            trainMapper.insert(train);
        }else{
            train.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", train.getId());
            trainMapper.updateByPrimaryKey( train);
        }
    }

    //查询列表
    public PageVO<TrainQueryVO> queryList(TrainQueryDTO trainQueryDTO){
        TrainExample trainExample = new TrainExample();
        trainExample.setOrderByClause("id asc");
        TrainExample.Criteria criteria = trainExample.createCriteria();

        PageHelper.startPage(trainQueryDTO.getPage(),trainQueryDTO.getSize());
        List<Train> trainList =trainMapper.selectByExample(trainExample);
        ;
        //固定用插件获取查询总数
        PageInfo<Train> pageInfo = new PageInfo<>(trainList);

        PageVO<TrainQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(trainList, TrainQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        trainMapper.deleteByPrimaryKey(id);
    }

    //查询所有 车次
    public List<TrainQueryVO> queryALL(){
        List<Train> trainList = selectAll();
        return BeanUtil.copyToList(trainList,TrainQueryVO.class);
    }

    public List<Train> selectAll() {
        TrainExample  trainExample=new TrainExample();
        trainExample.setOrderByClause("id asc");
        TrainExample.Criteria criteria=trainExample.createCriteria();
        return trainMapper.selectByExample(trainExample);
    }

    //根据唯一键traincode查询记录
    public Train queryByUnique(String code){
        TrainExample trainExample=new TrainExample();
        trainExample.createCriteria().andCodeEqualTo(code);
        List<Train> trainList=trainMapper.selectByExample(trainExample);
        if(CollUtil.isNotEmpty(trainList)){
            return trainList.get(0);
        }else {
            return null;
        }
    }

}
