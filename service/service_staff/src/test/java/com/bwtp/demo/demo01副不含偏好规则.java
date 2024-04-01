package com.bwtp.demo;

import java.text.DecimalFormat;
import java.time.*;
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
public class demo01副不含偏好规则 {

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
    private LocalTime lunchStartTime = LocalTime.of(11, 00);
    private LocalTime lunchEndTime = LocalTime.of(14, 00);

    //晚餐时间
    private LocalTime dinnerStartTime = LocalTime.of(17, 00);
    private LocalTime dinnerEndTime = LocalTime.of(20, 00);

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
    private Integer restDuration = 30;

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
     * 0 1 2 3 4 5 6 7 8 9
     * 1 1 0 1 0 0 0 0 0 0 0
     * 2 0 0 1 0 0 0 0 0 0 0
     * 3 1 0 0 0 0 0 1 0 0 0
     * 4 0 0 1 0 0 0 1 0 0 0
     */


    //不，这里就是按天
    @Data
    public class ScheduleByOne {
        private int[][] employeeClasses;   //1,0,表示有员工，0表示没有，N*M，N为员工数，M为班次数
        private String[] employeeArray;    //储存员工id
        private String[] classesArray;     //存储班次id
        private LocalDate localDate;       //时间  必须设置


        //工具方法，根据员工id获取员工数组里的数组下标
        public Integer getSubscript(String employeeId) {
            int employeeSubscript = 0;
            for (int i = 0; i < employeeArray.length; i++) {
                if (employeeArray[i].equals(employeeId)) {
                    employeeSubscript = i;
                    break;
                }
            }
            return employeeSubscript;
        }


