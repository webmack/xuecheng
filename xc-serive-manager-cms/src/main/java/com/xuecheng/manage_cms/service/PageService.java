package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRespository;
import com.xuecheng.manage_cms.dao.CmsSiteRespository;
import com.xuecheng.manage_cms.dao.CmsTemplateRespository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PageService {
    @Autowired
    CmsPageRespository cmsPageRespository;

    @Autowired
    CmsConfigRepository cmsConfigRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CmsTemplateRespository cmsTemplateRespository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    CmsSiteRespository cmsSiteRespository;
    /**
     * 页面查询方法
     * @param page 页码，从1开始计数
     * @param size 每页记录数
     * @param queryPageRequest 查询条件
     * @return
     */
    public QueryResponseResult findList(@PathVariable("page") int page, @PathVariable("size") int size, QueryPageRequest queryPageRequest){
       if(queryPageRequest == null){
           queryPageRequest = new QueryPageRequest();
       }
        //自定义条件查询
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());
        //条件值对象
        CmsPage cmsPage = new CmsPage();
        //设置条件值
        if(StringUtils.isNotEmpty(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //设置模板id
        if(StringUtils.isNotEmpty(queryPageRequest.getTemplateId())){
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        //设置页面别名作为页面查询条件
        if(StringUtils.isNotEmpty(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //定义条件对象
        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);


        if(page < 0){
            page = 1;
        }
        page = page -1;
        if(size <= 0){
            size = 10;
        }
        Pageable pageable = PageRequest.of(page, size);
        //实现自定义查询和分页查询
        Page<CmsPage> all = cmsPageRespository.findAll(example,pageable);
        QueryResult queryResult = new QueryResult();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());
        QueryResponseResult queryResponseResult =new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

//    //新增页面
//    public CmsPageResult add(CmsPage cmsPage){
//        //校验页面名称、站点Id、页面webpath的唯一性
//        CmsPage cmsPage1 = cmsPageRespository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
//        if(cmsPage1 == null){
//            cmsPage.setPageId(null);
//            cmsPageRespository.save(cmsPage);
//            return  new  CmsPageResult(CommonCode.SUCCESS,cmsPage);
//        }
//        return new   CmsPageResult(CommonCode.FAIL,null);
//    }
//新增页面
public CmsPageResult add(CmsPage cmsPage){
    //校验页面名称、站点Id、页面webpath的唯一性
    CmsPage cmsPage1 = cmsPageRespository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
    if(cmsPage1 != null){
        ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
    }
    cmsPage.setPageId(null);
    cmsPageRespository.save(cmsPage);
    return  new  CmsPageResult(CommonCode.SUCCESS,cmsPage);
}


    //根据id查询页面
    public CmsPage getById(String id){
        Optional<CmsPage> optional = cmsPageRespository.findById(id);
        if(optional.isPresent()){
            CmsPage cmsPage = optional.get();
            return cmsPage;
        }
        return null;
    }

    //修改页面
    public CmsPageResult update(String id,CmsPage cmsPage){
        //根据id从数据库查询页面信息
        CmsPage cmsPage1 = this.getById(id);
        if(cmsPage1 != null){
            //装备更新数据
            //设置要修改的数据
            cmsPage1.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            cmsPage1.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            cmsPage1.setPageAliase(cmsPage.getPageAliase());
             //更新页面名称
            cmsPage1.setPageName(cmsPage.getPageName());
             //更新访问路径
            cmsPage1.setPageWebPath(cmsPage.getPageWebPath());
            // 更新物理路径
            cmsPage1.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新dataUrl
            cmsPage1.setDataUrl(cmsPage.getDataUrl());
            // 执行更新
            CmsPage save = cmsPageRespository.save(cmsPage1);
            return new CmsPageResult(CommonCode.SUCCESS,cmsPage1);
        }
        //修改失败
        return new CmsPageResult(CommonCode.FAIL,null);
    }

    //删除页面
    public ResponseResult delete(String id){
        //先查询一下
        Optional<CmsPage> optional = cmsPageRespository.findById(id);
        if(optional.isPresent()){
            cmsPageRespository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    //根据id查询cmsConfig信息
    public CmsConfig getConfigById(String id){
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if(optional.isPresent()){
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }
        return  null;
    }
    /**
     * 静态化程序获取页面的dataurl
     *
     * 静态化晨曦远程请求dataurl获取数据模型
     *
     * 静态化程序获取页面的模板信息
     *
     * 执行页面静态化
     */
    //页面静态化方法
    public String getPageHtml(String pageId){
        //获取数据模型
        Map model = getModelByPageId(pageId);
        if(model == null){
            //数据模型获取不到
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //获取页面模板
        String template = getTemplatePageId(pageId);
        if(StringUtils.isEmpty(template)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //执行静态化方法
        String html = generateHtml(template, model);
        return html;
    }
    //执行静态化方法
    private String generateHtml(String templateContent,Map model){
        //创建配置对象
        Configuration configuration = new Configuration(freemarker.template.Configuration.getVersion());
        //创建模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",templateContent);
        //Configuration配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        //获取模板
        try {
            Template template = configuration.getTemplate("template");
            //调用挨批进行静态化
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    //获取页面模板名称
    private String getTemplatePageId(String pageId){
        //取出页面的信息
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //获取页面模板id
         String templateId = cmsPage.getTemplateId();
        if(StringUtils.isEmpty(templateId)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //查询模板信息
        Optional<CmsTemplate> optional = cmsTemplateRespository.findById(templateId);
        if(optional.isPresent()){
            CmsTemplate cmsTemplate = optional.get();
            //获取模板文件id
            String templateFileId = cmsTemplate.getTemplateFileId();
            //从gridfs中获取模板文件
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));

            //打开一个下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建gridResour对象获取流
            GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
            //从流中获取数据
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  null;
    }

    //获取数据模型
    private Map getModelByPageId(String pageId){
        //取出页面的信息
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //取出dataurl
        String dataUrl = cmsPage.getDataUrl();
        if(StringUtils.isEmpty(dataUrl)){
            //页面dataurl为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //通过restTemplate请求dataurl获取数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;
    }
    //页面发布
    public ResponseResult post(String pageId){
        //执行页面的静态化
        String pageHtml = this.getPageHtml(pageId);
        //将页面静态化文件存放到GridFs中
        saveHtml(pageId,pageHtml);
        //向MQ发送消息
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //向MQ发消息
    private  void sendPostPage(String pageId){
        //得到页面信息
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //创建消息对象
        Map<String,String> msg = new HashMap<>();
        msg.put("pageId",pageId);
        //转换json串
        String jsonString = JSON.toJSONString(msg);
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,cmsPage.getSiteId(),jsonString);
    }

    //保存HTML到GridFs中
    private CmsPage saveHtml(String pageId,String htmlContent){
        //得到页面信息
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        ObjectId objectId = null;
        try {
            //将HTMLcontent转换为输入流
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "utf-8");
            //将HTML文件内容保存到Gridfs
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //将HTML文件id更新cmspage中
        cmsPage.setHtmlFileId(objectId.toHexString());
        cmsPageRespository.save(cmsPage);
        return cmsPage;
    }
    public  CmsPageResult save(CmsPage cmsPage){
        //判断页面是否存在
        CmsPage cmsPage1 = cmsPageRespository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(cmsPage1 != null){
           return this.update(cmsPage1.getPageId(),cmsPage1);
        }
        return this.add(cmsPage);
    }

    public CmsPostPageResult postPageQuick(CmsPage cmsPage){
        //将页面信息存储到CMSPage集合中
        CmsPageResult save = this.save(cmsPage);
        if(!save.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //得到页面ID
        CmsPage cmsPageSave = save.getCmsPage();
        String pageId = cmsPageSave.getPageId();
        //执行页面发布（先静态化、保存GridFs 向MQ发送信息）
        ResponseResult post = this.post(pageId);
        if(!post.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //拼接页面URL
        //取出站点id
        String siteId = cmsPageSave.getSiteId();
        CmsSite cmsSite = this.findCmsSiteById(siteId);
        //页面Url
        String pageUrl = cmsSite.getSiteDomain() + cmsSite.getSiteWebPath() + cmsPageSave.getPageWebPath() + cmsPageSave.getPageName();
        return new CmsPostPageResult(CommonCode.SUCCESS,pageUrl);

    }

    //根据站点id查询站点信息
    public CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optional = cmsSiteRespository.findById(siteId);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }
}
