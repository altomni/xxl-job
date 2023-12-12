package com.xxl.job.admin.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.dto.XxlJobUpdateBySendTime;
import com.xxl.job.admin.core.exception.XxlJobException;
import com.xxl.job.admin.core.model.XxlExtendJobInfo;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.admin.core.route.ExecutorRouteStrategyEnum;
import com.xxl.job.admin.core.scheduler.MisfireStrategyEnum;
import com.xxl.job.admin.core.scheduler.ScheduleTypeEnum;
import com.xxl.job.admin.core.thread.JobScheduleHelper;
import com.xxl.job.admin.core.thread.JobTriggerPoolHelper;
import com.xxl.job.admin.core.trigger.TriggerTypeEnum;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.service.LoginService;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.job.core.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * index controller
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
@RequestMapping("/jobinfo")
public class JobInfoController {
	private static Logger logger = LoggerFactory.getLogger(JobInfoController.class);

	@Resource
	private XxlJobGroupDao xxlJobGroupDao;
	@Resource
	private XxlJobService xxlJobService;

	@Value("${emaily.access_token.key}")
	private String emailyAccessTokenKey;

	@Value("${emaily.access_token.value}")
	private String emailyAccessTokenValue;

	@Value("${apn.access_token.key}")
	private String apnAccessTokenKey;

	@Value("${apn.access_token.value}")
	private String apnAccessTokenValue;
	@Value("${openvpn.access_token.key}")
	private String openvpnAccessTokenKey;

	@Value("${openvpn.access_token.value}")
	private String openvpnAccessTokenValue;

	@Resource
	private XxlJobInfoDao xxlJobInfoDao;

	@RequestMapping
	public String index(HttpServletRequest request, Model model, @RequestParam(required = false, defaultValue = "-1") int jobGroup) {

		// 枚举-字典
		model.addAttribute("ExecutorRouteStrategyEnum", ExecutorRouteStrategyEnum.values());	    // 路由策略-列表
		model.addAttribute("GlueTypeEnum", GlueTypeEnum.values());								// Glue类型-字典
		model.addAttribute("ExecutorBlockStrategyEnum", ExecutorBlockStrategyEnum.values());	    // 阻塞处理策略-字典
		model.addAttribute("ScheduleTypeEnum", ScheduleTypeEnum.values());	    				// 调度类型
		model.addAttribute("MisfireStrategyEnum", MisfireStrategyEnum.values());	    			// 调度过期策略

		// 执行器列表
		List<XxlJobGroup> jobGroupList_all =  xxlJobGroupDao.findAll();

		// filter group
		List<XxlJobGroup> jobGroupList = filterJobGroupByRole(request, jobGroupList_all);
		if (jobGroupList==null || jobGroupList.size()==0) {
			throw new XxlJobException(I18nUtil.getString("jobgroup_empty"));
		}

		model.addAttribute("JobGroupList", jobGroupList);
		model.addAttribute("jobGroup", jobGroup);

		return "jobinfo/jobinfo.index";
	}

	public static List<XxlJobGroup> filterJobGroupByRole(HttpServletRequest request, List<XxlJobGroup> jobGroupList_all){
		List<XxlJobGroup> jobGroupList = new ArrayList<>();
		if (jobGroupList_all!=null && jobGroupList_all.size()>0) {
			XxlJobUser loginUser = (XxlJobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);
			if (loginUser.getRole() == 1) {
				jobGroupList = jobGroupList_all;
			} else {
				List<String> groupIdStrs = new ArrayList<>();
				if (loginUser.getPermission()!=null && loginUser.getPermission().trim().length()>0) {
					groupIdStrs = Arrays.asList(loginUser.getPermission().trim().split(","));
				}
				for (XxlJobGroup groupItem:jobGroupList_all) {
					if (groupIdStrs.contains(String.valueOf(groupItem.getId()))) {
						jobGroupList.add(groupItem);
					}
				}
			}
		}
		return jobGroupList;
	}
	public static void validPermission(HttpServletRequest request, int jobGroup) {
		XxlJobUser loginUser = (XxlJobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);
		if (!loginUser.validPermission(jobGroup)) {
			throw new RuntimeException(I18nUtil.getString("system_permission_limit") + "[username="+ loginUser.getUsername() +"]");
		}
	}

