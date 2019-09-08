package com.xuecheng.manage_cms_client.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.manage_cms_client.dao.CmsPageRespository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRespository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;

@Service
public class PageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageService.class);

    @Autowired
     GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    CmsSiteRespository cmsSiteRespository;

    @Autowired
    CmsPageRespository cmsPageRespository;

    //保存html页面到服务器物理路径
    public void savePageServicePath(String pageId) {
        //根据pageId查询cmsPage
        CmsPage cmsPage = this.findCmsPageById(pageId);
        //得到HTML的文件id,从cmsPage中获取htmlFileId的内容
        String htmlFileId = cmsPage.getHtmlFileId();
        //从gridfs查询HTML文件
        InputStream inputStream = this.getFileById(htmlFileId);
        if(inputStream == null){
            LOGGER.error("getFileById InputStream is null , htmlFile:{}",htmlFileId);
            return ;
        }
        //得到站点物理路径
        String siteId = cmsPage.getSiteId();
        //得到站点信息
        CmsSite cmsSite = this.findCmsSiteById(siteId);
        //得到站点路径
        String sitePhysicalPath = cmsSite.getSitePhysicalPath();
        //得到页面的物理路径
        String pagePath = sitePhysicalPath + cmsPage.getPagePhysicalPath() + cmsPage.getPageName();
        //将html文件保存到服务器物理路径上
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File(pagePath));
            //将文件内容保存到服务物理路径
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //根据gridfs中查询文件内容
    public InputStream getFileById(String fileId){
        //文件对象
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        //打开下载流
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //定义gridFsResource
        GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
        try {
            return gridFsResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    //根据页面id查询页面信息
    public CmsPage findCmsPageById(String pageId){
        Optional<CmsPage> optional = cmsPageRespository.findById(pageId);
        if(optional.isPresent()){
           return optional.get();
        }
        return  null;
    }
    //根据站点id查询页面信息
    public CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optional = cmsSiteRespository.findById(siteId);
        if(optional.isPresent()){
            return optional.get();
        }
        return  null;
    }
}
