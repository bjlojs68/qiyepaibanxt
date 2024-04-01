package com.bwtp.staffService.service.impl;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bwtp.staffService.entity.Classes;
import com.bwtp.staffService.entity.Scheduling;
import com.bwtp.staffService.entity.ServiceEmployee;
import com.bwtp.staffService.entity.vo.scheduleVo;
import com.bwtp.staffService.mapper.SchedulingMapper;
import com.bwtp.staffService.service.ClassesService;
import com.bwtp.staffService.service.EmployeeClassesService;
import com.bwtp.staffService.service.SchedulingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bwtp.staffService.service.ServiceEmployeeService;
import com.bwtp.staffService.utils.MyUserInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * <p>
 * 排班表 服务实现类
 * </p>
 *
 * @author blp
 * @since 2023-04-03
 */
@Service
public class SchedulingServiceImpl extends ServiceImpl<SchedulingMapper, Scheduling> implements SchedulingService {

    @Resource
    private SchedulingService schedulingService;

    @Resource
    private ClassesService classesService;

    @Resource
    private EmployeeClassesService employeeClassesService;

    @Resource
    private ServiceEmployeeService employeeService;

    public String[] getMouthClassesArray(String shopId){
        //存入班次id
        LocalDate localDate = LocalDate.of(2023, 1, 30);
        String[] mouthClassesArray = new String[classesService.list(null).size()];
        int a = 0; //mouthClassesArray  实时数据长度
        for (int i = 0; i < 35; i++) {
            LocalDate localDate1 = localDate.plusDays(i);
            //根据日期和startTime顺序存入班次id
            QueryWrapper<Classes> queryWrapper = new QueryWrapper<>();
            //时间从小到大顺序存入
            queryWrapper.eq("create_data", localDate1);
            queryWrapper.eq("shop_id", shopId);
            queryWrapper.orderByAsc("start_time");

            List<Classes> classesList = classesService.list(queryWrapper);

            for (int j = 0; j < classesList.size(); j++) {
                mouthClassesArray[j + a] = classesList.get(j).getId();
            }
            a = a + classesList.size();
        }
        return mouthClassesArray;
    }

    public String[] getEmployeeArray(String shopId){
        //存入员工id
        //根据门店获取员工数据
        QueryWrapper<ServiceEmployee> wrapper = new QueryWrapper<>();
        wrapper.eq("shop_id", shopId);
        //1,2,3,4顺序存入
        wrapper.orderByAsc("position");
        List<ServiceEmployee> employeeList = employeeService.list(wrapper);

        String[] employeeArray = new String[employeeList.size()];
        for (int j = 0; j < employeeList.size(); j++) {
            employeeArray[j] = employeeList.get(j).getId();
        }

        return employeeArray;
    }


/*
    @Override
    public List<scheduleVo> getMouthClassesList() {
        String[] mouthClassesArray = getMouthClassesArray();
        String[] employeeArray = getEmployeeArray();

        ArrayList<scheduleVo> voArrayList = new ArrayList<>();
        List<Scheduling> schedule= baseMapper.selectList(null);
        for (int i = 0; i < schedule.size(); i++) {
            int employeeSubscript=0;
            for (int j = 0; j < employeeArray.length; j++) {
                if (schedule.get(i).getEmployeeId().equals(employeeArray[j])){
                    employeeSubscript=j;
                }
            }
            ServiceEmployee serviceEmployee = employeeService.getById(employeeArray[employeeSubscript]);
            String name = serviceEmployee.getName();
            Integer position = serviceEmployee.getPosition();
            for (int j = 0; j < schedule.get(i).getRosterString().length(); j++) {
                scheduleVo scheduleVo = new scheduleVo();
                if (schedule.get(i).getRosterString().charAt(j)=='1'){
                    Classes classes = classesService.getById(mouthClassesArray[j]);

                    LocalTime startTime = classes.getStartTime();

                    LocalTime endTime = classes.getEndTime();
                    Date createData = classes.getCreateData();//util
                    //转为sql的date
                    long longTime = createData.getTime();
                    java.sql.Date createSqlData = new java.sql.Date(longTime);

                    scheduleVo.setEmployeeName(name);
                    scheduleVo.setPosition(position);
                    scheduleVo.setStartTime(startTime);
                    scheduleVo.setEndTime(endTime);
                    scheduleVo.setScheduleDate(createSqlData);
                }
                if (scheduleVo.getStartTime()!=null){
                    voArrayList.add(scheduleVo);
                }

            }
        }
        return voArrayList;
    }
*/

//    current  周数
    @Override
    public List<scheduleVo> getWeekClassesList(int current,String shopId) {

        String[] employeeArray = getEmployeeArray(shopId);
        String[] mouthClassesArray = getMouthClassesArray(shopId);


        int[] endStart=new int[2];
        int num=0;
        int beNum=0;
        LocalDate l=LocalDate.of(2023,1,30);
        for (int i = 0; i < 35; i++) { //用于判断b的值
            LocalDate date=l.plusDays(i);
            QueryWrapper<Classes> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("create_data",date);
            queryWrapper.eq("shop_id",shopId);
            queryWrapper.orderByAsc("start_time");

            int size = classesService.list(queryWrapper).size();
            beNum=num;
            num=size+num;

            //开始
            if (i==(current-1)*7){
                endStart[0]=beNum;
//                System.out.println(beNum+"beNum");
            }
            //结束
            if (i==(current*7-1)){
                endStart[1]=num;
//                System.out.println(num+"num");

            }
        }



        List<scheduleVo> weekClassesLists=new ArrayList<>();

        for (int i = 0; i <employeeArray.length; i++) {
            //根据员工id获取排班
            QueryWrapper<Scheduling> schedulingQueryWrapper = new QueryWrapper<>();
            schedulingQueryWrapper.eq("employee_id",employeeArray[i]);
            schedulingQueryWrapper.eq("sign",shopId);
            String rosterString = baseMapper.selectOne(schedulingQueryWrapper).getRosterString();
            ServiceEmployee byId = employeeService.getById(employeeArray[i]);
            Integer position = byId.getPosition();
            String name = byId.getName();
            for (int j = endStart[0]; j < endStart[1]; j++) {
                    if (rosterString.charAt(j)=='1'){  //为1则存入
                        Classes classesServiceById = classesService.getById(mouthClassesArray[j]);

                        LocalTime startTime = classesServiceById.getStartTime();
                        LocalTime endTime = classesServiceById.getEndTime();
                        Date createData =classesServiceById.getCreateData();
                        //转为sql的date
                        long longTime = createData.getTime();
                        java.sql.Date createSqlData = new java.sql.Date(longTime);

                        scheduleVo scheduleVo = new scheduleVo();
                        scheduleVo.setEmployeeName(name);
                        scheduleVo.setPosition(position);
                        scheduleVo.setStartTime(startTime);
                        scheduleVo.setEndTime(endTime);
                        scheduleVo.setScheduleDate(createSqlData);

                        weekClassesLists.add(scheduleVo);
                    }


            }
        }

        return weekClassesLists;
    }

