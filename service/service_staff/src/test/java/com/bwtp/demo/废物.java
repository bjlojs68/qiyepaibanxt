package com.bwtp.demo;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.Date;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bwtp.commonbases.exceptionhandler.BwtpException;
import com.bwtp.staffService.entity.Classes;
import com.bwtp.staffService.entity.EmployeeClasses;
import com.bwtp.staffService.entity.EmployeePreference;
import com.bwtp.staffService.entity.ServiceEmployee;
import com.bwtp.staffService.service.*;
import com.bwtp.staffService.staffApplication;
import lombok.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 以下是实现步骤：
 * <p>
 * 根据班次对象数据生成班次列表，并按照开始时间和结束时间进行排序。
 * 根据员工数据和优先级规则生成员工列表，其中优先级规则为固定规则大于自定义规则大于偏好规则。
 * 按照优先级依次处理每个员工的班次分配：
 * 对于固定规则，首先检查该员工是否符合最多工作时长以及每日最多工作时长的限制。
 * 将符合条件的班次按照员工偏好进行筛选，得到可用的班次列表。
 * 对于自定义规则，检查已经排班的员工是否满足一周内工作时长不少于28小时的要求，以及无班次天数不超过两天的要求。
 * 对于偏好规则，将可用班次按照工作日偏好、工作时间偏好和班次时长偏好进行筛选，得到最终的班次列表。
 * 从可用班次列表中选择符合连续工作时长的最长班次，并将其分配给该员工。如果没有符合要求的班次，则将该员工标记为未分配班次。
 * 使用遗传算法对未分配班次的员工进行重新排班：
 * 初始化种群，其中每个个体表示一种员工排班方案。
 * 对每个个体，随机选择未分配班次的员工，并在可用班次列表中选择一个班次进行分配。
 * 计算每个个体的适应度，其中适应度包括未分配班次的数量和员工工时数。
 * 进行遗传操作（交叉、变异等），生成新的种群。
 * 重复以上步骤，直至达到指定的迭代次数或者找到满足约束条件的员工排班方案。
 * 计算员工工时数，并返回最优员工排班数据。
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = staffApplication.class)
public class 废物 {

    @Resource
    private ClassesService classesService;

    @Resource
    private EmployeeClassesService employeeClassesService;

    @Resource
    private ServiceEmployeeService employeeService;

    @Resource
    private EmployeePreferenceService employeePreferenceService;

    @Resource
    private PreferenceService preferenceService;


    //午餐时间
    private LocalTime lunchStartTime=LocalTime.of(11,00);
    private LocalTime lunchEndTime=LocalTime.of(14,00);

    //晚餐时间
    private LocalTime dinnerStartTime=LocalTime.of(17,00);
    private LocalTime dinnerEndTime=LocalTime.of(20,00);

    //班次最多时间 小时
    private Integer classesMaxTime = 4;

    //班次至少时间 小时
    private Integer classesMinTime = 2;

    //日最多工作时长 小时
    private Integer dayWorkTime = 8;

    //周最多工作时长 小时
    private Integer weekWorkTime;

    //月做多工作时长 小时
    private Integer mouthWorkTime;

    //休息时长 30分钟
    private Integer restDuration=30;

    public LocalTime getStartWorkTime() {
        return startWorkTime;
    }

    public void setStartWorkTime(LocalTime startWorkTime) {
        this.startWorkTime = startWorkTime;
    }

    public LocalTime getEndWorkTime() {
        return endWorkTime;
    }

    public void setEndWorkTime(LocalTime endWorkTime) {
        this.endWorkTime = endWorkTime;
    }

    //开始工作时间
    private LocalTime startWorkTime;
    //下班时间
    private LocalTime endWorkTime;



