package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.TrainCarriageQueryDTO;
import com.jiawa.train.business.DTO.TrainCarriageSaveDTO;
import com.jiawa.train.business.VO.TrainCarriageQueryVO;
import com.jiawa.train.business.domain.TrainCarriage;
import com.jiawa.train.business.domain.TrainCarriageExample;
import com.jiawa.train.business.mapper.TrainCarriageMapper;
import com.jiawa.train.common.VO.PageVO;
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

    //保存
    public void save(TrainCarriageSaveDTO trainCarriageSaveDTO){
        DateTime now=DateTime.now();
        TrainCarriage trainCarriage = BeanUtil.copyProperties(trainCarriageSaveDTO, TrainCarriage.class);
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
        trainCarriageExample.setOrderByClause("id asc");
        TrainCarriageExample.Criteria criteria = trainCarriageExample.createCriteria();

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

}