    @Override
    public List<scheduleVo> getDayClassesList(Date creatDate,String shopId) {
        String[] employeeArray = getEmployeeArray(shopId);
        String[] mouthClassesArray = getMouthClassesArray(shopId);


        int[] endStart=new int[2];

        List<scheduleVo> scheduleVoList=new ArrayList<>();

        for (int i = 0; i < mouthClassesArray.length; i++) {
            if(classesService.getById(mouthClassesArray[i]).getCreateData().equals(creatDate)){
                endStart[0]=i;
                break;
            }
        }
        QueryWrapper<Classes> classesQueryWrapper = new QueryWrapper<>();
        classesQueryWrapper.eq("create_data",creatDate);
        classesQueryWrapper.eq("shop_id",shopId);
        classesQueryWrapper.orderByAsc("start_time");  //排序，从小到大
        List<Classes> list = classesService.list(classesQueryWrapper);
        endStart[1]=list.size()+endStart[0];

        for (int i = 0; i < employeeArray.length; i++) {
            QueryWrapper<Scheduling> schedulingQueryWrapper = new QueryWrapper<>();
            schedulingQueryWrapper.eq("employee_id",employeeArray[i]);
            schedulingQueryWrapper.eq("sign",shopId);
            String rosterString = baseMapper.selectOne(schedulingQueryWrapper).getRosterString();
            ServiceEmployee byId = employeeService.getById(employeeArray[i]);
            Integer position = byId.getPosition();
            String name = byId.getName();
            for (int j = endStart[0]; j < endStart[1]; j++) {
                if (rosterString.charAt(j)=='1'){
                    Classes classesServiceById = classesService.getById(mouthClassesArray[j]);

                    LocalTime startTime = classesServiceById.getStartTime();
                    LocalTime endTime = classesServiceById.getEndTime();
                    Date createData =classesServiceById.getCreateData();
                    //转为sql的date
                    long longTime = createData.getTime();
                    java.sql.Date createSqlData = new java.sql.Date(longTime);

                    scheduleVo scheduleVo = new scheduleVo();
                    scheduleVo.setEmployeeName(name);
                    scheduleVo.setPosition(position);
                    scheduleVo.setStartTime(startTime);
                    scheduleVo.setEndTime(endTime);
                    scheduleVo.setScheduleDate(createSqlData);

                    if (scheduleVo.getStartTime()!=null
                            &&scheduleVo.getEndTime()!=null
                            &&scheduleVo.getScheduleDate()!=null
                            &&scheduleVo.getPosition()!=null
                            &&scheduleVo.getEmployeeName()!=null){
                        scheduleVoList.add(scheduleVo);
                    }


                }
            }
        }


        return scheduleVoList;
    }