    /**为了表示每个员工的排班情况，我们可以采用二进制编码的方式。
     * 例如，如果某个员工在第一个班次工作，则为1，否则为0。
     * 同样，我们需要表示所有员工在一个周期内的排班情况，
     * 可以采用一个长度为N*M的二进制串来表示，其中N为员工数，M为周期内班次总数。
     * */
    /**
     * 如：
     * N*M
     * --------------------------------
     * 员工id数组下标\班次id数组下标
     * --------------------------------
     *    0 1 2 3 4 5 6 7 8 9
     *  1 1 0 1 0 0 0 0 0 0 0
     *  2 0 0 1 0 0 0 0 0 0 0
     *  3 1 0 0 0 0 0 1 0 0 0
     *  4 0 0 1 0 0 0 1 0 0 0
     * */

    //工具方法
    //将时间转为分钟，遇到0点将其转为24*60
    public Long timeToMinute(LocalTime localTime){
        long length=localTime.getHour()*60+localTime.getMinute();
        if (localTime.getHour()==0){
            length=24*60;
        }
        return length;
    }



    //不，这里就是按天
    @Data
    public  class ScheduleByOne {
        private int[][] employeeClasses;   //1,0,表示有员工，0表示没有，N*M，N为员工数，M为班次数
        private String[] employeeArray;    //储存员工id
        private String[] classesArray;     //存储班次id
        private LocalDate localDate;       //时间  必须设置


        //工具方法，根据员工id获取员工数组里的数组下标
        public Integer getSubscript(String employeeId){
            int employeeSubscript=0;
            for (int i = 0; i < employeeArray.length; i++) {
                if (employeeArray[i].equals(employeeId)){
                    employeeSubscript=i;
                    break;
                }
            }
            return employeeSubscript;
        }


        // 获取未排满的班次列表，纵轴1的数是否为该班次所需数
        public List<Classes> getUnfilledClassesList() {

            ArrayList<Classes> arrayList = new ArrayList<>();
            for (int i = 0; i <classesArray.length; i++) {
                Classes classes = new Classes();
                int count=0;
                for (int j = 0; j < employeeArray.length; j++) {
                    if (employeeClasses[j][i]==1){
                        count++;
                    }
                }
                Classes byId = classesService.getById(classesArray[i]);
                int b=byId.getEmployeeNumber()-count;
                if (b>0){
                    classes.setId(classesArray[i]);
                    classes.setStartTime(byId.getStartTime());
                    classes.setEndTime(byId.getEndTime());
                    classes.setEmployeeNumber(b);
                    arrayList.add(classes);
                }
            }
            return arrayList;
        }


        // 获取员工日排班总时长
        public Long getDayTotalDuration(String employeeId,ScheduleByOne scheduleByOne) {
            int b = isTrueRestDuration(employeeId, scheduleByOne);
            //根据员工id获取员工数组里的数组下标
            int employeeSubscript=getSubscript(employeeId);
            //根据下标在employeeClasses中获取该员工的工作时长
            long dayWorkTimeLength=0;
            for (int i = 0; i < employeeClasses.length; i++) {
                if (employeeClasses[employeeSubscript][i]==1){
                    LocalTime startTime = classesService.getById(classesArray[i]).getStartTime();
                    LocalTime endTime = classesService.getById(classesArray[i]).getEndTime();
                    dayWorkTimeLength=timeToMinute(endTime)-timeToMinute(startTime)+dayWorkTimeLength;
                }
            }
            return dayWorkTimeLength-(long)(30*b);
        }

