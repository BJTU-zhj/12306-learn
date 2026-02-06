package com.jiawa.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.member.DTO.PaseengerQueryDTO;
import com.jiawa.train.member.DTO.PassengerSaveDTO;
import com.jiawa.train.member.VO.PassengerQueryVO;
import com.jiawa.train.member.domain.Passenger;
import com.jiawa.train.member.domain.PassengerExample;
import com.jiawa.train.member.mapper.PassengerMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PassengerService {

    private static final Logger LOG= LoggerFactory.getLogger(PassengerService.class);

    @Resource
    private PassengerMapper passengerMapper;

    //保存
    public void save(PassengerSaveDTO passengerSaveDTO){
        DateTime now=DateTime.now();
        Passenger passenger = BeanUtil.copyProperties(passengerSaveDTO, Passenger.class);
        if(ObjectUtil.isEmpty(passenger.getId())) {
            passenger.setMemberId(LoginMemberContext.getId());
            passenger.setId(SnowUtil.getSnowflakeId());
            passenger.setCreateTime(now);
            passenger.setUpdateTime(now);
            passengerMapper.insert(passenger);
        }else{
            passenger.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", passenger.getId());
            passengerMapper.updateByPrimaryKey( passenger);
        }
    }

    //查询列表
    public PageVO<PassengerQueryVO> queryList(PaseengerQueryDTO paseengerQueryDTO){
        PassengerExample passengerExample = new PassengerExample();
        passengerExample.setOrderByClause("id desc");
        PassengerExample.Criteria criteria = passengerExample.createCriteria();
        if(ObjectUtil.isNotEmpty(paseengerQueryDTO.getMemberId())){
            criteria.andMemberIdEqualTo(paseengerQueryDTO.getMemberId());
        }
        PageHelper.startPage(paseengerQueryDTO.getPage(),paseengerQueryDTO.getSize());
        List<Passenger> passengerList =passengerMapper.selectByExample(passengerExample);
        ;
        //固定用插件获取查询总数
        PageInfo<Passenger> pageInfo = new PageInfo<>(passengerList);

        PageVO<PassengerQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(passengerList, PassengerQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        passengerMapper.deleteByPrimaryKey(id);
    }

}
