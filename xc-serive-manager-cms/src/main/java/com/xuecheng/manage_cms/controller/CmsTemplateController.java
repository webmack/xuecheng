package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsSiteControllerApi;
import com.xuecheng.api.cms.CmsTemplateControllerApi;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.manage_cms.service.SiteService;
import com.xuecheng.manage_cms.service.TemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cms/template")
@Api(value="cms模板管理接口",description = "cms模板管理接口，提供页面的增、删、改、查")
public class CmsTemplateController implements CmsTemplateControllerApi{

    @Autowired
    private TemplateService templateService;

    @GetMapping("/list")
    @ApiOperation("查询所有模板")
    public List<CmsTemplate> list(){
        return templateService.queryAll();
    }
}