        //获取员工日连续工作时长
        public Long getContinuousDuration(String employeeId) {
            //根据员工id获取员工数组里的数组下标
            int employeeSubscript=getSubscript(employeeId);
            //直接看该员工的行内是否存在两个1存在邻近关系，因为有规则规定不能存在空闲时间段
            long continuousDuration=0;
            //存放时长
            ArrayList<Long> longs = new ArrayList<>();
            for (int i = 0; i <employeeClasses.length; i++) {
                if (employeeClasses[employeeSubscript][i]==1){
                    int count=0;
                    //算时长
                    LocalTime startTime = classesService.getById(classesArray[i]).getStartTime();
                    LocalTime endTime = classesService.getById(classesArray[i]).getEndTime();
                    //判断是否覆盖吃饭时间
                    Long l=timeToMinute(endTime);
                    Long aLong = timeToMinute(lunchEndTime);
                    Long startTimeLong = timeToMinute(startTime);
                    Long Long = timeToMinute(lunchStartTime);
                    Long dinnerStartTimeLong = timeToMinute(dinnerStartTime);
                    Long dinnerEndTimeLong = timeToMinute(dinnerEndTime);

                    if (startTimeLong<=Long&&l>=aLong||startTimeLong<=dinnerStartTimeLong&&l>=dinnerEndTimeLong){  //就是开始时间和结束时间覆盖吃饭时间
                        count++;
                    }

                    continuousDuration=continuousDuration+timeToMinute(endTime)-timeToMinute(startTime)-30*count;
                }else {
                    longs.add(continuousDuration);
                    continuousDuration=0;
                }
            }

            continuousDuration=0;
            for (int i = 0; i < longs.size(); i++) {
                continuousDuration=Math.max(continuousDuration,longs.get(i));
            }
            return continuousDuration;
        }

        //根据员工id获取具体班次集合
        public List<Classes> getClassesByEmployeeId(String employeeId){
            int a=0;//该员工id下标
            for (int i = 0; i < employeeArray.length; i++) {
                if (employeeId.equals(employeeArray[i])){
                    a=i;
                }
            }
            ArrayList<Integer> list = new ArrayList<>();//用来存储班次下标
            for (int i = 0; i < employeeClasses.length; i++) {
                if (employeeClasses[a][i]==1){
                    list.add(i);
                }
            }
            ArrayList<Classes> classesArrayList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                Classes classes = classesService.getById(classesArray[list.get(i)]);
                classesArrayList.add(classes);
            }
            return classesArrayList;
        }


