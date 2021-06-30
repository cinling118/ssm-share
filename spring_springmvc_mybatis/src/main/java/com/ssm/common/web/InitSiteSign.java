package com.ssm.common.web;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.ssm.common.service.impl.InitSiteService;

public class InitSiteSign implements InitializingBean{
	@Autowired
	private InitSiteService initSiteService;
	
	private boolean init;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (init) {
			initSiteService.initSite();
		}
	}

	public boolean isInit() {
		return init;
	}

	public void setInit(boolean init) {
		this.init = init;
	}

}
