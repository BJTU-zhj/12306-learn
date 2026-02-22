package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.DailyTrainCarriageQueryDTO;
import com.jiawa.train.business.DTO.DailyTrainCarriageSaveDTO;
import com.jiawa.train.business.VO.DailyTrainCarriageQueryVO;
import com.jiawa.train.business.domain.DailyTrainCarriage;
import com.jiawa.train.business.domain.DailyTrainCarriageExample;
import com.jiawa.train.business.domain.TrainCarriage;
import com.jiawa.train.business.enums.SeatColEnum;
import com.jiawa.train.business.mapper.DailyTrainCarriageMapper;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DailyTrainCarriageService {

    private static final Logger LOG= LoggerFactory.getLogger(DailyTrainCarriageService.class);

    @Resource
    private DailyTrainCarriageMapper dailyTrainCarriageMapper;

    @Resource
    private TrainCarriageService trainCarriageService;

    //保存
    public void save(DailyTrainCarriageSaveDTO dailyTrainCarriageSaveDTO){
        DateTime now=DateTime.now();
        DailyTrainCarriage dailyTrainCarriage = BeanUtil.copyProperties(dailyTrainCarriageSaveDTO, DailyTrainCarriage.class);
        //自动计算列数和总的座位数
        List<SeatColEnum> cols = SeatColEnum.getColsByType(dailyTrainCarriage.getSeatType());
        int seatCount=cols.size()*dailyTrainCarriage.getRowCount();
        dailyTrainCarriage.setColCount(cols.size());
        dailyTrainCarriage.setSeatCount(seatCount);

        if(ObjectUtil.isEmpty(dailyTrainCarriage.getId())) {
            dailyTrainCarriage.setId(SnowUtil.getSnowflakeId());
            dailyTrainCarriage.setCreateTime(now);
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriageMapper.insert(dailyTrainCarriage);
        }else{
            dailyTrainCarriage.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", dailyTrainCarriage.getId());
            dailyTrainCarriageMapper.updateByPrimaryKey( dailyTrainCarriage);
        }
    }

    //查询列表
    public PageVO<DailyTrainCarriageQueryVO> queryList(DailyTrainCarriageQueryDTO dailyTrainCarriageQueryDTO){
        DailyTrainCarriageExample dailyTrainCarriageExample = new DailyTrainCarriageExample();
        dailyTrainCarriageExample.setOrderByClause("date desc,train_code asc,`index` asc");
        DailyTrainCarriageExample.Criteria criteria = dailyTrainCarriageExample.createCriteria();
        //条件查询
        if(ObjectUtil.isNotEmpty(dailyTrainCarriageQueryDTO.getTrainCode())){
            criteria.andTrainCodeEqualTo(dailyTrainCarriageQueryDTO.getTrainCode());
        }
        if(ObjectUtil.isNotNull(dailyTrainCarriageQueryDTO.getDate())){
            criteria.andDateEqualTo(dailyTrainCarriageQueryDTO.getDate());
        }
        PageHelper.startPage(dailyTrainCarriageQueryDTO.getPage(),dailyTrainCarriageQueryDTO.getSize());
        List<DailyTrainCarriage> dailyTrainCarriageList =dailyTrainCarriageMapper.selectByExample(dailyTrainCarriageExample);
        ;
        //固定用插件获取查询总数
        PageInfo<DailyTrainCarriage> pageInfo = new PageInfo<>(dailyTrainCarriageList);

        PageVO<DailyTrainCarriageQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(dailyTrainCarriageList, DailyTrainCarriageQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        dailyTrainCarriageMapper.deleteByPrimaryKey(id);
    }

    //生成date天后trainCode每日车厢信息
    public void genDaily(Date date, String trainCode){
        LOG.info("开始生成{}的{}车次车厢每日数据", DateUtil.format(date, "yyyy-MM-dd"),trainCode);
        //删除该车次的所有车厢--某天的
        DailyTrainCarriageExample dailyTrainCarriageExample = new DailyTrainCarriageExample();
        dailyTrainCarriageExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode);
        dailyTrainCarriageMapper.deleteByExample(dailyTrainCarriageExample);
        //获取所有车厢
        List<TrainCarriage> trainCarriageList=trainCarriageService.selectByTrainCode(trainCode);
        if(CollUtil.isEmpty(trainCarriageList)){
            LOG.info("车次没有车厢，车次：{}", trainCode);
            return;
        }
        //插入所有车厢
        for (TrainCarriage trainCarriage : trainCarriageList){
            DateTime now=DateTime.now();
            DailyTrainCarriage dailyTrainCarriage=BeanUtil.copyProperties(trainCarriage,DailyTrainCarriage.class);
            dailyTrainCarriage.setId(SnowUtil.getSnowflakeId());
            dailyTrainCarriage.setCreateTime(now);
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriage.setDate(date);
            dailyTrainCarriageMapper.insert(dailyTrainCarriage);
        }
        LOG.info("结束生成{}的{}车次车厢每日数据", DateUtil.format(date, "yyyy-MM-dd"),trainCode);
    }

    //根据座位类型查询某车次某天车厢信息
    public List<DailyTrainCarriage> selectBySeatType(Date date, String trainCode, String seatType){
        DailyTrainCarriageExample dailyTrainCarriageExample = new DailyTrainCarriageExample();
        dailyTrainCarriageExample.setOrderByClause("`index` asc");
        dailyTrainCarriageExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode)
                .andSeatTypeEqualTo(seatType);
        return dailyTrainCarriageMapper.selectByExample(dailyTrainCarriageExample);
    }

}