        // 求最大工时数  根据Classes计算每天的最大工时数
        public Long getMaxWorkTime(){
            long max=0;
            for (int i = 0; i < classesArray.length; i++) {
                Classes classesServiceById = classesService.getById(classesArray[i]);
                LocalTime startTime = classesServiceById.getStartTime();
                LocalTime endTime = classesServiceById.getEndTime();
                Integer employeeNumber = classesServiceById.getEmployeeNumber();

                long startMinute = timeToMinute(startTime);
                long endMinute = timeToMinute(endTime);

                max=max+(endMinute-startMinute)*employeeNumber;
            }
            return max;
        }



    }

    //TODO 周和月，定义一个对象用来存储ScheduleByOne，天员工班次，
    @Data
    public  class ScheduleByAll{

        private int[][] mouthEmployeeClasses;   //1,0,表示有员工，0表示没有，N*M，N为员工数，M为班次数
        private String[] employeeArray;    //储存员工id
        private String[] mouthClassesArray;     //存储班次id
//        private LocalDate localDate;       //时间  必须设置





    }


    //    定义一个对象用来存储员工班次
    @Data
    public class Schedule {
        //必须加上startTime字段，因为所有班次都存放在一个表中，因此后面要根据startTime来查询数据 在set数据时根据时间查询
        private List<EmployeeClasses> employeeClassesList; //员工与班次之间是多对多关系，

        // 获取未排满的班次列表
        public List<Classes> getUnfilledClassesList() {
            List<Classes> classesArrayList = new ArrayList<>(); //用来存储未分配班次
            if (employeeClassesList.isEmpty()) {
                //TODO 要增加时间等条件
                classesArrayList = classesService.list(null);
            } else {
                //用map存储班次id和员工人数 TODO 按日 后面要改
                HashMap<String, Integer> classesMap = new HashMap<>();
                List<Classes> classes = classesService.list(null);
                String classesId = null;
                for (int i = 0; i < classes.size(); i++) {
                    classesMap.put(classes.get(i).getId(), 0);
                }
                //根据遍历员工排班列表集合classesList获取id与班次对应的id比较并将未分配的班次存入集合中
                for (int i = 0; i < employeeClassesList.size(); i++) {
                    EmployeeClasses employeeClasses = employeeClassesList.get(i);
                    classesId = employeeClasses.getClassesId();
                    if (classesId != null) {
                        //根据班次id获取数据，根据数据是否为空,不为空则员工数加一
                        if (classesService.getById(classesId) != null) {
                            Integer integer = classesMap.get(classesId);
                            int j = integer + 1;
                            classesMap.replace(classesId, integer, j);
                        }
                    }

                }

                //将classesMap与classes相比较
                for (int i = 0; i < classes.size(); i++) {
                    Classes classes1 = classes.get(i);
                    String id = classes1.getId();
                    Integer employeeNumber = classes1.getEmployeeNumber();
                    LocalTime startTime = classes1.getStartTime();
                    LocalTime endTime = classes1.getEndTime();
                    Classes classes2 = new Classes();

                    int i1 = employeeNumber - classesMap.get(id);
                    if (i1 < 0) {
                        throw new BwtpException();
                    } else if (i1 != 0) {
                        classes2.setId(id);
                        classes2.setStartTime(startTime);
                        classes2.setEndTime(endTime);
                        classes2.setEmployeeNumber(i1);
                        classesArrayList.add(classes2);
                    }
                }
            }
            return classesArrayList;

        }

        // 获取员工日排班总时长
        public Long getDayTotalDuration(String employeeId) {
            long diff = 0; //时长   分钟
            //在员工排班classesList根据员工id获取时长
            for (int i = 0; i < employeeClassesList.size(); i++) {
                EmployeeClasses employeeClasses = employeeClassesList.get(i);
                String employeeid = employeeClasses.getEmployeeId();
                String classesId = employeeClasses.getClassesId();
                if (employeeid.equals(employeeId)) {
                    Classes classes = classesService.getById(classesId);
                    //单位为分钟
                    Duration duration = Duration.between(classes.getEndTime(), classes.getStartTime());
                    diff = diff + Math.abs(duration.toMinutes());
                }
            }

            return diff;
        }

        //         TODO 获取员工日连续工作时长
        public Long getContinuousDuration(String employeeId) {
            //判断是否给了吃饭时间 就是判断班次是否完全覆盖吃饭时间  覆盖直接时长减去半小时
//            int b = isTrueRestDuration(employeeId, employeeClassesList);
            List<LocalTime> list = new ArrayList<>();
            for (int i = 0; i < employeeClassesList.size(); i++) {
                if (employeeClassesList.get(i).getEmployeeId().equals(employeeId)){
                    EmployeeClasses employeeClasses = employeeClassesList.get(i);
                    String classesId = employeeClasses.getClassesId();
                    Classes byId = classesService.getById(classesId);
                    list.add(byId.getStartTime());
                    list.add(byId.getEndTime());
                }
            }
//            System.out.println("list--------"+list);
            //判断是否有两个班次连续，就是看list中是否有两个相同的时间
            //判断list集合中是否包含重复元素，可以借用set集合，因为set集合不允许存在重复元素
            /*
             * 思路：
             * 创建一个HashSet对象用来存储list集合里面的元素
             * 借用set里面的contains()方法，该方法的作用为，判断set集合中是否存在该元素
             *如果不存在则将元素存入set集合中
             * 存在则输出*/
            Set<LocalTime> timeHashSet = new HashSet<>();
            ArrayList<LocalTime> localTimes = new ArrayList<>(); //用来存放重复的时间
            for (LocalTime localTime: list) {
                if (timeHashSet.contains(localTime)) { //存在
                    localTimes.add(localTime);
                }else {   //不存在
                    timeHashSet.add(localTime);
                }
            }
//            System.out.println("timeHashSet--------"+timeHashSet);
            //计算时长
            long max=Integer.MIN_VALUE;
            ArrayList<Integer> integerArrayList = new ArrayList<>(); //存放时长
            for (int i = 0; i < list.size(); i++) {
                integerArrayList.add((list.get(i+1).getHour()*60+list.get(i+1).getMinute())-(list.get(i).getHour()*60+list.get(i).getMinute()));
                i++;
            }
//            System.out.println("integerArrayList--------"+integerArrayList);
            if (localTimes.isEmpty()){
                for (int i = 0; i < integerArrayList.size(); i++) {
                    max=Math.max(max,integerArrayList.get(i));
                }
            }else {
                for (int i = 0; i < localTimes.size(); i++) {
                    ArrayList<Integer> arrayList = new ArrayList<>();//存放相同数据
                    for (int j = 0; j < list.size(); j++) {
                        int a=0;
                        if (localTimes.get(i).equals(list.get(j))){
                            if (j%2==0){ //判断奇偶性
                                a=a+list.get(j+1).getMinute()-list.get(j).getMinute();
                            }else {
                                a=a+list.get(j).getMinute()-list.get(j-1).getMinute();
                            }
                        }else {
                            arrayList.add(list.get(j+1).getMinute()-list.get(j).getMinute());
                        }
                        arrayList.add(a);
                    }
                    for (int j = 0; j < arrayList.size(); j++) {  //求最大值
                        max=Math.max(max,arrayList.get(j));
                    }
                }
            }
            return max;

        }
    }


