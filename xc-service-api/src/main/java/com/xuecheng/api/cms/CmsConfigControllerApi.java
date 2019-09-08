package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsConfig;
import io.swagger.annotations.ApiOperation;


public interface CmsConfigControllerApi {

    @ApiOperation("根据id查询Cms配置信息")
    public CmsConfig getmodel(String id);



}
