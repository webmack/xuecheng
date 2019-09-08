package com.xuecheng.manage_cms.service;


import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.manage_cms.dao.CmsSiteRespository;
import com.xuecheng.manage_cms.dao.CmsTemplateRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TemplateService {
    @Autowired
    CmsTemplateRespository cmsTemplateRespository;


    public List<CmsTemplate> queryAll(){
       return cmsTemplateRespository.findAll();
    }

}
