package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.business.DTO.SkTokenQueryDTO;
import com.jiawa.train.business.DTO.SkTokenSaveDTO;
import com.jiawa.train.business.VO.SkTokenQueryVO;
import com.jiawa.train.business.domain.SkToken;
import com.jiawa.train.business.domain.SkTokenExample;
import com.jiawa.train.business.mapper.SkTokenMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkTokenService {

    private static final Logger LOG= LoggerFactory.getLogger(SkTokenService.class);

    @Resource
    private SkTokenMapper skTokenMapper;

    //保存
    public void save(SkTokenSaveDTO skTokenSaveDTO){
        DateTime now=DateTime.now();
        SkToken skToken = BeanUtil.copyProperties(skTokenSaveDTO, SkToken.class);
        if(ObjectUtil.isEmpty(skToken.getId())) {
            skToken.setId(SnowUtil.getSnowflakeId());
            skToken.setCreateTime(now);
            skToken.setUpdateTime(now);
            skTokenMapper.insert(skToken);
        }else{
            skToken.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", skToken.getId());
            skTokenMapper.updateByPrimaryKey( skToken);
        }
    }

    //查询列表
    public PageVO<SkTokenQueryVO> queryList(SkTokenQueryDTO skTokenQueryDTO){
        SkTokenExample skTokenExample = new SkTokenExample();
        skTokenExample.setOrderByClause("id desc");
        SkTokenExample.Criteria criteria = skTokenExample.createCriteria();

        PageHelper.startPage(skTokenQueryDTO.getPage(),skTokenQueryDTO.getSize());
        List<SkToken> skTokenList =skTokenMapper.selectByExample(skTokenExample);
        ;
        //固定用插件获取查询总数
        PageInfo<SkToken> pageInfo = new PageInfo<>(skTokenList);

        PageVO<SkTokenQueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(skTokenList, SkTokenQueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        skTokenMapper.deleteByPrimaryKey(id);
    }

}