        // 获取未排满的班次列表，纵轴1的数是否为该班次所需数
        public List<Classes> getUnfilledClassesList() {

            ArrayList<Classes> arrayList = new ArrayList<>();
            for (int i = 0; i < classesArray.length; i++) {
                Classes classes = new Classes();
                int count = 0;
                for (int j = 0; j < employeeArray.length; j++) {
                    if (employeeClasses[j][i] == 1) {
                        count++;
                    }
                }
                Classes byId = classesService.getById(classesArray[i]);
                int b = byId.getEmployeeNumber() - count;
                if (b > 0) {
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
        public Long getDayTotalDuration(String employeeId, ScheduleByOne scheduleByOne) {
            int b = isTrueRestDuration(employeeId, scheduleByOne);
            //根据员工id获取员工数组里的数组下标
            int employeeSubscript = getSubscript(employeeId);
            //根据下标在employeeClasses中获取该员工的工作时长
            long dayWorkTimeLength = 0;
            for (int i = 0; i < employeeClasses.length; i++) {
                if (employeeClasses[employeeSubscript][i] == 1) {
                    LocalTime startTime = classesService.getById(classesArray[i]).getStartTime();
                    LocalTime endTime = classesService.getById(classesArray[i]).getEndTime();
                    dayWorkTimeLength = timeToMinute(endTime) - timeToMinute(startTime) + dayWorkTimeLength;
                }
            }
            return dayWorkTimeLength - (long) (30 * b);
        }

        //获取员工日连续工作时长
        public Long getContinuousDuration(String employeeId) {
            //根据员工id获取员工数组里的数组下标
            int employeeSubscript = getSubscript(employeeId);
            //直接看该员工的行内是否存在两个1存在邻近关系，因为有规则规定不能存在空闲时间段
            long continuousDuration = 0;
            //存放时长
            ArrayList<Long> longs = new ArrayList<>();
            for (int i = 0; i < employeeClasses.length; i++) {
                if (employeeClasses[employeeSubscript][i] == 1) {
                    int count = 0;
                    //算时长
                    LocalTime startTime = classesService.getById(classesArray[i]).getStartTime();
                    LocalTime endTime = classesService.getById(classesArray[i]).getEndTime();
                    //判断是否覆盖吃饭时间
                    Long l = timeToMinute(endTime);
                    Long aLong = timeToMinute(lunchEndTime);
                    Long startTimeLong = timeToMinute(startTime);
                    Long Long = timeToMinute(lunchStartTime);
                    Long dinnerStartTimeLong = timeToMinute(dinnerStartTime);
                    Long dinnerEndTimeLong = timeToMinute(dinnerEndTime);

                    if (startTimeLong <= Long && l >= aLong || startTimeLong <= dinnerStartTimeLong && l >= dinnerEndTimeLong) {  //就是开始时间和结束时间覆盖吃饭时间
                        count++;
                    }

                    continuousDuration = continuousDuration + timeToMinute(endTime) - timeToMinute(startTime) - 30 * count;
                } else {
                    longs.add(continuousDuration);
                    continuousDuration = 0;
                }
            }

            continuousDuration = 0;
            for (int i = 0; i < longs.size(); i++) {
                continuousDuration = Math.max(continuousDuration, longs.get(i));
            }
            return continuousDuration;
        }

        //根据员工id获取具体班次集合
        public List<Classes> getClassesByEmployeeId(String employeeId) {
            int a = 0;//该员工id下标
            for (int i = 0; i < employeeArray.length; i++) {
                if (employeeId.equals(employeeArray[i])) {
                    a = i;
                }
            }
            ArrayList<Integer> list = new ArrayList<>();//用来存储班次下标
            for (int i = 0; i < employeeClasses.length; i++) {
                if (employeeClasses[a][i] == 1) {
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
        public Long getMaxWorkTime() {
            long max = 0;
            for (int i = 0; i < classesArray.length; i++) {
                Classes classesServiceById = classesService.getById(classesArray[i]);
                LocalTime startTime = classesServiceById.getStartTime();
                LocalTime endTime = classesServiceById.getEndTime();
                Integer employeeNumber = classesServiceById.getEmployeeNumber();

                long startMinute = timeToMinute(startTime);
                long endMinute = timeToMinute(endTime);

                max = max + (endMinute - startMinute) * employeeNumber;
            }
            return max;
        }


    }

    //TODO 周和月，定义一个对象用来存储ScheduleByOne，天员工班次，
    @Data
    public class ScheduleByAll {

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
                if (employeeClassesList.get(i).getEmployeeId().equals(employeeId)) {
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
            for (LocalTime localTime : list) {
                if (timeHashSet.contains(localTime)) { //存在
                    localTimes.add(localTime);
                } else {   //不存在
                    timeHashSet.add(localTime);
                }
            }
//            System.out.println("timeHashSet--------"+timeHashSet);
            //计算时长
            long max = Integer.MIN_VALUE;
            ArrayList<Integer> integerArrayList = new ArrayList<>(); //存放时长
            for (int i = 0; i < list.size(); i++) {
                integerArrayList.add((list.get(i + 1).getHour() * 60 + list.get(i + 1).getMinute()) - (list.get(i).getHour() * 60 + list.get(i).getMinute()));
                i++;
            }
//            System.out.println("integerArrayList--------"+integerArrayList);
            if (localTimes.isEmpty()) {
                for (int i = 0; i < integerArrayList.size(); i++) {
                    max = Math.max(max, integerArrayList.get(i));
                }
            } else {
                for (int i = 0; i < localTimes.size(); i++) {
                    ArrayList<Integer> arrayList = new ArrayList<>();//存放相同数据
                    for (int j = 0; j < list.size(); j++) {
                        int a = 0;
                        if (localTimes.get(i).equals(list.get(j))) {
                            if (j % 2 == 0) { //判断奇偶性
                                a = a + list.get(j + 1).getMinute() - list.get(j).getMinute();
                            } else {
                                a = a + list.get(j).getMinute() - list.get(j - 1).getMinute();
                            }
                        } else {
                            arrayList.add(list.get(j + 1).getMinute() - list.get(j).getMinute());
                        }
                        arrayList.add(a);
                    }
                    for (int j = 0; j < arrayList.size(); j++) {  //求最大值
                        max = Math.max(max, arrayList.get(j));
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
    public boolean isTrueForWorkTime12(String employeeId, Schedule schedule) {

        boolean a;
        boolean b = true;

        //  TODO 根据员工id获取月工作时长
        //  TODO 根据员工id获取周工作时长

        //根据员工id获取日工作时长
        Long totalDuration = schedule.getDayTotalDuration(employeeId);
        if (totalDuration > dayWorkTime) {
            a = true;
        } else {
            a = false;
        }

        //员工最长连续工作时长：4小时。达到连续工作时长，必须安排休息时间
        //连续工作时间超过240分钟则返回false
        if (schedule.getContinuousDuration(employeeId) > 240) {
            b = false;
        }


        return a && b;
    }


    //必须给工作时间完全覆盖午餐、晚餐时间的员工，安排午餐或晚餐时间   判断是否给了吃饭时间 就是判断班次是否完全覆盖吃饭时间  覆盖直接时长减去半小时或1小时
    public Integer isTrueRestDuration(String employeeId, ScheduleByOne scheduleByOne) {
        //在employee_classes中根据员工id获取班次id，根据班次id获取时间段
        int count = 0;//用来计数，覆盖吃饭时间次数
        List<Classes> classesByEmployeeId = scheduleByOne.getClassesByEmployeeId(employeeId);
        for (Classes classes : classesByEmployeeId) {
            LocalTime startTime = classes.getStartTime();
            LocalTime endTime = classes.getEndTime();
            //将结束时间转为分钟
            Long l = timeToMinute(endTime);
            Long aLong = timeToMinute(lunchEndTime);
            Long startTimeLong = timeToMinute(startTime);
            Long Long = timeToMinute(lunchStartTime);
            Long dinnerStartTimeLong = timeToMinute(dinnerStartTime);
            Long dinnerEndTimeLong = timeToMinute(dinnerEndTime);

            if (startTimeLong <= Long && l >= aLong || startTimeLong <= dinnerStartTimeLong && l >= dinnerEndTimeLong) {  //就是开始时间和结束时间刚好
                count++;
            }

        }
        return count;
    }

    /**
     * 这个废了
     */
    public void test() {


        //月排班
        ScheduleByAll scheduleByAll = new ScheduleByAll();
        LocalDate date = LocalDate.of(2023, 2, 1);

        int a = 0, b = 0, c = 0;
        List<Classes> list = classesService.list(null);
        String[] mouthClasses = new String[list.size()];
        for (int i = 0; i < 28; i++) {


            LocalDate dateN = date.plusDays(i);
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
            if (date.getDayOfMonth() == 1) {
                scheduleByAll.setMouthClassesArray(classesArray);
                scheduleByAll.setMouthEmployeeClasses(employeeClasses);
            } else {
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

    //获取空闲班次
    public List<Classes> getUnfilledClassesList(int[][] employeeClasses, String[] mouthClassesArray, String[] mouthEmployeeArray) {
        ArrayList<Classes> arrayList = new ArrayList<>();
        for (int i = 0; i < mouthClassesArray.length; i++) {
            Classes classes = new Classes();
            int count = 0;
            for (int j = 0; j < mouthEmployeeArray.length; j++) {
                if (employeeClasses[j][i] == 1) {
                    count++;
                }
            }
            Classes byId = classesService.getById(mouthClassesArray[i]);
            int b = byId.getEmployeeNumber() - count;
            if (b > 0) {
                classes.setId(mouthClassesArray[i]);
                classes.setStartTime(byId.getStartTime());
                classes.setEndTime(byId.getEndTime());
                classes.setEmployeeNumber(b);
                arrayList.add(classes);
            }
        }
        for (int i = 0; i < arrayList.size(); i++) {

            if (arrayList.get(i).getId().equals("1639176896068988930")) {
                System.out.println(arrayList.get(i).getEmployeeNumber());
            }
        }


        return arrayList;
    }

    /****************************上面的没用***************************分界线**********************************下面为正式遗传算法*******************************************************************/


    /**
     * 注意该排班算法中日期固定，即2023-02，只求了二月份排班，方便排班。如果要通用，还应增加假期，将月份作为变量。
     * */

    /**
     * 工具方法
     * 将时间转为分钟，遇到0点将其转为24*60
     */
    public Long timeToMinute(LocalTime localTime) {
        long length = localTime.getHour() * 60 + localTime.getMinute();
        if (localTime.getHour() == 0) {
            length = 24 * 60;
        }
        return length;
    }

    /**
     * 工具方法
     * 随机选择n个员工   完成初步测试
     */
    public int[] random(int numberLength, String[] employeeArray,int[] selectedNumsB,boolean b) {  //selectedNumsB 上一个被选中的数  b是否可以和上一个数重复
        int[] nums = new int[employeeArray.length];
        for (int i = 0; i < employeeArray.length; i++) {
            nums[i] = i;
        }



        int n = numberLength; // 需要随机选取的数的个数
        int[] selectedNums = new int[n]; // 用于存储选中的数
        Random random = new Random();
        for (int i = 0; i < n; i++) {

            int index;// 随机生成一个索引
            //判断是否在上一个选中的数中存在 存在则重新选取，隔天不算
            if (b||selectedNumsB==null){ //可以重复，且至少有一个为重复被选，思路：第一个班次在上一个班次已选员工中选择员工，然后其他随机
                if (i==0&&selectedNumsB!=null) {
                    while (true){
                        int count=0; //用于判断是否跳出while循环
                        index= random.nextInt(nums.length);
                        for (int j = 0; j <selectedNumsB.length; j++) {
                            if (nums[index]==selectedNumsB[j]){
                                count++;
                                break;
                            }
                        }
                        if (count==1){
                            break;
                        }
                    }
                }else {
                    index= random.nextInt(nums.length);
                }

            }else {
                while (true){
                    index= random.nextInt(nums.length);
                    int count=0; //用于判断是否跳出while循环
                    for (int j = 0; j < selectedNumsB.length; j++) {
                        if (nums[index]==selectedNumsB[j]){
                            count++;
                            break;//重复跳出for循环重新赋值
                        }
                    }
                    if (count==0){ //没有经过break
                        break;
                    }
                }

            }

            selectedNums[i] = nums[index]; // 将选中的数存储到数组中
            nums[index] = nums[nums.length - 1]; // 将选中的数与最后一个数交换
            nums = Arrays.copyOf(nums, nums.length - 1); // 删除最后一个数 ,以避免重复选取。
        }

        if (selectedNumsB!=null){
            System.out.println("");
            for (int j = 0; j < selectedNumsB.length; j++) {
                System.out.print(selectedNumsB[j]+",");
            }
        }
        System.out.print(b);
        if (selectedNumsB!=null){
            System.out.print("-------");
            for (int j = 0; j < selectedNums.length; j++) {
                System.out.print(selectedNums[j]+",");
            }
        }
        return selectedNums;
    }


    /**
     * 工具方法
     * 已完成初步测试
     * 获取单个员工月排班工作时长
     */
    public Long getDayTotalDuration(String employeeId, int[][] employeeClasses, String[] mouthClassesArray, String[] mouthEmployeeArray) {
        int b = isTrueRestDuration(employeeId, employeeClasses, mouthClassesArray, mouthEmployeeArray);
        //根据员工id获取员工数组里的数组下标
        int employeeSubscript = 0;
        for (int i = 0; i < mouthEmployeeArray.length; i++) {
            if (employeeId.equals(mouthEmployeeArray[i])) {
                employeeSubscript = i;
            }
        }
        //根据下标在employeeClasses中获取该员工的工作时长
        long dayWorkTimeLength = 0;
        for (int i = 0; i < mouthClassesArray.length; i++) {
            if (employeeClasses[employeeSubscript][i] == 1) {
                LocalTime startTime = classesService.getById(mouthClassesArray[i]).getStartTime();
                LocalTime endTime = classesService.getById(mouthClassesArray[i]).getEndTime();
                dayWorkTimeLength = timeToMinute(endTime) - timeToMinute(startTime) + dayWorkTimeLength;
            }
        }

        return dayWorkTimeLength - (long) (30 * b);
    }

    /**
     * 工具方法
     * 获取每个员工日连续工作时长  分钟   已完成初步测试
     */
    public List<Long> getContinuousDuration(String[] employeeArray, String[] mouthClassesArray, int[][] employeeClasses, LocalDate localDate) {
        //判断是否给了吃饭时间 就是判断班次是否完全覆盖吃饭时间  覆盖直接时长减去半小时
//            int b = isTrueRestDuration(employeeId, employeeClassesList);
        ArrayList<Long> longArrayList = new ArrayList<>();

        //根据日期获取j的值和长度
        QueryWrapper<Classes> wrapper = new QueryWrapper<>();
        wrapper.eq("create_data", localDate);
        wrapper.orderByAsc("start_time");
        List<Classes> list = classesService.list(wrapper);
        int length = list.size();
        int j_subscript = 0;
        for (int i = 0; i < mouthClassesArray.length; i++) {
            if (mouthClassesArray[i].equals(list.get(0).getId())) {
                j_subscript = i;
            }
        }

        for (int i = 0; i < employeeArray.length; i++) {
            List<Long> workTime = new ArrayList<>();
            long workLong = 0;
            long max = Integer.MIN_VALUE;
            for (int j = j_subscript; j - j_subscript < length; j++) {
                if (employeeClasses[i][j] == 1) {

                    Date data = classesService.getById(mouthClassesArray[j]).getCreateData();
                    LocalDate localDate01 = Instant.ofEpochMilli(data.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                    if (localDate.isEqual(localDate01)) { //相等

                        LocalTime startTime = classesService.getById(mouthClassesArray[j]).getStartTime();
                        LocalTime endTime = classesService.getById(mouthClassesArray[j]).getEndTime();
                        Long startLong = timeToMinute(startTime);
                        Long endLong = timeToMinute(endTime);
                        if (j != j_subscript && employeeClasses[i][j - 1] == 1) {
                            LocalTime endTime01 = classesService.getById(mouthClassesArray[j - 1]).getEndTime();
                            Long endLong01 = timeToMinute(endTime01);
                            if (endLong01 >= startLong) {
                                workLong = workLong + endLong - endLong01;
                            } else {
                                workLong = endLong - startLong;
                            }
                        } else {
                            workLong = endLong - startLong;
                        }
                        workTime.add(workLong);
                    } else {
                        break;
                    }
                }
            }

            for (int j = 0; j < workTime.size(); j++) {
                max = Math.max(max, workTime.get(j));
            }
            longArrayList.add(max);
        }
        return longArrayList;
    }

    /**
     * 工具方法
     * 已完成初步测试
     * 必须给工作时间完全覆盖午餐、晚餐时间的员工，安排午餐或晚餐时间   判断是否给了吃饭时间 就是判断班次是否完全覆盖吃饭时间  覆盖直接时长减去半小时或1小时
     */
    public Integer isTrueRestDuration(String employeeId, int[][] employeeClasses, String[] classesArray, String[] employeeArray) {
        //不考虑假期
        //在employeeClasses根据员工id获取员工一个月的班次中完全覆盖吃饭时间的次数
        int count = 0;//用来计数，覆盖吃饭时间次数

        //根据员工id获取下标
        int a = 0;
        for (int i = 0; i < employeeArray.length; i++) {
            if (employeeId.equals(employeeArray[i])) {
                a = i;
            }
        }

        for (int i = 0; i < classesArray.length; i++) {
            if (employeeClasses[a][i] == 1) {
                //根据下标获取班次
                Classes classes = classesService.getById(classesArray[i]);
                LocalTime startTime = classes.getStartTime();
                LocalTime endTime = classes.getEndTime();
                //将结束时间转为分钟
                Long l = timeToMinute(endTime);
                Long aLong = timeToMinute(lunchEndTime);
                Long startTimeLong = timeToMinute(startTime);
                Long Long = timeToMinute(lunchStartTime);
                Long dinnerStartTimeLong = timeToMinute(dinnerStartTime);
                Long dinnerEndTimeLong = timeToMinute(dinnerEndTime);

                if (startTimeLong <= Long && l >= aLong || startTimeLong <= dinnerStartTimeLong && l >= dinnerEndTimeLong) {  //就是开始时间和结束时间刚好
                    count++;
                }

            }
        }
//        System.out.println(count);
        return count;
    }

    /**
     * 工具方法
     * 判断单个员工日工作时长是否超过8小时
     */
    public boolean getDayWorkTime(String employeeId, int[][] employeeClasses, String[] mouthClassesArray, String[] employeeArray) {

        Integer integer = isTrueRestDuration(employeeId, employeeClasses, mouthClassesArray, employeeArray);//获取休息时间段数

        int employeeSubscript = 0;
        for (int i = 0; i < employeeArray.length; i++) {
            if (employeeId.equals(employeeArray[i])) {
                employeeSubscript = i;
            }
        }

        LocalDate localDate=LocalDate.of(2023,2,1);//固定
        int classesSubscript = 0;// 每次开始遍历的下标
        for (int j = 0; j < 28; j++) {
            long dayWorkTime = 0; //每天工作时长
            LocalDate c=localDate.plusDays(j);

            if (j!=0){
                QueryWrapper<Classes> wrapper = new QueryWrapper<>();
                wrapper.eq("create_data", c.minusDays(1)); //前一天
                wrapper.orderByAsc("start_time"); //升序
                List<Classes> list = classesService.list(wrapper); //获取每天的班次
                int classesLength = list.size();  //每天班次长度
                classesSubscript=classesSubscript+classesLength;
            }

            QueryWrapper<Classes> wrapper = new QueryWrapper<>();
            wrapper.eq("create_data", c);
            wrapper.orderByAsc("start_time"); //升序
            List<Classes> list = classesService.list(wrapper); //获取每天的班次
            int classesLength = list.size();  //每天班次长度

            for (int i = classesSubscript; i-classesSubscript< classesLength; i++) {
                if (employeeClasses[employeeSubscript][i] == 1) {
                    Long startTime = timeToMinute(classesService.getById(mouthClassesArray[i]).getStartTime());
                    Long endTime = timeToMinute(classesService.getById(mouthClassesArray[i]).getEndTime());
                    dayWorkTime = dayWorkTime + endTime-startTime;
                }
            }


            if (dayWorkTime-integer*30>480){//判断是否超过8小时 减去休息时间段

                return false;
            }
        }
        return true; //都通过则为true
    }

    /**
     * 固定规则，首先检查该员工是否符合月最多工作时长以及每日最多工作时长的限制。
     */


    /**
     * 规则
     * a. 员工每周最多工作40小时
     * b. 员工每天最多工作８小时  480
     * c. 员工最长连续工作时长：4小时。达到连续工作时长，必须安排休息时间  未测
     */
    public boolean isTrueForWorkTime(String[] employeeArray, String[] mouthClassesArray, int[][] employeeClasses) {

        //规则：员工最长连续工作时长：4小时。达到连续工作时长，必须安排休息时间
        LocalDate a = LocalDate.of(2023, 2, 1);
        for (int i = 0; i < 28; i++) {
            LocalDate c = a.plusDays(i);
            List<Long> longList = getContinuousDuration(employeeArray, mouthClassesArray, employeeClasses, c);//获取每个员工日连续工作时长
            for (int j = 0; j < longList.size(); j++) {
                if (longList.get(j) > 240) {
                    System.out.println(longList.get(j));
                    return false;
                }
            }

        }

        //规则：员工每天最多工作８小时
        boolean b =true;
        for (int i = 0; i <employeeArray.length; i++) {
            b=b&&getDayWorkTime(employeeArray[i], employeeClasses, mouthClassesArray, employeeArray);
            if (!b){  //不符合规则

                return false;
            }
        }

        //规则：员工每周最多工作40小时

        return true; //都通过返回true

    }


    /**
     * 自定义规则
     * 关店，开店，客流这三个规则必选
     * 在任何时间段内至少有一个员工值班，不能都去休息，午餐和晚餐在不是满4休息的规则内轮流吃饭，先后顺序不定。
     * 传入员工班次表，判断在工作时间内是否有时间段没有员工，也就是至少
     */
    //TODO 还不确定  因为它允许有空闲班次  未测
    public boolean isTrueEmployees(String[] employeeArray, String[] mouthClassesArray, int[][] employeeClasses) {
        //遍历employeeClasses，获取1的个数与总排班员工数相比等于为true
        int count = 0;
        for (int i = 0; i < employeeArray.length; i++) {
            for (int j = 0; j < mouthClassesArray.length; j++) {
                if (employeeClasses[i][j] == 1) {
                    count++;
                }
            }
        }

        int totalNumber = 0;
        for (int i = 0; i < mouthClassesArray.length; i++) {
            totalNumber = totalNumber + classesService.getById(mouthClassesArray[i]).getEmployeeNumber();
        }


        if (count == totalNumber) {
            return true;
        } else {
            return false;
        }


    }

    /**
     * 规则
     * 一周内员工无班次天数不能超过两天。
     * 这个只适用于28天的月份      初步测试完成
     */
    public boolean restExceedTwoDays(String[] employeeArray, String[] mouthClassesArray, int[][] employeeClasses) {
        //算出每个员工一周以内无班次数
        LocalDate a = LocalDate.of(2023, 2, 1);

        ArrayList<List<Integer>> lists = new ArrayList<>();  //用来储存每周的班次数组下标  没问题
        for (int i = 1; i < 5; i++) {
            ArrayList<Integer> list = new ArrayList<>();
            for (int j = 0; j < mouthClassesArray.length; j++) {
                //将data转为localdata
                LocalDate localDate = Instant.ofEpochMilli(classesService.getById(mouthClassesArray[j]).getCreateData().getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                if (localDate.getDayOfMonth() <= i * 7
                        &&localDate.getDayOfMonth()>= ((i - 1) * 7 + 1)) {
                    list.add(j);
                }
            }
            lists.add(list);

        }


        for (int j = 0; j < employeeArray.length; j++) {

            for (int k = 0; k < 4; k++) {

                //用于判断一周内无班次天数
                ArrayList<Integer> list = new ArrayList<>();  //1-7 没问题
                for (int n = 1; n < 8; n++) {
                    list.add(n);
                }
                for (int l = 0; l < lists.get(k).size(); l++) {
                    if (employeeClasses[j][lists.get(k).get(l)] == 1) {
                        LocalDate localDate = Instant.ofEpochMilli(classesService.getById(mouthClassesArray[lists.get(k).get(l)]).getCreateData().getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                        list.set(localDate.getDayOfMonth()% 7, 0);  //当天有班次就把list对应下标值设置为0
                    }
                }

                int count=0;//用于统计不为0个数，即当天为班次个数
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) != 0) {
                        count++;
                    }
                }

                if (count>=2){ //超过两天
                    return false;
                }
            }
        }
        return true;
    }

    /**
     前一天21点到23点班次的员工里必须有一位员工在第二天8点到12点内值班。  初步完成测试
     */
    public boolean ruleThree(String[] employeeArray, int[][] employeeClasses){
        LocalDate localDate=LocalDate.of(2023,2,1);
        int classesSubscript=0;
        ArrayList<Integer> arrayList = new ArrayList<>();//用集合存储该天最后一个和后一天第一个班次下标
        int i=0;//下标
        for (int j = 0; j <28 ; j++) {
            LocalDate c=localDate.plusDays(j);

            if (j!=27){
                QueryWrapper<Classes> wrapper = new QueryWrapper<>();
                wrapper.eq("create_data", c);
                wrapper.orderByAsc("start_time"); //升序
                List<Classes> list = classesService.list(wrapper); //获取每天的班次
                int classesLength = list.size();  //每天班次长度
                i=i+classesLength;
                arrayList.add(i);
                arrayList.add(i+1);
            }
        }

        //判断是否有员工最后一个和后一天第一个班次为1
        int count=0;
        for (int j = 0; j < arrayList.size(); j++) {
            for (int k = 0; k < employeeArray.length; k++) {
                if (employeeClasses[k][j]==1&&employeeClasses[k][j+1]==1){
                    count++;
                }
            }
            j++;
        }

        if (count>=27){  //28-1
            return true;
        }else {
            return false;
        }
    }


    /**
     * 偏好规则
     * 将符合条件的班次按照员工偏好进行筛选，得到可用的班次列表。
     * 如果有多个员工根据员工等级1到4(暂时不用)，根据员工id，查询偏好，并与班次相比较，符合为true
     * Schedule这个后面应为ScheduleByAll 这里后期应根据日期
     */
    public boolean doesMeetPreferences(String employeeId, ScheduleByOne scheduleByOne) {
        //在EmployeePreference表中根据id获取员工偏好及其偏好值
        QueryWrapper<EmployeePreference> employeePreferenceQueryWrapper = new QueryWrapper<>();
        employeePreferenceQueryWrapper.eq("EmployeeId", employeeId);
        List<EmployeePreference> list = employeePreferenceService.list(employeePreferenceQueryWrapper);
        //根据id获取员工班次
        List<Classes> classesByEmployeeId = scheduleByOne.getClassesByEmployeeId(employeeId);
        JSONObject jsonObject = null;
        boolean a = true;
        boolean b = true;
        boolean c = true;
        for (EmployeePreference employeePreference : list) {
            String preferenceTypeId = employeePreference.getPreferenceTypeId();
            String preferenceValue = employeePreference.getPreferenceValue();  //偏好值，json
            jsonObject = JSON.parseObject(preferenceValue);//转为json字符串
            //  TODO 偏好一 121
            if (preferenceTypeId.equals("121")) {

            }

            //偏好二 122
            if (preferenceTypeId.equals("122")) {
                for (Classes classes : classesByEmployeeId) {
                    LocalTime startTime = classes.getStartTime();
                    LocalTime endTime = classes.getEndTime();
                    Long endMinute = timeToMinute(endTime);
                    Long startMinute = timeToMinute(startTime);
                    int start = jsonObject.getInteger("start") * 60;
                    int end = jsonObject.getInteger("end") * 60;

                    if (!(startMinute >= start && endMinute <= end)) { //即工作时间都在在偏好以内
                        b = false;
                        break;
                    }
                }
            }

            //偏好三 123
            if (preferenceTypeId.equals("123")) {
                //每天不超过时长
                //获取每天的总时长
                Long dayTotalDuration = scheduleByOne.getDayTotalDuration(employeeId, scheduleByOne);
                int dayLength = jsonObject.getInteger("dayLength") * 60;
                //TODO 周
                int weekLength = jsonObject.getInteger("weekLength");
                if (dayLength < dayTotalDuration) {
                    c = false;
                }
            }
        }

        return a && b && c;
    }


    /**
     * 适应度
     * 已完成初步测试
     * 计算适应度  空闲班次数   工时数
     */
    public double[] evaluate(int[][] employeeClasses, String[] mouthClassesArray, String[] employeeArray) {

        double[] fitness = new double[2];
        //总共开放班次数
        double count = 0;

        //获取空闲班次
//        List<Classes> classesList = getUnfilledClassesList(employeeClasses, mouthClassesArray, employeeArray);
//        for (int i = 0; i < classesList.size(); i++) {
//            count = count + classesList.get(i).getEmployeeNumber();
//        }

        //总classes内班次数减1的数量
        int totalNumber = 0;
        for (int i = 0; i < mouthClassesArray.length; i++) {
            totalNumber = totalNumber + classesService.getById(mouthClassesArray[i]).getEmployeeNumber();
        }

        int a = 0;
        for (int i = 0; i < employeeArray.length; i++) {
            for (int j = 0; j < mouthClassesArray.length; j++) {
                if (employeeClasses[i][j] == 1) {
                    a++;
                }
            }
        }

//        System.out.println("a="+a);
        fitness[0] = totalNumber - a;

        //总共工时数     最大工时数
        long totalWorkTime = 0;

        for (int i = 0; i < employeeArray.length; i++) {
            totalWorkTime = getDayTotalDuration(employeeArray[i], employeeClasses, mouthClassesArray, employeeArray) + totalWorkTime;
        }


        //根据班次获取最大工作时长
        long max = 0;
        for (int i = 0; i < mouthClassesArray.length; i++) {
            Classes classesServiceById = classesService.getById(mouthClassesArray[i]);
            LocalTime startTime = classesServiceById.getStartTime();
            LocalTime endTime = classesServiceById.getEndTime();
            Integer employeeNumber = classesServiceById.getEmployeeNumber();

            long startMinute = timeToMinute(startTime);
            long endMinute = timeToMinute(endTime);

            max = max + (endMinute - startMinute) * employeeNumber;
        }


        double totalWorkTimePercentage = (double) totalWorkTime / max;


        DecimalFormat format = new DecimalFormat("#.000");  //控制精度
        String s = format.format(totalWorkTimePercentage);
        double v = Double.parseDouble(s);

        fitness[1] = v;

        return fitness;

    }


    @Test
    public void test01() {
        //直接月开始

        //存入员工id
        //根据门店获取员工数据
        QueryWrapper<ServiceEmployee> wrapper = new QueryWrapper<>();
        wrapper.eq("shop_id", "1");
        //1,2,3,4顺序存入
        wrapper.orderByAsc("position");
        List<ServiceEmployee> employeeList = employeeService.list(wrapper);

        String[] employeeArray = new String[employeeList.size()];
        for (int j = 0; j < employeeList.size(); j++) {
            employeeArray[j] = employeeList.get(j).getId();
        }

//        System.out.println(employeeArray.length);


        //存入班次id
        LocalDate localDate = LocalDate.of(2023, 2, 1);
        String[] mouthClassesArray = new String[classesService.list(null).size()];
        int a = 0; //mouthClassesArray  实时数据长度
        for (int i = 0; i < 28; i++) {
            LocalDate localDate1 = localDate.plusDays(i);
            //根据日期和starttime顺序存入班次id
            QueryWrapper<Classes> queryWrapper = new QueryWrapper<>();
            //时间从小到大顺序存入
            queryWrapper.eq("create_data", localDate1);
            queryWrapper.orderByAsc("start_time");

            List<Classes> classesList = classesService.list(queryWrapper);

            for (int j = 0; j < classesList.size(); j++) {
                mouthClassesArray[j + a] = classesList.get(j).getId();
            }
            a = a + classesList.size();
        }



        //先手动生成一组排班测试方法
//        int[][] array={
//                //           1                   1
//                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//                {0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}
//        };

//        for (int i = 0; i < employeeArray.length; i++) {
//            System.out.print("{");
//            for (int j = 0; j < mouthClassesArray.length; j++) {
//                System.out.print(","+array[i][j]);
//            }
//            System.out.print("}");
//            System.out.println("");
//        }

//        System.out.println(isTrueForWorkTime(employeeArray, mouthClassesArray, array));

        /**
         * 初始化种群  种群数暂定为50
         * 基因序列使用二进制 1为有员工 0没有员工
         * 应该满足的规则，
         * 1.在classes内排班  即固定规则和部分自定义规则
         * 2.满足自定义规则
         * 3.尽量瞒足偏好
         * */

        /**随机生成50组排班*/

        ArrayList<int[][]> aList = new ArrayList<>();
        for (int n = 0; n < 1; n++) {
            int[][] employeeClasses = new int[employeeArray.length][mouthClassesArray.length];
            //获取每天的班次数

            //遍历每个班次，随机选择对应的员工数设置为1
            Date createData;

            ArrayList<int[]> lists = new ArrayList<>();

            ArrayList<Integer> list = new ArrayList<>();
            int num=0;
            LocalDate l=LocalDate.of(2023,2,1);
            for (int i = 0; i < 28; i++) { //用于判断b的值
                LocalDate date=l.plusDays(i);
                QueryWrapper<Classes> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("create_data",date);
                int size = classesService.list(queryWrapper).size();
                num=size+num;
                list.add(num);
            }

//            System.out.println(list);
            for (int i = 0; i < mouthClassesArray.length; i++) {
                boolean b=false;

                for (int j = 0; j < list.size(); j++) {
                    if (i==list.get(j)){
                        b=true;
                        break;
                    }
                }

//                System.out.println(b);

                Integer number = classesService.getById(mouthClassesArray[i]).getEmployeeNumber();
                int[] random;
                if (lists.isEmpty()){
                    random = random(number, employeeArray,null,b);  //随机员工
                }else {
                    random = random(number, employeeArray,lists.get(i-1),b);  //随机员工
//                    for (int bb = 0; bb < lists.get(i-1).length; bb++) {
//                        System.out.print(lists.get(i-1)[bb]+",");
//                    }
//                    System.out.println("上一个");
                }
                lists.add(random);
//
//                for (int bb = 0; bb < random.length; bb++) {
//                    System.out.print(random[bb]+",");
//                }
//                System.out.println("");


                for (int k = 0; k < random.length; k++) {
                    employeeClasses[random[k]][i] = 1;
                }

            }

//            for (int[] aa:lists
//                 ) {
//                for (int i = 0; i < aa.length; i++) {
//                    System.out.print(aa[i]+",");
//                }
//                System.out.println("");
//
//            }


            for (int i = 0; i < employeeArray.length; i++) {
                System.out.println("");
                for (int j = 0; j < mouthClassesArray.length; j++) {
                    System.out.print(employeeClasses[i][j] + ",");

                }
            }



            //去除一些恶不符合规则的排班
            //固定规则
            boolean trueForWorkTime = isTrueForWorkTime(employeeArray, mouthClassesArray, employeeClasses);

//            System.out.println("trueForWorkTime"+trueForWorkTime);

            //自定义规则
            //开店，关店，客流已符合
            //一周内员工无班次天数不能超过两天
//            boolean restExceedTwoDays = restExceedTwoDays(employeeArray, mouthClassesArray, employeeClasses);

//            System.out.println("restExceedTwoDays"+restExceedTwoDays);
            //前一天21点到23点班次的员工里必须有一位员工在第二天8点到12点内值班。  未测
//            boolean ruleThree = ruleThree(employeeArray, employeeClasses);
//            System.out.println("ruleThree"+ruleThree);

            //TODO 偏好规则   暂时先不写

            if (trueForWorkTime){
                aList.add(employeeClasses);
            }
            //
        }



        //计算适应度，排序并选择

        /************************************排班*******************************分界线***********************测试*****************************************************/

//
//        int[][] arr=new int[employeeArray.length][mouthClassesArray.length];
//
////        for (int i = 0; i <4 ; i++) {
////            for (int j = 0; j <5 ; j++) {
////                arr[i][j]=1;
////            }
////        }
//
//        boolean ruleThree = ruleThree(employeeArray, array);
//        System.out.println("ruleThree+"+ruleThree);


        System.out.println(aList.size());
//        System.out.println(employeeArray.length);

//                for (int i = 0; i < employeeArray.length; i++) {
//            System.out.println("");
//            for (int j = 0; j < mouthClassesArray.length; j++) {
//                System.out.print(aList.get(0)[i][j] + ",");
//
//            }
//        }




    }

}