//    对于固定规则，首先检查该员工是否符合月最多工作时长以及每日最多工作时长的限制。

    /**
     * a. 员工每周最多工作40小时
     * <p>
     * b. 员工每天最多工作８小时
     * <p>
     * c. 单个班次最少2小时，最多4小时。员工可以连续排多个班次  已遵守
     * <p>
     * d. 员工最长连续工作时长：4小时。达到连续工作时长，必须安排休息时间
     * <p>
     * e. 必须给工作时间完全覆盖午餐、晚餐时间的员工，安排午餐或晚餐时间
     */
    //员工每天最多工作８小时
    //员工每周最多工作40小时
    //员工最长连续工作时长：4小时。达到连续工作时长，必须安排休息时间
    public boolean isTrueForWorkTime(String employeeId, Schedule schedule) {

        boolean a;
        boolean b=true;

        //  TODO 根据员工id获取月工作时长
        //  TODO 根据员工id获取周工作时长

        //根据员工id获取日工作时长
        Long totalDuration =schedule.getDayTotalDuration(employeeId);
        if (totalDuration > dayWorkTime) {
            a=true;
        } else {
            a=false;
        }

        //员工最长连续工作时长：4小时。达到连续工作时长，必须安排休息时间
        //连续工作时间超过240分钟则返回false
        if (schedule.getContinuousDuration(employeeId)>240){
            b=false;
        }


        return a&&b;
    }


    //必须给工作时间完全覆盖午餐、晚餐时间的员工，安排午餐或晚餐时间   判断是否给了吃饭时间 就是判断班次是否完全覆盖吃饭时间  覆盖直接时长减去半小时或1小时
    public Integer isTrueRestDuration(String employeeId,ScheduleByOne scheduleByOne) {
        //在employee_classes中根据员工id获取班次id，根据班次id获取时间段
        int count=0;//用来计数，覆盖吃饭时间次数
        List<Classes> classesByEmployeeId = scheduleByOne.getClassesByEmployeeId(employeeId);
        for (Classes classes :classesByEmployeeId) {
            LocalTime startTime = classes.getStartTime();
            LocalTime endTime = classes.getEndTime();
            //将结束时间转为分钟
            Long l=timeToMinute(endTime);
            Long aLong = timeToMinute(lunchEndTime);
            Long startTimeLong = timeToMinute(startTime);
            Long Long = timeToMinute(lunchStartTime);
            Long dinnerStartTimeLong = timeToMinute(dinnerStartTime);
            Long dinnerEndTimeLong = timeToMinute(dinnerEndTime);

            if (startTimeLong<=Long&&l>=aLong||startTimeLong<=dinnerStartTimeLong&&l>=dinnerEndTimeLong){  //就是开始时间和结束时间刚好
                count++;
            }

        }
        return count;
    }


    //    自定义规则
    //关店，开店，客流这三个规则必选
    //在任何时间段内至少有一个员工值班，不能都去休息，午餐和晚餐在不是满4休息的规则内轮流吃饭，先后顺序不定。
    //传入员工班次表，判断在工作时间内是否有时间段没有员工
    public boolean isTrueEmployees(Schedule schedule){
        List<EmployeeClasses> employeeClassesList = schedule.getEmployeeClassesList();
        //先判断是否是周末
        DayOfWeek week = employeeClassesList.get(0).getCreateTime().getDayOfWeek();
        int index = week.getValue();
        System.out.println(index);

        if (index != 6 && index != 7) {
            //按照开店和关店规则
            setStartWorkTime(LocalTime.of(8, 0));
            setEndWorkTime(LocalTime.of(23, 0));
        } else {
            setStartWorkTime(LocalTime.of(9, 0));
            setEndWorkTime(LocalTime.of(24, 0));
        }

        int startWorkTimeM=startWorkTime.getHour()*60+startWorkTime.getMinute();
        int endWorkTimeM=0;
        //要转为分钟来比较，小时24点为0
        if(endWorkTime.getHour()==0){
            endWorkTimeM=24*60;
        }else {
            endWorkTimeM=endWorkTime.getHour()*60+endWorkTime.getMinute();
        }

        int count=0;
        while (startWorkTimeM<=endWorkTimeM){
            for (EmployeeClasses employeeClasses:employeeClassesList) {
                Classes classesServiceById = classesService.getById(employeeClasses.getClassesId());
                LocalTime startTime = classesServiceById.getStartTime();
                LocalTime endTime = classesServiceById.getEndTime();
                setStartWorkTime(startWorkTime.plusHours(1));
                int endTimeM=0;
                if (endTime.getHour()==0){
                    endTimeM=24*60;
                }else {
                    endTimeM = endTime.getHour() * 60 + endTime.getMinute();
                }

                if(startWorkTime.getHour()==0){
                    startWorkTimeM=24*60;
                }else {
                    startWorkTimeM=endWorkTime.getHour()*60+endWorkTime.getMinute();
                }

                if ((startTime.getHour()*60+startWorkTime.getMinute())<=startWorkTimeM&&startWorkTimeM<=endTimeM){
                    count++;
                    break;
                }
            }
        }

        //重置时间
        if (index != 6 && index != 7) {
            setStartWorkTime(LocalTime.of(9, 0));
            setEndWorkTime(LocalTime.of(21, 0));
        } else {
            setStartWorkTime(LocalTime.of(10, 0));
            setEndWorkTime(LocalTime.of(22, 0));
        }

        if (count==(endWorkTime.getHour()-startWorkTime.getHour()-1)){
            return true;
        }else {
            return false;
        }

    }

    //TODO 一周内员工无班次天数不能超过两天。
