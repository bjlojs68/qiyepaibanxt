package com.bwtp.demo;
import java.time.LocalTime;

import java.text.DecimalFormat;
import java.time.*;
import java.util.Date;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bwtp.commonbases.exceptionhandler.BwtpException;
import com.bwtp.staffService.entity.*;
import com.bwtp.staffService.service.*;
import com.bwtp.staffService.staffApplication;
import com.jayway.jsonpath.internal.function.numeric.Max;
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
public class demo02 {

    @Resource
    private SchedulingService schedulingService;

    @Resource
    private ClassesService classesService;


    @Resource
    private ServiceEmployeeService employeeService;

    @Resource
    private EmployeePreferenceService employeePreferenceService;


    private String idShop="1";


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
    private Long weekWorkTime;

    public Long getWeekWorkTime() {
        return weekWorkTime;
    }

    public void setWeekWorkTime(Long weekWorkTime) {
        this.weekWorkTime = weekWorkTime;
    }

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

    /****************************上面的没用***************************分界线**********************************下面为正式遗传算法*******************************************************************/


    /**
     * 注意该排班算法中日期固定，即2023-01-30到2023-02-26，只求了二月份排班，方便排班。如果要通用，还应增加假期，将月份作为变量。
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
     * 就是管理员可以在不限，导购员，收银员三个选项中选择
     * 当选择不限时随机值不限  0
     * 当选择导购员时随机值在对应id下标的数组中随机   3
     * 当选择收银员时随机值在对应id下标的数组中随机   4
     *
     * choose  要从前端传入
     * */
    //开店  关店
    public List<Integer> chooseJob(int choose,String[] employeeArray){
        if (choose==0){
            return null;
        }else {
            QueryWrapper<ServiceEmployee> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("position", choose);
            List<ServiceEmployee> list = employeeService.list(queryWrapper); //获取该职位的员工集合
            List<Integer> arrayList = new ArrayList<>();//用来存储该职位的员工id在employeeArray中的下标
            for (int i = 0; i < employeeArray.length; i++) {
                for (int j = 0; j < list.size(); j++) {
                    if (employeeArray[i].equals(list.get(j).getId())){
                        arrayList.add(i);
                    }
                }
            }
            return arrayList;
        }

    }