    //通过缓存名称获取key，根据key获取实际的值
    /** 名称无所谓，存的是方法返回的值，就相当于两个key一个value
     * value：指定缓存的名称，用于区分不同的缓存。可以在缓存管理器的配置中指定相应的缓存。
     * key：指定缓存的键值，用于存储和检索缓存的结果。可以使用Spring表达式来定义键值，例如 key = "#param" 表示使用方法的参数 param 作为键值。*/
    @Cacheable(value = "ScheduleString", key = "'dateTimeHashMap'")
    @Override
    public String getDataTimeRedis() {
        QueryWrapper<Scheduling> schedulingQueryWrapper = new QueryWrapper<>();
        schedulingQueryWrapper.eq("employee_id", MyUserInfo.serviceEmployee.getId());

        Scheduling scheduling = schedulingService.getOne(schedulingQueryWrapper);
        String rosterString = scheduling.getRosterString();

        /**rosterString是一个二进制字符串，他是根据classes班次模板得到的，0为有排班，1位无排班*/
        //获取当前周
        /*TODO 暂时固定值*/
        LocalDate currentDate = MyUserInfo.currentDate; // 获取当前日期

//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // 创建自定义格式的日期时间格式化器
//        String formattedDate = currentDate.format(formatter); // 格式化日期为字符串
        //根据当前周获取周班次
        /**
         * 7做闭环，直到
         * */

        //日期集合
        List<String> strings = new ArrayList<>();

        int num = 0;

        int dayValue = currentDate.getDayOfWeek().getValue();

        LocalDate currentDateM;
        LocalDate currentDateP;
        LocalDate startLocalDate = currentDate.minusDays(dayValue);

        Date startDate = Date.from(startLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        //获取日期集合
        while (num != 7) {


            //获取当天的日期后，后面的日期与7比较，
            //添加日期
            for (int j = dayValue - 1; j >= 0; j--) {
                num++;
                currentDateM = currentDate.minusDays(j);
                strings.add(currentDateM.toString());
            }

            for (int i = 1; i < 8 - dayValue; i++) {
                num++;
                currentDateP = currentDate.plusDays(i);
                strings.add(currentDateP.toString());
            }

        }


        //获取固定班次
        QueryWrapper<Classes> classesQueryWrapper = new QueryWrapper<>();
        classesQueryWrapper.eq("shop_id", MyUserInfo.serviceEmployee.getShopId());
        classesQueryWrapper.orderByAsc("create_data");
        List<Classes> classesList = classesService.list(classesQueryWrapper);
        //获取周固定班次范围
        String format = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(format);


        int pre = 0, next = 0;
        try {
            Date parseStart = sdf.parse(strings.get(0));

            for (int i = 0; i < classesList.size(); i++) {
                if (classesList.get(i).getCreateData().compareTo(parseStart) == 0) {
                    pre = i;
                    break;
                }
            }
            Date parseEnd = sdf.parse(strings.get(strings.size() - 1));
            for (int i = 0; i < classesList.size(); i++) {
                if (classesList.get(i).getCreateData().compareTo(parseEnd) > 0) {
                    next = i;
                    break;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //范围是pre<= d < next
        String s = "";
        LinkedHashMap<String, String> dateTimeHashMap = new LinkedHashMap<>();

        for (int i = 0; i < strings.size(); i++) {
            s = "";
            for (int j = pre; j < next; j++) {



                try {
                    if (sdf.parse(strings.get(i)).compareTo(classesList.get(j).getCreateData()) == 0) {
                        if (rosterString.getBytes()[j] == '1') {
                            if (!s.isEmpty()){
                                s=s+"     ";
                            }
                            s = s+classesList.get(j).getStartTime().toString()+"-"+ classesList.get(j).getEndTime().toString();
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            dateTimeHashMap.put(strings.get(i), s);
        }

        //转json
        ObjectMapper objectMapper = new ObjectMapper();
        String dateTimeHashMapa = null;
        try {
            dateTimeHashMapa = objectMapper.writeValueAsString(dateTimeHashMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return dateTimeHashMapa;
    }


}
