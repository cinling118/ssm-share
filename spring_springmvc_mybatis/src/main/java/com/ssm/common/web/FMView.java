package com.ssm.common.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

public class FMView extends FreeMarkerView{
	private static String styleVersion;//页面版本
	static {
		SimpleDateFormat df=new SimpleDateFormat("yyyyMMddHHmmss");
		styleVersion=df.format(new Date());
	}
	
	@Override
	protected void renderMergedTemplateModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		model.put("style_version", styleVersion);
		model.put("style_path", request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath());
		exposeHelpers(model, request);
		doRender(model, request, response);
	}
			
}
