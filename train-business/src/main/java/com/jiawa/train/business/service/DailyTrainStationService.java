package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.DailyTrainStationQueryDTO;
import com.jiawa.train.business.DTO.DailyTrainStationSaveDTO;
import com.jiawa.train.business.VO.DailyTrainStationQueryVO;
import com.jiawa.train.business.domain.DailyTrainStation;
import com.jiawa.train.business.domain.DailyTrainStationExample;
import com.jiawa.train.business.mapper.DailyTrainStationMapper;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyTrainStationService {

    private static final Logger LOG= LoggerFactory.getLogger(DailyTrainStationService.class);

    @Resource
    private DailyTrainStationMapper dailyTrainStationMapper;

    //保存
    public void save(DailyTrainStationSaveDTO dailyTrainStationSaveDTO){
        DateTime now=DateTime.now();
        DailyTrainStation dailyTrainStation = BeanUtil.copyProperties(dailyTrainStationSaveDTO, DailyTrainStation.class);
        if(ObjectUtil.isEmpty(dailyTrainStation.getId())) {
            dailyTrainStation.setId(SnowUtil.getSnowflakeId());
            dailyTrainStation.setCreateTime(now);
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStationMapper.insert(dailyTrainStation);
        }else{
            dailyTrainStation.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", dailyTrainStation.getId());
            dailyTrainStationMapper.updateByPrimaryKey( dailyTrainStation);
        }
    }

    //查询列表
    public PageVO<DailyTrainStationQueryVO> queryList(DailyTrainStationQueryDTO dailyTrainStationQueryDTO){
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        dailyTrainStationExample.setOrderByClause("date desc,train_code asc,`index` asc");
        DailyTrainStationExample.Criteria criteria = dailyTrainStationExample.createCriteria();

        //条件查询
        if(ObjectUtil.isNotEmpty(dailyTrainStationQueryDTO.getTrainCode())){
            criteria.andTrainCodeEqualTo(dailyTrainStationQueryDTO.getTrainCode());
        }
        if(ObjectUtil.isNotNull(dailyTrainStationQueryDTO.getDate())){
            criteria.andDateEqualTo(dailyTrainStationQueryDTO.getDate());
        }

        PageHelper.startPage(dailyTrainStationQueryDTO.getPage(),dailyTrainStationQueryDTO.getSize());
        List<DailyTrainStation> dailyTrainStationList =dailyTrainStationMapper.selectByExample(dailyTrainStationExample);
        ;
        //固定用插件获取查询总数
        PageInfo<DailyTrainStation> pageInfo = new PageInfo<>(dailyTrainStationList);

        PageVO<DailyTrainStationQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(dailyTrainStationList, DailyTrainStationQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        dailyTrainStationMapper.deleteByPrimaryKey(id);
    }

}