    /**
     * 偏好规则
     * 用于生成被选择的员工数组，但是全部员工也要传入，当偏好内员工与固定规则和自定规则相违背时使用全员工
     *
     * 偏好一：时间选择之间差在4小时以上
     * 偏好二：天数相差5天以上
     * 偏好三：日时长选择4小时以上，周时长20小时以上
     *
     * employeeId   员工id
     * classesId    班次id
     *
     * 适应度即返回多少个true
     * */
    public boolean preferenceEmployee(String employeeId,String classesId){
        QueryWrapper<EmployeePreference> wrapper = new QueryWrapper<>();
        wrapper.eq("EmployeeId",employeeId);
        wrapper.eq("shop_id",idShop);
        EmployeePreference employeePreference = employeePreferenceService.getOne(wrapper);
        String preferenceTypeId = employeePreference.getPreferenceTypeId();
        String preferenceValue = employeePreference.getPreferenceValue();
        JSONObject jsonObject =null;
        if (preferenceValue==null){  //无偏好直接返回true
            return true;
        }else {
            jsonObject=JSON.parseObject(preferenceValue);//转为json字符串
        }

        Classes classes = classesService.getById(classesId);
        LocalTime startTime = classes.getStartTime();
        LocalTime endTime = classes.getEndTime();
        Date createData = classes.getCreateData();

        LocalDate localDate = Instant.ofEpochMilli(createData.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();//将data转为localdata
        int value = localDate.getDayOfWeek().getValue();//星期几 1-7

        //这个是因为我只给每个员工设置了一个偏好
        if (preferenceTypeId.equals("121")){  //如周３到周６.缺省为全部
            Integer start = jsonObject.getInteger("start");
            Integer end = jsonObject.getInteger("end");  //start到end
            if (start<=value&&value<=end){
                return true;
            }else {
                return false;
            }
        }else
        if (preferenceTypeId.equals("122")){  //如上午８点到下午６点。缺省为全部。
            Integer start = jsonObject.getInteger("start");
            Integer end = jsonObject.getInteger("end");  //start到end
            if (startTime.getHour()>=start&&endTime.getHour()<=end){
                return true;
            }else {
                return false;
            }
        }else {
            return true;  //无偏好
        }
    }

    /**监管每周员工工作时长  时长不够优先安排排班
     * 保证每个员工工作时长差不多
     * 每周工作时长大于等于20小于等于40小时
     *
     * employeeId   员工id
     * classesId    班次id
     * */
    //保存dayWorkTime及其员工  一一对应
    private int[] eleDayWorkHour; //储存班次次数

    public int[] getEleDayWorkHour() {
        return eleDayWorkHour;
    }

    public void setEleDayWorkHour(int[] eleDayWorkHour) {
        this.eleDayWorkHour = eleDayWorkHour;
    }

    /**
     * 工具方法  重要
     * 随机选择n个员工   完成初步测试
     * numberLength    该班次所需员工数
     * employeeArray   所有员工id数组
     * selectedNumsB   前一个班次员工id数组下标
     * selectedNumsTwo 前两个班次员工id数组下标
     * b               时间判断，是否为隔天邻接班次
     * chooseJob       被选择员工数组，要求固定职业
     *
     * mouthClassesBefore 前两个班次索引
     *
     * mouthClassesThree 前两个班次索引
     *
     * mouthClassesNow    当前班次索引
     *
     * selectedNumsThree  前三个班次索引
     *
     */
    public int[] random(int mouthClassesBefore,int mouthClassesNow,int mouthClassesThree,int numberLength, String[] mouthClassesArray,String[] employeeArray,int[] selectedNumsB,int[] selectedNumsTwo,int[] selectedNumsThree,boolean b,int[] chooseJob) {  //selectedNumsB 上一个被选中的数  b是否可以和上一个数重复
        int[] nums;
        if (chooseJob==null){  //选择0
            nums = new int[employeeArray.length];
            for (int i = 0; i < employeeArray.length; i++) {
                nums[i] = i;
            }
        }else {//选择特定员工
            nums=chooseJob;
        }

        //满2不能选  遍历eleDayWorkHour,满2就把该下标删除
        for (int i = 0; i < eleDayWorkHour.length; i++) {
            if(eleDayWorkHour[i]==2){
                for (int j = 0; j < nums.length; j++) {
                    if (nums[j]==i){
                        //移除
                        nums[j] = nums[nums.length - 1]; // 将选中的数与最后一个数交换
                        nums = Arrays.copyOf(nums, nums.length - 1); // 删除最后一个数 ,以避免重复选取。
                    }
                }

            }
        }

        int n = numberLength; // 需要随机选取的数的个数
        int[] selectedNums = new int[n]; // 用于存储选中的数
        Random random = new Random();
        int countPosition=0;
        for (int i = 0; i < n; i++) {
            int index;// 随机生成一个索引

            //判断是否在上一个选中的数中存在 存在则重新选取，隔天不算
            if (b||selectedNumsB==null){ //可以重复，且至少有一个为重复被选，思路：第一个班次在上一个班次已选员工中选择员工，然后其他随机
                if (i==0&&selectedNumsB!=null) {
                    while (true){
                            int count = 0; //用于判断是否跳出while循环
                            index = random.nextInt(nums.length);
                            for (int j = 0; j < selectedNumsB.length; j++) {
                                if (nums[index] == selectedNumsB[j]) {
                                    count++;
                                    break;
/*                                Integer position = employeeService.getById(employeeArray[nums[index]]).getPosition();//职位
                                //如果出现1,2则count加一
                                if (position==1||position==2){
                                    countPosition++;
                                }
                                System.out.println("countPosition"+countPosition);
                                //出现两次则重新选择
                                if (countPosition<=1){
                                    count++;
                                    break;
                                }*/
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
                    boolean bd=true;  //用于判断是否跳出while循环
                    if (count==0){
                        if (selectedNumsTwo!=null){
                            for (int j = 0; j < selectedNumsTwo.length; j++) {
                                if (nums[index] == selectedNumsTwo[j]) { //员工id
                                    Classes classesBefore = classesService.getById(mouthClassesArray[mouthClassesBefore]);
                                    Classes classesNow = classesService.getById(mouthClassesArray[mouthClassesNow]);

                                    if (classesBefore.getCreateData().equals(classesNow.getCreateData())){
                                        if (classesBefore.getStartTime().compareTo(classesNow.getStartTime())<0&&
                                                classesBefore.getEndTime().compareTo(classesNow.getStartTime())>=0){
                                            bd=false;
                                            break;
                                        }

                                    }else {
                                        break;
                                    }
                                }
                            }
                        }

                        if (selectedNumsThree!=null){
                            for (int j = 0; j < selectedNumsThree.length; j++) {
                                if (nums[index] == selectedNumsThree[j]) { //员工id
                                    Classes classesBefore = classesService.getById(mouthClassesArray[mouthClassesThree]);
                                    Classes classesNow = classesService.getById(mouthClassesArray[mouthClassesNow]);

                                    if (classesBefore.getCreateData().equals(classesNow.getCreateData())){
                                        if (classesBefore.getStartTime().compareTo(classesNow.getStartTime())<0&&classesBefore.getEndTime().compareTo(classesNow.getStartTime())>=0){
                                            bd=false;
                                            break;
                                        }

                                    }else {
                                        break;
                                    }
                                }
                            }
                        }


                    }
                    if (bd&&count==0){
                        break;
                    }
//                    if (count==0){ //没有经过break
//                        break;
//                    }
                }

            }

            selectedNums[i] = nums[index]; // 将选中的数存储到数组中
            Integer position = employeeService.getById(employeeArray[nums[index]]).getPosition();//职位
            nums[index] = nums[nums.length - 1]; // 将选中的数与最后一个数交换
            nums = Arrays.copyOf(nums, nums.length - 1); // 删除最后一个数 ,以避免重复选取。
            //如果已经选择了经理或副经理直接在随机选择数组nums中移除
            //如果出现1,2则移除职位为1或2的数
            if (position==1||position==2){
                for (int j = 0; j < nums.length; j++) {
                    Integer positionF = employeeService.getById(employeeArray[nums[j]]).getPosition();//职位
                    if (positionF==1||positionF==2){
                        //移除
                        nums[j] = nums[nums.length - 1]; // 将选中的数与最后一个数交换
                        nums = Arrays.copyOf(nums, nums.length - 1); // 删除最后一个数 ,以避免重复选取。
                    }
                }

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
        wrapper.eq("shop_id", idShop);
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

        LocalDate localDate=LocalDate.of(2023,1,30);//固定
        int classesSubscript = 0;// 每次开始遍历的下标
        for (int j = 0; j < 35; j++) {
            long dayWorkTime = 0; //每天工作时长
            LocalDate c=localDate.plusDays(j);

            if (j!=0){
                QueryWrapper<Classes> wrapper = new QueryWrapper<>();
                wrapper.eq("create_data", c.minusDays(1)); //前一天
                wrapper.eq("shop_id", idShop);
                wrapper.orderByAsc("start_time"); //升序
                List<Classes> list = classesService.list(wrapper); //获取每天的班次
                int classesLength = list.size();  //每天班次长度
                classesSubscript=classesSubscript+classesLength;
            }

            QueryWrapper<Classes> wrapper = new QueryWrapper<>();
            wrapper.eq("create_data", c);
            wrapper.eq("shop_id", idShop);

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
        LocalDate a = LocalDate.of(2023, 1, 30);
        for (int i = 0; i < 35; i++) {
            LocalDate c = a.plusDays(i);
            List<Long> longList = getContinuousDuration(employeeArray, mouthClassesArray, employeeClasses, c);//获取每个员工日连续工作时长
            for (int j = 0; j < longList.size(); j++) {
                if (longList.get(j) > 240) {
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
        for (int i = 0; i <employeeArray.length; i++) {
            long weekHours=0;
            for (int j = 0; j <mouthClassesArray.length; j++) {
                Date data = classesService.getById(mouthClassesArray[j]).getCreateData();
                LocalDate localDate = Instant.ofEpochMilli(data.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                if (localDate.getDayOfWeek().getValue()==1){  //为星期一则重置
//                    System.out.println(weekHours+"---weekHours");
                    if (weekHours>2400){  //超过40个小时
                        return false;
                    }
                    weekHours=0;
                }else if (employeeClasses[i][j]==1){
                    Classes classesServiceById = classesService.getById(mouthClassesArray[j]);
                    long endTime = timeToMinute(classesServiceById.getEndTime());
                    long startTime= timeToMinute(classesServiceById.getStartTime());
                    weekHours=weekHours+endTime-startTime;
                }
            }
        }

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
     * 自定义规则   先不用
     * 一周内员工无班次天数不能超过两天。
     * 这个只适用于28天的月份      初步测试完成
     */
    public boolean restExceedTwoDays(String[] employeeArray, String[] mouthClassesArray, int[][] employeeClasses) {
        //算出每个员工一周以内无班次数
        LocalDate a = LocalDate.of(2023, 1, 30);

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
     * 自定义规则
     前一天21点到23点班次的员工里必须有一位员工在第二天8点到12点内值班。  初步完成测试
     */
    public boolean ruleThree(String[] employeeArray, int[][] employeeClasses){
        LocalDate localDate=LocalDate.of(2023,1,30);
        ArrayList<Integer> arrayList = new ArrayList<>();//用集合存储该天最后一个和后一天第一个班次下标
        int num=0;//下标
        for (int i = 0; i <35; i++) { //用于判断b的值
            LocalDate date=localDate.plusDays(i);
            QueryWrapper<Classes> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("shop_id",idShop);
            queryWrapper.eq("create_data",date);
            int size = classesService.list(queryWrapper).size();
            num=size+num;
            arrayList.add(num);
        }

        //判断是否有员工最后一个和后一天第一个班次为1
        int count=0;
        for (int j = 0; j < arrayList.size()-1; j++) {
            for (int k = 0; k < employeeArray.length; k++) {
                if (employeeClasses[k][arrayList.get(j)] == 1 && employeeClasses[k][arrayList.get(j)-1] == 1) {
                    count++;
                }

            }
        }

        if (count>=34){  //28-1
            return true;
        }else {
            return false;
        }
    }


    /**
     * 自定义规则  初始化中使用  考虑判断交叉，变异
     * 经理和副经理不能在同一个班次中  直接判断
     * 思路：在随机中加个职位判断
     * */



    /**
     * 偏好规则
     * 将符合条件的班次按照员工偏好进行筛选，得到可用的班次列表。
     * 如果有多个员工根据员工等级1到4(暂时不用)，根据员工id，查询偏好，并与班次相比较，符合为true
     * Schedule这个后面应为ScheduleByAll 这里后期应根据日期
     */


    /**
     * 适应度
     * 已完成初步测试
     * 计算适应度  空闲班次数   工时数
     */
    public double[] evaluate(int[][] employeeClasses, String[] mouthClassesArray, String[] employeeArray) {

        double[] fitness = new double[2];
        //总共开放班次数
        double count = 0;

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

        double ab=(totalNumber - a)/totalNumber;
        DecimalFormat format = new DecimalFormat("#.000");  //控制精度
        String s = format.format(ab);
        fitness[0]= Double.parseDouble(s);


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


        System.out.println(max);

        double totalWorkTimePercentage = (double) totalWorkTime / max;

        String sb = format.format(totalWorkTimePercentage);
        double v = Double.parseDouble(sb);

        fitness[1] = v;

        return fitness;

    }

    /**
     * 计算标准差
     * */
    public double getMouthWorkHours(int[][] employeeClasses, String[] mouthClassesArray, String[] employeeArray){

        int[] MouthWorkHours=new int[employeeArray.length];
        for (int i = 0; i < employeeArray.length; i++) {
            int mouth=0;
            for (int j = 0; j < mouthClassesArray.length; j++) {
                if (employeeClasses[i][j]==1){
                    Classes byId = classesService.getById(mouthClassesArray[j]);

                    Long startTime = timeToMinute(byId.getStartTime());
                    Long endTime = timeToMinute(byId.getEndTime());
                    mouth=mouth+(int)(endTime-startTime);
                }
            }
            MouthWorkHours[i]=mouth;
        }

        //标准差
        double sum = 0.0;
        for (double i : MouthWorkHours) {
            sum += i;
        }

        int length = MouthWorkHours.length;
        double mean = sum / length;

        double standardDeviation = 0.0;
        for (double num : MouthWorkHours) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / length);
    }

    @Test
    public void test01() {
        //直接月开始

        //存入员工id
        //根据门店获取员工数据
        QueryWrapper<ServiceEmployee> wrapper = new QueryWrapper<>();
        wrapper.eq("shop_id", idShop);
        //1,2,3,4顺序存入
        wrapper.orderByAsc("position");
        List<ServiceEmployee> employeeList = employeeService.list(wrapper);

        String[] employeeArray = new String[employeeList.size()];
        for (int j = 0; j < employeeList.size(); j++) {
            employeeArray[j] = employeeList.get(j).getId();
        }



        //存入班次id
        LocalDate localDate = LocalDate.of(2023, 1, 30);
        QueryWrapper<Classes> queryWrapperd = new QueryWrapper<>();
        //时间从小到大顺序存入
        queryWrapperd.eq("shop_id", idShop);
        String[] mouthClassesArray = new String[classesService.list(queryWrapperd).size()];
        int a = 0; //mouthClassesArray  实时数据长度
        for (int i = 0; i <35; i++) {
            LocalDate localDate1 = localDate.plusDays(i);
            //根据日期和startTime顺序存入班次id
            QueryWrapper<Classes> queryWrapper = new QueryWrapper<>();
            //时间从小到大顺序存入
            queryWrapper.eq("create_data", localDate1);
            queryWrapper.eq("shop_id", idShop);
            queryWrapper.orderByAsc("start_time");

            List<Classes> classesList = classesService.list(queryWrapper);

            for (int j = 0; j < classesList.size(); j++) {
                mouthClassesArray[j + a] = classesList.get(j).getId();
            }
            a = a + classesList.size();

        }



        /**
         * 初始化种群  种群数暂定为50
         * 基因序列使用二进制 1为有员工 0没有员工
         * 应该满足的规则，
         * 1.在classes内排班  即固定规则和部分自定义规则
         * 2.满足自定义规则
         * 3.尽量瞒足偏好
         * */

        /**随机生成50组排班*/
        //TODO 职位选择
        int openEmployeeId=0,closeEmployeeId=0; //0,3,4 默认为0
        ArrayList<int[][]> aList = new ArrayList<>();
        for (int n = 0; n <20; n++) {
            //为0数组
            setEleDayWorkHour(new int[employeeArray.length]);

            //储存排班
            int[][] employeeClasses = new int[employeeArray.length][mouthClassesArray.length];

            ArrayList<int[]> lists = new ArrayList<>();    //用来存储班次

            ArrayList<Integer> list = new ArrayList<>();   //用来存储每天最后一个班次下标
            int num=0;
            LocalDate l=LocalDate.of(2023,1,30);
            for (int i = 0; i < 35; i++) { //用于判断b的值
                LocalDate date=l.plusDays(i);
                QueryWrapper<Classes> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("create_data",date);
                queryWrapper.eq("shop_id",idShop);
                int size = classesService.list(queryWrapper).size();
                num=size+num;
                list.add(num);
            }

            //遍历每个班次，随机选择对应的员工数设置为1
            for (int i = 0; i < mouthClassesArray.length; i++) {

                int mouthClassesBefore=0;  //前两个班次索引
                int mouthClassesThree=0;  //前三个班次索引

                if(i>=2){
                    mouthClassesBefore=i-2;
                }
                if (i>=3){
                    mouthClassesThree=i-3;
                }

                boolean b=false; //用于判断是否可以重复
                for (int j = 0; j < list.size(); j++) {
                    if (i==list.get(j)){
                        //满一天，员工日次数统计置空
                        setEleDayWorkHour(new int[employeeArray.length]);
                        b=true;
                        break;
                    }
                    if (list.get(j)>i){
                        break;
                    }
                }

                //暂时不用
/*                List<Integer> chooseJob=new ArrayList<>();
                for (int j = 0; j < list.size(); j++) {
                    //判断是否是第一个还是最后一个班次 根据班次list集合来判断   存储的是数组下标
                    //[9, 17, 24, 32, 41, 50, 57, 64, 74, 83, 91, 99, 108, 117, 125, 133, 141, 149, 157, 167, 176, 185, 193, 201, 209, 217, 225, 232]

                    if (i==list.get(j)&&openEmployeeId!=0||i==0&&openEmployeeId!=0){  //第一个班次
                        //管理员设置特定职位的员工
                        chooseJob= chooseJob(openEmployeeId, employeeArray);
                        break;
                    }else if(i==list.get(j)-1&&closeEmployeeId!=0){ //最后一个班次
                        chooseJob= chooseJob(closeEmployeeId, employeeArray);
                        break;
                    }

                    if (list.get(j)>i){
                        break;
                    }
                }

                int[] jobChoose;
                if (!chooseJob.isEmpty()) {
                    jobChoose = new int[chooseJob.size()];
                    for (int j = 0; j < chooseJob.size(); j++) {
                        jobChoose[j] = chooseJob.get(j);
                    }
                } else {
                    jobChoose = null;
                }*/

                Integer number = classesService.getById(mouthClassesArray[i]).getEmployeeNumber(); //该班次所需人员
                int[] random;
                if (lists.isEmpty()){
                    random = random(mouthClassesBefore,i,mouthClassesThree,number, mouthClassesArray,employeeArray,null,null,null,b,null);  //随机员工
                }else if (lists.size()<2){
                    random = random(mouthClassesBefore,i,mouthClassesThree,number, mouthClassesArray,employeeArray,lists.get(i-1),null,null,b,null);  //随机员工
                }else if (lists.size()<3){
                    random = random(mouthClassesBefore,i,mouthClassesThree,number, mouthClassesArray,employeeArray,lists.get(i-1),lists.get(i-2),null,b,null);  //随机员工
                }else {
                    random = random(mouthClassesBefore,i,mouthClassesThree,number, mouthClassesArray,employeeArray,lists.get(i-1),lists.get(i-2),lists.get(i-3),b,null);  //随机员工
                }
                lists.add(random);

                for (int k = 0; k < random.length; k++) {
                    employeeClasses[random[k]][i] = 1;
                }

                //统计每个员工每天已安排班次数
                for (int k = 0; k < random.length; k++) {
                    eleDayWorkHour[random[k]]++;
                }

            }

            //去除一些恶不符合规则的排班
            //固定规则
//            boolean trueForWorkTime = isTrueForWorkTime(employeeArray, mouthClassesArray, employeeClasses);

            //自定义规则
            //开店，关店，客流已符合

            //前一天21点到23点班次的员工里必须有一位员工在第二天8点到12点内值班。
//            boolean ruleThree = ruleThree(employeeArray, employeeClasses);

//            if (trueForWorkTime&&ruleThree){
                aList.add(employeeClasses);
//            }
        }

        System.out.println(aList.size());

        //计算适应度，排序并选择
        double[] comparisonValueArray=new double[aList.size()];
        for (int n = 0; n < aList.size(); n++) {
            //时长   班次
            double[] evaluate = evaluate(aList.get(n), mouthClassesArray, employeeArray);
            double number=evaluate[0];   //开放班次
            double time=evaluate[1];     //工作时长

            //标准差
            double mouthWorkHours = getMouthWorkHours(aList.get(n), mouthClassesArray, employeeArray);
            DecimalFormat format = new DecimalFormat("#.0000");  //控制精度
            String s = format.format(mouthWorkHours);
            double mouthWorkHour = Double.parseDouble(s);

            //统计所有通过偏好数
            int count=0;
            for (int i = 0; i <employeeArray.length ; i++) {
                for (int j = 0; j <mouthClassesArray.length ; j++) {
                    if (aList.get(n)[i][j]==1){
                        if(preferenceEmployee( employeeArray[i], mouthClassesArray[j])){
                            count++;
                        }
                    }
                }
            }

//            System.out.println("开放班次数="+number);
//            System.out.println("工作时长数="+time);
//            System.out.println("标准差诶啊="+mouthWorkHours);
//            System.out.println("通过偏好数="+count);


            //乘以权重之后相加
            double comparisonValue= number*0.35+time*0.35+mouthWorkHour*0.2+count*0.1;
            comparisonValueArray[n]=comparisonValue;
        }

        double max= Integer.MIN_VALUE;
        for (int i = 0; i < comparisonValueArray.length; i++) {
            max=Math.max(max,comparisonValueArray[i]);
        }

        //获取该部分最优的排班
        int[][] optimalScheduling=new int[employeeArray.length][mouthClassesArray.length];
        for (int i = 0; i < comparisonValueArray.length; i++) {
            if (max==comparisonValueArray[i]){
                optimalScheduling=aList.get(i);
            }
        }

//        for (int i = 0; i < employeeArray.length; i++) {
//            for (int j = 0; j < mouthClassesArray.length; j++) {
//                System.out.print(optimalScheduling[i][j]);
//            }
//            System.out.println("");
//        }

        //先清空数据库
//        schedulingService.remove(null);
        //将该排班存入数据

        for (int i = 0; i < employeeArray.length; i++) {
            Scheduling scheduling = new Scheduling();
            String s="";
            scheduling.setSign(idShop);
            scheduling.setEmployeeId(employeeArray[i]);
            for (int j = 0; j < mouthClassesArray.length; j++) {
                s=s+optimalScheduling[i][j];
            }
            scheduling.setRosterString(s);
            schedulingService.save(scheduling);
        }

    }

}


