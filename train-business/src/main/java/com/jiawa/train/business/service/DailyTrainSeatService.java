package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.DailyTrainSeatQueryDTO;
import com.jiawa.train.business.DTO.DailyTrainSeatSaveDTO;
import com.jiawa.train.business.VO.DailyTrainSeatQueryVO;
import com.jiawa.train.business.domain.DailyTrainSeat;
import com.jiawa.train.business.domain.DailyTrainSeatExample;
import com.jiawa.train.business.mapper.DailyTrainSeatMapper;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyTrainSeatService {

    private static final Logger LOG= LoggerFactory.getLogger(DailyTrainSeatService.class);

    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;

    //保存
    public void save(DailyTrainSeatSaveDTO dailyTrainSeatSaveDTO){
        DateTime now=DateTime.now();
        DailyTrainSeat dailyTrainSeat = BeanUtil.copyProperties(dailyTrainSeatSaveDTO, DailyTrainSeat.class);
        if(ObjectUtil.isEmpty(dailyTrainSeat.getId())) {
            dailyTrainSeat.setId(SnowUtil.getSnowflakeId());
            dailyTrainSeat.setCreateTime(now);
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeatMapper.insert(dailyTrainSeat);
        }else{
            dailyTrainSeat.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", dailyTrainSeat.getId());
            dailyTrainSeatMapper.updateByPrimaryKey( dailyTrainSeat);
        }
    }

    //查询列表
    public PageVO<DailyTrainSeatQueryVO> queryList(DailyTrainSeatQueryDTO dailyTrainSeatQueryDTO){
        DailyTrainSeatExample dailyTrainSeatExample = new DailyTrainSeatExample();
        dailyTrainSeatExample.setOrderByClause("date desc,train_code asc,carriage_index  asc");
        DailyTrainSeatExample.Criteria criteria = dailyTrainSeatExample.createCriteria();
        //条件查询
        if(ObjectUtil.isNotEmpty(dailyTrainSeatQueryDTO.getTrainCode())){
            criteria.andTrainCodeEqualTo(dailyTrainSeatQueryDTO.getTrainCode());
        }
        if(ObjectUtil.isNotNull(dailyTrainSeatQueryDTO.getDate())){
            criteria.andDateEqualTo(dailyTrainSeatQueryDTO.getDate());
        }

        PageHelper.startPage(dailyTrainSeatQueryDTO.getPage(),dailyTrainSeatQueryDTO.getSize());
        List<DailyTrainSeat> dailyTrainSeatList =dailyTrainSeatMapper.selectByExample(dailyTrainSeatExample);
        ;
        //固定用插件获取查询总数
        PageInfo<DailyTrainSeat> pageInfo = new PageInfo<>(dailyTrainSeatList);

        PageVO<DailyTrainSeatQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(dailyTrainSeatList, DailyTrainSeatQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        dailyTrainSeatMapper.deleteByPrimaryKey(id);
    }

}
