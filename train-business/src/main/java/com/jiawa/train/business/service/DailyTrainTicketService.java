package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.business.DTO.DailyTrainTicketQueryDTO;
import com.jiawa.train.business.DTO.DailyTrainTicketSaveDTO;
import com.jiawa.train.business.VO.DailyTrainTicketQueryVO;
import com.jiawa.train.business.domain.DailyTrainTicket;
import com.jiawa.train.business.domain.DailyTrainTicketExample;
import com.jiawa.train.business.mapper.DailyTrainTicketMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyTrainTicketService {

    private static final Logger LOG= LoggerFactory.getLogger(DailyTrainTicketService.class);

    @Resource
    private DailyTrainTicketMapper dailyTrainTicketMapper;

    //保存
    public void save(DailyTrainTicketSaveDTO dailyTrainTicketSaveDTO){
        DateTime now=DateTime.now();
        DailyTrainTicket dailyTrainTicket = BeanUtil.copyProperties(dailyTrainTicketSaveDTO, DailyTrainTicket.class);
        if(ObjectUtil.isEmpty(dailyTrainTicket.getId())) {
            dailyTrainTicket.setId(SnowUtil.getSnowflakeId());
            dailyTrainTicket.setCreateTime(now);
            dailyTrainTicket.setUpdateTime(now);
            dailyTrainTicketMapper.insert(dailyTrainTicket);
        }else{
            dailyTrainTicket.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", dailyTrainTicket.getId());
            dailyTrainTicketMapper.updateByPrimaryKey( dailyTrainTicket);
        }
    }

    //查询列表
    public PageVO<DailyTrainTicketQueryVO> queryList(DailyTrainTicketQueryDTO dailyTrainTicketQueryDTO){
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        dailyTrainTicketExample.setOrderByClause("id desc");
        DailyTrainTicketExample.Criteria criteria = dailyTrainTicketExample.createCriteria();

        PageHelper.startPage(dailyTrainTicketQueryDTO.getPage(),dailyTrainTicketQueryDTO.getSize());
        List<DailyTrainTicket> dailyTrainTicketList =dailyTrainTicketMapper.selectByExample(dailyTrainTicketExample);
        ;
        //固定用插件获取查询总数
        PageInfo<DailyTrainTicket> pageInfo = new PageInfo<>(dailyTrainTicketList);

        PageVO<DailyTrainTicketQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(dailyTrainTicketList, DailyTrainTicketQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        dailyTrainTicketMapper.deleteByPrimaryKey(id);
    }

}
