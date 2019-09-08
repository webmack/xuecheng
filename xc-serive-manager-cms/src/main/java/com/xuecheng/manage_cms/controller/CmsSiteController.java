package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsSiteControllerApi;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.manage_cms.service.SiteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cms/site")
@Api(value="cms站点管理接口",description = "cms站点管理接口，提供页面的增、删、改、查")
public class CmsSiteController implements CmsSiteControllerApi {

    @Autowired
    private SiteService siteService;

    @GetMapping("/list")
    @ApiOperation("查询所有站点")
    public List<CmsSite> list(){
        return siteService.queryAll();
    }
}