	@RequestMapping("/pageList")
	@ResponseBody
	public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
			@RequestParam(required = false, defaultValue = "10") int length,
			int jobGroup, int triggerStatus, String jobDesc, String executorHandler, String author) {

		return xxlJobService.pageList(start, length, jobGroup, triggerStatus, jobDesc, executorHandler, author);
	}

	@RequestMapping("/add")
	@ResponseBody
	public ReturnT<String> add(XxlJobInfo jobInfo) {
		return xxlJobService.add(jobInfo);
	}

	@RequestMapping("/update")
	@ResponseBody
	public ReturnT<String> update(XxlJobInfo jobInfo) {
		return xxlJobService.update(jobInfo);
	}

	@RequestMapping("/remove")
	@ResponseBody
	public ReturnT<String> remove(int id) {
		return xxlJobService.remove(id);
	}

	@RequestMapping("/stop")
	@ResponseBody
	public ReturnT<String> pause(int id) {
		return xxlJobService.stop(id);
	}

	@RequestMapping("/start")
	@ResponseBody
	public ReturnT<String> start(int id) {
		return xxlJobService.start(id);
	}

	@RequestMapping("/trigger")
	@ResponseBody
	//@PermissionLimit(limit = false)
	public ReturnT<String> triggerJob(int id, String executorParam, String addressList) {
		// force cover job param
		if (executorParam == null) {
			executorParam = "";
		}

		JobTriggerPoolHelper.trigger(id, TriggerTypeEnum.MANUAL, -1, null, executorParam, addressList);
		return ReturnT.SUCCESS;
	}


	@RequestMapping("/triggerOnce")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> triggerJobForHitalent(int id,Long jobId) {
		// force cover job param

		XxlJobInfo jobInfo = xxlJobInfoDao.loadById(id);
		if(jobInfo == null)
		{
			return  ReturnT.FAIL;
		}
		String params = jobInfo.getExecutorParam();
		if(jobId != null)
		{

			Gson gson = new Gson();
			JsonObject object = gson.fromJson(params,JsonObject.class);
			object.addProperty("jobId",jobId);
			params = gson.toJson(object);
			//System.out.println("-----------------param:"+params);
		}
		JobTriggerPoolHelper.trigger(id, TriggerTypeEnum.MANUAL, -1, null, params, null);
		return ReturnT.SUCCESS;
	}

	@RequestMapping("/nextTriggerTime")
	@ResponseBody
	public ReturnT<List<String>> nextTriggerTime(String scheduleType, String scheduleConf) {

		XxlJobInfo paramXxlJobInfo = new XxlJobInfo();
		paramXxlJobInfo.setScheduleType(scheduleType);
		paramXxlJobInfo.setScheduleConf(scheduleConf);

		List<String> result = new ArrayList<>();
		try {
			Date lastTime = new Date();
			for (int i = 0; i < 5; i++) {
				lastTime = JobScheduleHelper.generateNextValidTime(paramXxlJobInfo, lastTime);
				if (lastTime != null) {
					result.add(DateUtil.formatDateTime(lastTime));
				} else {
					break;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ReturnT<List<String>>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) + e.getMessage());
		}
		return new ReturnT<List<String>>(result);

	}

	@RequestMapping("/add-and-start")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> addAndStart(HttpServletRequest request, XxlJobInfo jobInfo) {
		logger.info(jobInfo.toString());
		if (!request.getHeader(emailyAccessTokenKey).equals(emailyAccessTokenValue)) {
			logger.error("[XXL-JOB-ADMIN: addAndStart @-1] Emaily access token is wrong!");
			return new ReturnT<String>(ReturnT.FAIL_CODE, "Emaily access token is wrong!");
		}
		return xxlJobService.addAndStart(jobInfo);
	}

	@RequestMapping("/add-and-start-openvpn")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> addAndStartOpenVPNJob(HttpServletRequest request, XxlJobInfo jobInfo) {
		logger.info(jobInfo.toString());
		logger.info(request.toString());
		logger.info(request.getHeaderNames().toString());
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			String headerValue = request.getHeader(headerName);
			logger.info(headerName + ": " + headerValue);
		}
		if(!request.getHeader(openvpnAccessTokenKey).equals(openvpnAccessTokenValue)) {
			logger.error("[XXL-JOB-ADMIN: addAndStart @-1] OpenVPN access token is wrong!");
			return new ReturnT<String>(ReturnT.FAIL_CODE, "OpenVPN access token is wrong!");
		}
		return xxlJobService.addAndStart(jobInfo);
	}

	@RequestMapping("/remove-job-openvpn")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> removeOpenVPNJob(HttpServletRequest request, int id) {
		if(!request.getHeader(openvpnAccessTokenKey).equals(openvpnAccessTokenValue)) {
			logger.error("[XXL-JOB-ADMIN: remove @-1] OpenVPN access token is wrong!");
			return new ReturnT<String>(ReturnT.FAIL_CODE, "OpenVPN access token is wrong!");
		}
		return xxlJobService.remove(id);
	}

	@RequestMapping("/update-and-start")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> updateAndStart(HttpServletRequest request, XxlJobInfo jobInfo) {
		if (!request.getHeader(emailyAccessTokenKey).equals(emailyAccessTokenValue)) {
			logger.error("[XXL-JOB-ADMIN: updateAndStart @-1] Emaily access token is wrong!");
			return new ReturnT<String>(ReturnT.FAIL_CODE, "Emaily access token is wrong!");
		}
		return xxlJobService.updateAndStart(jobInfo);
	}


	@RequestMapping("/update-and-start-openvpn")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> updateAndStartOpenVPNJob(HttpServletRequest request, XxlJobInfo jobInfo) {
		if(!request.getHeader(openvpnAccessTokenKey).equals(openvpnAccessTokenValue)) {
			logger.error("[XXL-JOB-ADMIN: updateAndStart @-1] OpenVPN access token is wrong!");
			return new ReturnT<String>(ReturnT.FAIL_CODE, "OpenVPN access token is wrong!");
		}
		return xxlJobService.updateAndStart(jobInfo);
	}


	@RequestMapping("/cancel-jobs")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<List<Integer>> cancelJobs(HttpServletRequest request, @RequestBody Map<String, String> jobMap) {
		logger.info("[XXL-JOB-ADMIN: cancelJobs @-1] cancel jobs: {}", jobMap);
		if (!request.getHeader(emailyAccessTokenKey).equals(emailyAccessTokenValue)) {
			logger.error("[XXL-JOB-ADMIN: cancelJobs @-1] Emaily access token is wrong!");
			return new ReturnT<List<Integer>>(ReturnT.FAIL_CODE, "Emaily access token is wrong!");
		}
		Set<String> failedCampaigns = new HashSet<>();
		List<Integer> successfulJobIds = new ArrayList<>();
		for (String jobId : jobMap.keySet()) {
			if (xxlJobService.stop(Integer.parseInt(jobId)).getCode() == ReturnT.SUCCESS_CODE){
				successfulJobIds.add(Integer.parseInt(jobId));
			}else {
				failedCampaigns.add(jobMap.get(jobId));
			}
		}

		if (failedCampaigns.isEmpty()){
			return new ReturnT<List<Integer>>(successfulJobIds);
		}
		ReturnT<List<Integer>> returnT = new ReturnT<>(ReturnT.FAIL_CODE, "Failed Cancel Campaigns: " + String.join(",", failedCampaigns));
		returnT.setContent(successfulJobIds);
		return returnT;
	}

	@RequestMapping("/start-jobs")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<List<Integer>> startJobs(HttpServletRequest request, @RequestBody Map<Integer, String> jobMap) {
		logger.info("[XXL-JOB-ADMIN: startJobs @-1] start jobs: {}", jobMap);
		if (!request.getHeader(emailyAccessTokenKey).equals(emailyAccessTokenValue)) {
			logger.error("[XXL-JOB-ADMIN: startJobs @-1] Emaily access token is wrong!");
			return new ReturnT<List<Integer>>(ReturnT.FAIL_CODE, "Emaily access token is wrong!");
		}
		Set<String> failedCampaigns = new HashSet<>();
		List<Integer> successfulJobIds = new ArrayList<>();
		for (Integer jobId : jobMap.keySet()) {
			if (xxlJobService.start(jobId).getCode() == ReturnT.SUCCESS_CODE){
				successfulJobIds.add(jobId);
			}else {
				failedCampaigns.add(jobMap.get(jobId));
			}
		}

		if (failedCampaigns.isEmpty()){
			return new ReturnT<>(successfulJobIds);
		}
		ReturnT<List<Integer>> returnT = new ReturnT<>(ReturnT.FAIL_CODE, "Failed Start Campaigns: " + String.join(",", failedCampaigns));
		returnT.setContent(successfulJobIds);
		return returnT;
	}

	@RequestMapping("/add-jobs")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<Map<String, Integer>> addJobs(HttpServletRequest request, @RequestBody List<XxlExtendJobInfo> jobInfoList) {
		if (!request.getHeader(apnAccessTokenKey).equals(apnAccessTokenValue)) {
			logger.error("[XXL-JOB-ADMIN: addJobs @-1] apn access token is wrong!");
			return new ReturnT<>(ReturnT.FAIL_CODE, "apn access token is wrong!");
		}
		Map<String, Integer> successfulJobIds = new HashMap<>(16);
		jobInfoList.forEach(jobInfo -> {
			ReturnT<String> jobReturn = xxlJobService.add(jobInfo);
			if (jobReturn.getCode() == ReturnT.SUCCESS_CODE){
				successfulJobIds.put(jobInfo.getOnlyId(), Integer.parseInt(jobReturn.getContent()));
			}
		});
		return new ReturnT<>(successfulJobIds);
	}

	@RequestMapping("/delete-jobs")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> remove(HttpServletRequest request, @RequestBody List<Integer> ids) {
		logger.info("[XXL-JOB-ADMIN: delete jobs @-1] delete jobs: {}", ids);
		if (!request.getHeader(apnAccessTokenKey).equals(apnAccessTokenValue)) {
			logger.error("[XXL-JOB-ADMIN: addJobs @-1] apn access token is wrong!");
			return new ReturnT<>(ReturnT.FAIL_CODE, "apn access token is wrong!");
		}
		return xxlJobService.deleteJobs(ids);
	}

	@PostMapping("/update-jobs-by-sendTime")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> updateJobsBySendTime(HttpServletRequest request, @RequestBody List<XxlJobUpdateBySendTime> xxlJobUpdateBySendTime) {
		logger.info("[XXL-JOB-ADMIN: update jobs by sendTime @-1] param : {}", xxlJobUpdateBySendTime);
		if (!request.getHeader(apnAccessTokenKey).equals(apnAccessTokenValue)) {
			logger.error("[XXL-JOB-ADMIN: addJobs @-1] apn access token is wrong!");
			return new ReturnT<>(ReturnT.FAIL_CODE, "apn access token is wrong!");
		}
		return xxlJobService.updateJobsBySendTime(xxlJobUpdateBySendTime);
	}

	@RequestMapping("/my-add")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> myAdd(HttpServletRequest request, XxlJobInfo jobInfo) {
		logger.info("[XXL-JOB-ADMIN: add job @-1] param : {}", jobInfo);
		if (!request.getHeader(apnAccessTokenKey).equals(apnAccessTokenValue)) {
			logger.error("[XXL-JOB-ADMIN: myAdd @-1] apn access token is wrong!");
			return new ReturnT<>(ReturnT.FAIL_CODE, "apn access token is wrong!");
		}
		return xxlJobService.add(jobInfo);
	}

	@RequestMapping("/my-update")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> myUpdate(HttpServletRequest request, XxlJobInfo jobInfo) {
		logger.info("[XXL-JOB-ADMIN: update job @-1] param : {}", jobInfo);
		if (!request.getHeader(apnAccessTokenKey).equals(apnAccessTokenValue)) {
			logger.error("[XXL-JOB-ADMIN: myUpdate @-1] apn access token is wrong!");
			return new ReturnT<>(ReturnT.FAIL_CODE, "apn access token is wrong!");
		}
		return xxlJobService.update(jobInfo);
	}

	@RequestMapping("/my-remove")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> myRemove(HttpServletRequest request, int id) {
		logger.info("[XXL-JOB-ADMIN: remove job @-1] id : {}", id);
		if (!request.getHeader(apnAccessTokenKey).equals(apnAccessTokenValue)) {
			logger.error("[XXL-JOB-ADMIN: myRemove @-1] apn access token is wrong!");
			return new ReturnT<>(ReturnT.FAIL_CODE, "apn access token is wrong!");
		}
		return xxlJobService.remove(id);
	}

}
