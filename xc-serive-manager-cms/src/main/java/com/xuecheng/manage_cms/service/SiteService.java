package com.xuecheng.manage_cms.service;


import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.manage_cms.dao.CmsSiteRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SiteService {
    @Autowired
    CmsSiteRespository cmsSiteRespository;


    public List<CmsSite> queryAll(){
       return cmsSiteRespository.findAll();
    }

}
