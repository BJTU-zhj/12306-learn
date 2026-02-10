package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.DTO.StationQueryDTO;
import com.jiawa.train.business.DTO.StationSaveDTO;
import com.jiawa.train.business.VO.StationQueryVO;
import com.jiawa.train.business.domain.Station;
import com.jiawa.train.business.domain.StationExample;
import com.jiawa.train.business.mapper.StationMapper;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationService {

    private static final Logger LOG= LoggerFactory.getLogger(StationService.class);

    @Resource
    private StationMapper stationMapper;

    //保存
    public void save(StationSaveDTO stationSaveDTO){
        DateTime now=DateTime.now();
        Station station = BeanUtil.copyProperties(stationSaveDTO, Station.class);
        if(ObjectUtil.isEmpty(station.getId())) {
            station.setId(SnowUtil.getSnowflakeId());
            station.setCreateTime(now);
            station.setUpdateTime(now);
            stationMapper.insert(station);
        }else{
            station.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", station.getId());
            stationMapper.updateByPrimaryKey( station);
        }
    }

    //查询列表
    public PageVO<StationQueryVO> queryList(StationQueryDTO stationQueryDTO){
        StationExample stationExample = new StationExample();
        stationExample.setOrderByClause("id asc");
        StationExample.Criteria criteria = stationExample.createCriteria();

        PageHelper.startPage(stationQueryDTO.getPage(),stationQueryDTO.getSize());
        List<Station> stationList =stationMapper.selectByExample(stationExample);
        ;
        //固定用插件获取查询总数
        PageInfo<Station> pageInfo = new PageInfo<>(stationList);

        PageVO<StationQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(stationList, StationQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        stationMapper.deleteByPrimaryKey(id);
    }

    //查询所有的车站
    public List<StationQueryVO> queryAll(){
        StationExample stationExample = new StationExample();
        stationExample.setOrderByClause("name_pinyin asc");
        stationExample.createCriteria();
        List<Station> stationList = stationMapper.selectByExample(stationExample);
        return BeanUtil.copyToList(stationList,StationQueryVO.class);
    }

}
