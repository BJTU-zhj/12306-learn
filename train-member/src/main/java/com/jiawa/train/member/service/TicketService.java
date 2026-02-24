package com.jiawa.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.member.DTO.TicketQueryDTO;
import com.jiawa.train.member.DTO.TicketSaveDTO;
import com.jiawa.train.member.VO.TicketQueryVO;
import com.jiawa.train.member.domain.Ticket;
import com.jiawa.train.member.domain.TicketExample;
import com.jiawa.train.member.mapper.TicketMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private static final Logger LOG= LoggerFactory.getLogger(TicketService.class);

    @Resource
    private TicketMapper ticketMapper;

    //保存
    public void save(TicketSaveDTO ticketSaveDTO){
        DateTime now=DateTime.now();
        Ticket ticket = BeanUtil.copyProperties(ticketSaveDTO, Ticket.class);
        if(ObjectUtil.isEmpty(ticket.getId())) {
            ticket.setId(SnowUtil.getSnowflakeId());
            ticket.setCreateTime(now);
            ticket.setUpdateTime(now);
            ticketMapper.insert(ticket);
        }else{
            ticket.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", ticket.getId());
            ticketMapper.updateByPrimaryKey( ticket);
        }
    }

    //查询列表
    public PageVO<TicketQueryVO> queryList(TicketQueryDTO ticketQueryDTO){
        TicketExample ticketExample = new TicketExample();
        ticketExample.setOrderByClause("id desc");
        TicketExample.Criteria criteria = ticketExample.createCriteria();

        PageHelper.startPage(ticketQueryDTO.getPage(),ticketQueryDTO.getSize());
        List<Ticket> ticketList =ticketMapper.selectByExample(ticketExample);
        ;
        //固定用插件获取查询总数
        PageInfo<Ticket> pageInfo = new PageInfo<>(ticketList);

        PageVO<TicketQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(ticketList, TicketQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        ticketMapper.deleteByPrimaryKey(id);
    }

}
