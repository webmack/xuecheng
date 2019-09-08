package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CmsPageRespository extends MongoRepository<CmsPage,String> {
    //校验页面名称、站点Id、页面webpath的唯一性
    CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName,String siteId, String webPath);


}