//    public boolean restExceedTwoDays(){
//
//
//    }

//    TODO 前一天21点到23点班次的员工里必须有一位员工在第二天8点到12点内值班。


    //    将符合条件的班次按照员工偏好进行筛选，得到可用的班次列表。
    //偏好规则  如果有多个员工根据员工等级1到4(暂时不用)，根据员工id，查询偏好，并与班次相比较，符合为true
    //TODO Schedule这个后面应为ScheduleByAll 这里后期应根据日期
    public boolean doesMeetPreferences(String employeeId,ScheduleByOne scheduleByOne){
        //在EmployeePreference表中根据id获取员工偏好及其偏好值
        QueryWrapper<EmployeePreference> employeePreferenceQueryWrapper = new QueryWrapper<>();
        employeePreferenceQueryWrapper.eq("EmployeeId",employeeId);
        List<EmployeePreference> list = employeePreferenceService.list(employeePreferenceQueryWrapper);
        //根据id获取员工班次
        List<Classes> classesByEmployeeId = scheduleByOne.getClassesByEmployeeId(employeeId);
        JSONObject jsonObject = null;
        boolean a=true;
        boolean b=true;
        boolean c=true;
        for (EmployeePreference employeePreference:list) {
            String preferenceTypeId = employeePreference.getPreferenceTypeId();
            String preferenceValue = employeePreference.getPreferenceValue();  //偏好值，json
            jsonObject=JSON.parseObject(preferenceValue);//转为json字符串
            //  TODO 偏好一 121
            if (preferenceTypeId.equals("121")){

            }

            //偏好二 122
            if (preferenceTypeId.equals("122")){
                for (Classes classes:classesByEmployeeId) {
                    LocalTime startTime = classes.getStartTime();
                    LocalTime endTime = classes.getEndTime();
                    Long endMinute = timeToMinute(endTime);
                    Long startMinute = timeToMinute(startTime);
                    int start=jsonObject.getInteger("start")*60;
                    int end=jsonObject.getInteger("end")*60;

                    if (!(startMinute>=start&&endMinute<=end)){ //即工作时间都在在偏好以内
                        b=false;
                        break;
                    }
                }
            }

            //偏好三 123
            if (preferenceTypeId.equals("123")){
                //每天不超过时长
                //获取每天的总时长
                Long dayTotalDuration = scheduleByOne.getDayTotalDuration(employeeId, scheduleByOne);
                int dayLength =  jsonObject.getInteger("dayLength")*60;
                //TODO 周
                int weekLength =  jsonObject.getInteger("weekLength");
                if (dayLength<dayTotalDuration){
                    c=false;
                }
            }
        }

        return a&&b&&c;
    }



