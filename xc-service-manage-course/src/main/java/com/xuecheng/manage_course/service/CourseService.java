package com.xuecheng.manage_course.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    TeachplanRepository teachplanRepository;

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    TeachplanMapper teachplanMapper;
    
    @Autowired
    CourseMapper courseMapper;

    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    CoursePicMapper coursePicMapper;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Autowired
    CmsPageClient cmsPageClient;


    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;
    //课程计划的查询
    public TeachplanNode findTeachplanList(String courseId){
        return  teachplanMapper.selectList(courseId);
    }

    //添加课程计划
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan){
        if(teachplan == null || StringUtils.isEmpty(teachplan.getCourseid()) || StringUtils.isEmpty(teachplan.getPname())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //课程计划
        String courseid = teachplan.getCourseid();
        String parentid = teachplan.getParentid();
        //parentid
        if(StringUtils.isEmpty(parentid)){
            //取出该该课程的根节点
            parentid = this.getTeachplanRoot(courseid);
        }

        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan parentNode = optional.get();
        //父节点的级别
        String grade = parentNode.getGrade();
        //新节点
        Teachplan teachplanNew = new Teachplan();
        //将页面提交的teachplan信息拷贝到teachplanNew对象中
        BeanUtils.copyProperties(teachplan ,teachplanNew);
        teachplanNew.setParentid(parentid);
        teachplanNew.setCourseid(courseid);
        if(grade.equals("1")){
            teachplanNew.setGrade("2");
        }else{
            teachplanNew.setGrade("3");
        }
        teachplanRepository.save(teachplanNew);

        return  new ResponseResult(CommonCode.SUCCESS);
    }
    //查询课程根节点，如果查询不到根节点自动添加根节点
    private String getTeachplanRoot(String courseId){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(!optional.isPresent()){
            return  null;
        }
        CourseBase courseBase = optional.get();
        //查询课程的根节点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId,"0");
        if(teachplanList == null || teachplanList.size() <=0){
            //查询不到，自动添加根节点
            Teachplan teachplan = new Teachplan();
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setPname(courseBase.getName());
            teachplan.setCourseid(courseId);
            teachplan.setStatus("0");
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        //返回根节点
        return teachplanList.get(0).getId();
    }

    //查询课程信息
    public QueryResponseResult  findCourseList(int page, int size , CourseListRequest courseListRequest){
       if(courseListRequest == null){
          courseListRequest = new CourseListRequest();
       }
       if(page <=0){
          page = 0;
       }
       if(size <= 0){
           size = 20;
       }
       //设置页面参数
        PageHelper.startPage(page,size);
       //查询分页
        Page<CourseInfo> courseList = courseMapper.findCourseList(courseListRequest);
        //查询列表
        List<CourseInfo> result = courseList.getResult();
        //总记录数
        long total = courseList.getTotal();
        QueryResult queryResult = new QueryResult();
        queryResult.setList(result);
        queryResult.setTotal(total);
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    //向课程管理添加课程与图片的相关信息
    @Transactional
    public ResponseResult addCoursePic(String courseId ,String pic){
        CoursePic coursePic = null;
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        if(picOptional.isPresent()){
            coursePic = picOptional.get();
        }
        if(coursePic == null){
            coursePic =new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //查询课程图片
    public CoursePic findCoursePic(String courseId){
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            return coursePic;
        }
        return null;
    }

    //删除课程图片
    public ResponseResult deleteCoursePic(String courseId){
        long result = coursePicMapper.deleteByCourseid(courseId);
        if(result >0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
     }

     //查询课程详情 包括基本信息 图片 营销 课程计划
    public CourseView getCourseView(String id){
        CourseView courseView = new CourseView();
        //查询课程基本信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if(courseBaseOptional.isPresent()){
            courseView.setCourseBase(courseBaseOptional.get());
        }
        //查询课程图片
        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(id);
        if(courseBaseOptional.isPresent()){
            courseView.setCoursePic(coursePicOptional.get());
        }

        //课程营销信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
        if(courseBaseOptional.isPresent()){
            courseView.setCourseMarket(courseMarketOptional.get());
        }
        //课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }

    //根据id查询课程基本信息
    public CourseBase findCourseBaseById(String courseId){
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
        if(baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            return courseBase;
        }
        ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
        return null;
    }

    public CoursePublishResult preview(String id){
        //查询课程
        CourseBase courseBase = this.findCourseBaseById(id);
        //请求cms添加页面
        //准备cmsPage信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点id
        cmsPage.setDataUrl(publish_dataUrlPre+id);//数据模型url
        cmsPage.setPageName(id+".html");//页面名称
        cmsPage.setPageAliase(courseBase.getName());//页面别名
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面物理路径
        cmsPage.setPageWebPath(publish_page_webpath);//页面webPath
        cmsPage.setTemplateId(publish_templateId);

        CmsPageResult cmsPageResult = cmsPageClient.saveCmsPage(cmsPage);
        if(!cmsPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        String pageId = cmsPage1.getPageId();
        //拼装页面的预览的路劲
        String url = previewUrl+pageId;
        return new CoursePublishResult(CommonCode.SUCCESS,url);
    }
    //课程发布
    @Transactional
    public CoursePublishResult publish(String id){
        //查询课程
        CourseBase courseBase = this.findCourseBaseById(id);
        //调用cms一键发布接口将课程详情页面发布到服务器
        //装备页面id
        //准备cmsPage信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点id
        cmsPage.setDataUrl(publish_dataUrlPre+id);//数据模型url
        cmsPage.setPageName(id+".html");//页面名称
        cmsPage.setPageAliase(courseBase.getName());//页面别名
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面物理路径
        cmsPage.setPageWebPath(publish_page_webpath);//页面webPath
        cmsPage.setTemplateId(publish_templateId);

        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if(!cmsPostPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //保存课程的发布状态(已发布)
        CourseBase courseBase1 = this.saveCoursePubState(id);
        if(courseBase1 == null){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        //保存课程索引信息
        //缓存课程的信息
        String pageUrl = cmsPostPageResult.getPageUrl();
        return  new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    //更改课程状态为已发布 202002
    private CourseBase saveCoursePubState(String courseId){
        CourseBase courseBase = this.findCourseBaseById(courseId);
        courseBase.setStatus("202002");
        courseBaseRepository.save(courseBase);
        return courseBase;
    }
}
