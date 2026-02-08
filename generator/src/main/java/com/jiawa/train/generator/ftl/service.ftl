package com.jiawa.train.${module}.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.${module}.DTO.${Domain}QueryDTO;
import com.jiawa.train.${module}.DTO.${Domain}SaveDTO;
import com.jiawa.train.${module}.VO.${Domain}QueryVO;
import com.jiawa.train.${module}.domain.${Domain};
import com.jiawa.train.${module}.domain.${Domain}Example;
import com.jiawa.train.${module}.mapper.${Domain}Mapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ${Domain}Service {

    private static final Logger LOG= LoggerFactory.getLogger(${Domain}Service.class);

    @Resource
    private ${Domain}Mapper ${domain}Mapper;

    //保存
    public void save(${Domain}SaveDTO ${domain}SaveDTO){
        DateTime now=DateTime.now();
        ${Domain} ${domain} = BeanUtil.copyProperties(${domain}SaveDTO, ${Domain}.class);
        if(ObjectUtil.isEmpty(${domain}.getId())) {
            ${domain}.setId(SnowUtil.getSnowflakeId());
            ${domain}.setCreateTime(now);
            ${domain}.setUpdateTime(now);
            ${domain}Mapper.insert(${domain});
        }else{
            ${domain}.setUpdateTime( now);
            LOG.info("开始更新乘客信息,id:{}", ${domain}.getId());
            ${domain}Mapper.updateByPrimaryKey( ${domain});
        }
    }

    //查询列表
    public PageVO<${Domain}QueryVO> queryList(${Domain}QueryDTO ${domain}QueryDTO){
        ${Domain}Example ${domain}Example = new ${Domain}Example();
        ${domain}Example.setOrderByClause("id desc");
        ${Domain}Example.Criteria criteria = ${domain}Example.createCriteria();

        PageHelper.startPage(${domain}QueryDTO.getPage(),${domain}QueryDTO.getSize());
        List<${Domain}> ${domain}List =${domain}Mapper.selectByExample(${domain}Example);
        ;
        //固定用插件获取查询总数
        PageInfo<${Domain}> pageInfo = new PageInfo<>(${domain}List);

        PageVO<${Domain}QueryVO> pageVO = new PageVO<>();
        pageVO.setList(BeanUtil.copyToList(${domain}List, ${Domain}QueryVO.class));
        pageVO.setTotal(pageInfo.getTotal());

        return pageVO;
    }

    //删除,根据id
    public void delete(Long id){
        ${domain}Mapper.deleteByPrimaryKey(id);
    }

}
