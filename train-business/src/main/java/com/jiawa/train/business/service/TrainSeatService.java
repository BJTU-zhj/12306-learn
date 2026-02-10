package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.TrainSeatQueryDTO;
import com.jiawa.train.business.DTO.TrainSeatSaveDTO;
import com.jiawa.train.business.VO.TrainSeatQueryVO;
import com.jiawa.train.business.domain.TrainSeat;
import com.jiawa.train.business.domain.TrainSeatExample;
import com.jiawa.train.business.mapper.TrainSeatMapper;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainSeatService {

    private static final Logger LOG= LoggerFactory.getLogger(TrainSeatService.class);

    @Resource
    private TrainSeatMapper trainSeatMapper;

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
        trainSeatExample.setOrderByClause("id asc");
        TrainSeatExample.Criteria criteria = trainSeatExample.createCriteria();

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

}
