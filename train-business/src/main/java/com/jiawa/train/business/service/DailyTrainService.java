package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.DailyTrainQueryDTO;
import com.jiawa.train.business.DTO.DailyTrainSaveDTO;
import com.jiawa.train.business.VO.DailyTrainQueryVO;
import com.jiawa.train.business.domain.DailyTrain;
import com.jiawa.train.business.domain.DailyTrainExample;
import com.jiawa.train.business.mapper.DailyTrainMapper;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyTrainService {

    private static final Logger LOG= LoggerFactory.getLogger(DailyTrainService.class);

    @Resource
    private DailyTrainMapper dailyTrainMapper;

    //保存
    public void save(DailyTrainSaveDTO dailyTrainSaveDTO){
        DateTime now=DateTime.now();
        DailyTrain dailyTrain = BeanUtil.copyProperties(dailyTrainSaveDTO, DailyTrain.class);
        if(ObjectUtil.isEmpty(dailyTrain.getId())) {
            dailyTrain.setId(SnowUtil.getSnowflakeId());
            dailyTrain.setCreateTime(now);
            dailyTrain.setUpdateTime(now);
            dailyTrainMapper.insert(dailyTrain);
        }else{
            dailyTrain.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", dailyTrain.getId());
            dailyTrainMapper.updateByPrimaryKey( dailyTrain);
        }
    }

    //查询列表
    public PageVO<DailyTrainQueryVO> queryList(DailyTrainQueryDTO dailyTrainQueryDTO){
        DailyTrainExample dailyTrainExample = new DailyTrainExample();
        dailyTrainExample.setOrderByClause("date desc,code asc");
        DailyTrainExample.Criteria criteria = dailyTrainExample.createCriteria();

        //条件查询
        if(ObjectUtil.isNotEmpty(dailyTrainQueryDTO.getCode())){
            criteria.andCodeEqualTo(dailyTrainQueryDTO.getCode());
        }
        if(ObjectUtil.isNotNull(dailyTrainQueryDTO.getDate())){
            criteria.andDateEqualTo(dailyTrainQueryDTO.getDate());
        }
        PageHelper.startPage(dailyTrainQueryDTO.getPage(),dailyTrainQueryDTO.getSize());
        List<DailyTrain> dailyTrainList =dailyTrainMapper.selectByExample(dailyTrainExample);
        ;
        //固定用插件获取查询总数
        PageInfo<DailyTrain> pageInfo = new PageInfo<>(dailyTrainList);

        PageVO<DailyTrainQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(dailyTrainList, DailyTrainQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        dailyTrainMapper.deleteByPrimaryKey(id);
    }

}