//    计算适应度  空闲班次数   工时数  先求一天的
    public double[] evaluate(ScheduleByOne scheduleByOne){

        double[] fitness=new double[2];
        //总共开放班次数
        double count=0;

        //获取空闲班次
        List<Classes> classesList = scheduleByOne.getUnfilledClassesList();
        for (int i = 0; i < classesList.size(); i++) {
            count=count+classesList.get(i).getEmployeeNumber();
        }

        fitness[0]=count;

        //总共工时数     最大工时数
        String[] employeeArray = scheduleByOne.getEmployeeArray();
        long totalWorkTime=0;
        for (int i = 0; i < employeeArray.length; i++) {
            totalWorkTime=scheduleByOne.getDayTotalDuration(employeeArray[i],scheduleByOne)+totalWorkTime;
        }

        double totalWorkTimePercentage=(double) totalWorkTime/scheduleByOne.getMaxWorkTime();


        DecimalFormat format = new DecimalFormat("#.000");  //控制精度
        String s = format.format(totalWorkTimePercentage);
        double v = Double.parseDouble(s);

        fitness[1]=v;

        return fitness;

    }




    /**这个废了*/
    public void test() {


        //月排班
        ScheduleByAll scheduleByAll = new ScheduleByAll();
        LocalDate date = LocalDate.of(2023, 2,1 );

        int a=0,b=0,c=0;
        List<Classes> list = classesService.list(null);
        String[] mouthClasses=new String[list.size()];
        for (int i = 0; i < 28; i++) {


            LocalDate dateN= date.plusDays(i);
//            System.out.println(dateN);
            ScheduleByOne schedule = new ScheduleByOne();


            schedule.setLocalDate(dateN);
            //        private String[] employeeArray;    //储存员工id 固定的 根据门店id
            QueryWrapper<ServiceEmployee> wrapper = new QueryWrapper<>();
            wrapper.eq("shop_id", "1");
            //1,2,3,4顺序存入
            wrapper.orderByAsc("position");
            List<ServiceEmployee> employeeList = employeeService.list(wrapper);

            String[] employeeArray = new String[employeeList.size()];
            for (int j = 0; j < employeeList.size(); j++) {
                employeeArray[j] = employeeList.get(j).getId();
            }

//        private String[] classesArray;     //存储班次id
            QueryWrapper<Classes> queryWrapper = new QueryWrapper<>();
            //根据日期
            //获取28号班次
            queryWrapper.eq("create_data", schedule.getLocalDate());
            //时间从小到大顺序存入
            queryWrapper.orderByAsc("start_time");

            List<Classes> classesList = classesService.list(queryWrapper);
            String[] classesArray = new String[classesList.size()];

            for (int j = 0; j < classesList.size(); j++) {
                classesArray[j] = classesList.get(j).getId();
            }

            /**classesArray数组获取没问题*/

//           直接存入月班次表中




//        private int[][] employeeClasses;   //1,0,表示有员工，0表示没有，N*M，N为员工数，M为班次数
            QueryWrapper<EmployeeClasses> employeeClassesQueryWrapper = new QueryWrapper<>();
            employeeClassesQueryWrapper.eq("create_time", schedule.getLocalDate());


            /**employeeClassesService*/
            List<EmployeeClasses> employeeClassesList = employeeClassesService.list(employeeClassesQueryWrapper);



            //按班次要求随机生成
            int[][] employeeClasses = new int[employeeList.size()][classesList.size()]; //行列
            for (int j = 0; j < classesArray.length; j++) {
                for (int k = 0; k < employeeArray.length; k++) {
                    for (int n = 0; n < employeeClassesList.size(); n++) {
                        if (employeeClassesList.get(n).getEmployeeId().equals(employeeArray[k])) {
                            if (classesArray[j].equals(employeeClassesList.get(n).getClassesId())) {
                                employeeClasses[k][j] = 1;
                            }
                        }
                    }

                }
            }

            schedule.setEmployeeClasses(employeeClasses);
            schedule.setEmployeeArray(employeeArray);
            schedule.setClassesArray(classesArray);

//            for (int q = 0; q < employeeArray.length; q++) {
//                System.out.println("");
//                for (int j = 0; j < classesArray.length; j++) {
//                    System.out.print(employeeClasses[q][j]);
//                }
//            }


            //给月排班设置值
            //员工
            scheduleByAll.setEmployeeArray(schedule.getEmployeeArray());

            //班次数组  复制  Arrays.copyOf  System.arraycopy
            //班次数组和排班数组按日期，如果为1号直接set否则复制
            if (date.getDayOfMonth()==1){
                scheduleByAll.setMouthClassesArray(classesArray);
                scheduleByAll.setMouthEmployeeClasses(employeeClasses);
            }else {
                String[] array = scheduleByAll.getMouthClassesArray();

                String[] mouthClassesArray = Arrays.copyOf(array, array.length + classesArray.length);
                System.arraycopy(classesArray, 0, mouthClassesArray, array.length, classesArray.length);
                scheduleByAll.setMouthClassesArray(mouthClassesArray);

                //排班数组  复制  Arrays.copyOf  System.arraycopy
                int[][] mouthEmployeeClasses = scheduleByAll.getMouthEmployeeClasses();
                int[][] mouthEmployeeClassesW = Arrays.copyOf(mouthEmployeeClasses, mouthEmployeeClasses.length + employeeClasses.length);
                System.arraycopy(employeeClasses, 0, mouthEmployeeClassesW, mouthEmployeeClasses.length, employeeClasses.length);
                scheduleByAll.setMouthEmployeeClasses(mouthEmployeeClassesW);

            }


        }



        int[][] mouthEmployeeClasses = scheduleByAll.getMouthEmployeeClasses();
        String[] employeeArray = scheduleByAll.getEmployeeArray();
        String[] mouthClassesArray = scheduleByAll.getMouthClassesArray();

//        for (int i = 0; i < employeeArray.length; i++) {
//            System.out.println("");
//            for (int j = 0; j < mouthClassesArray.length; j++) {
//                System.out.print(mouthEmployeeClasses[i][j]);
//            }
//        }






    }



}


