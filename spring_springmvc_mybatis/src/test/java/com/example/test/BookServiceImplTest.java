package com.example.test;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.common.test.BaseTest;
import com.ss.example.thread.BookTask;
import com.ss.example.thread.ThreadPoolTool;
import com.ssm.example.dto.AppointExecution;
import com.ssm.example.entity.Book;
import com.ssm.example.service.BookService;

public class BookServiceImplTest extends BaseTest {

	@Autowired
	private BookService bookService;
	@Autowired
	private DataSourceTransactionManager transactionManager;

	@Test
	public void testAppoint() throws Exception {
		long bookId = 1001;
		long studentId = 12345678910L;
		AppointExecution execution = bookService.appoint(bookId, studentId);
		System.out.println(execution);
	}

	@Test
	public void test_batchInsert() throws Exception {
		bookService.batchInsert();
	}
	
	/**
	 * @description:多线程分批插入数据
	 * 2021年7月10日 下午5:50:08
	 */
	@Test
	public void test_bootTask(){
		try {
			ThreadPoolTool threadPoolTool = new ThreadPoolTool();
			int threadCount = 5;
			
			List<Book> bookList = new ArrayList<Book>();
			for (int i=2000;i<10000;i++) {
				Book book = new Book();
				book.setBookId(i);
				book.setName("name_" + i);
				book.setNumber(i);
				bookList.add(book);
			}
			// 需要分批处理的数据
			Map<String, Object> params = new HashMap<>();
			params.put("objectList", bookList);
			params.put("bookService", bookService);
			// 调用多线程工具方法
			long startTime = System.currentTimeMillis();
			threadPoolTool.excuteTask(transactionManager, bookList, threadCount, params, BookTask.class);
			long endTime = System.currentTimeMillis();
			System.out.println("总耗时(毫秒)：" + (endTime -startTime));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}

