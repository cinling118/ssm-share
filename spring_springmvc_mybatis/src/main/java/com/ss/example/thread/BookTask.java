package com.ss.example.thread;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.ssm.example.entity.Book;
import com.ssm.example.service.BookService;

/**
 * @description:
 * @author:zhiyun.chen
 * 2021年7月10日 下午4:29:49
 */
/**
 * 多线程处理任务类
 */
public class BookTask extends ThreadTask {

    /**
       分批处理的数据
     */
    private List<Book> objectList;

    /**
     * 可能需要注入的某些服务
     */
    private BookService bookService;

    public BookTask(CountDownLatch childCountDown, CountDownLatch mainCountDown, BlockingDeque<Boolean> result, RollBack rollback, DataSourceTransactionManager transactionManager, Object obj, Map<String, Object> params) {
        super(childCountDown, mainCountDown, result, rollback, transactionManager, obj, params);
    }

    @Override
    public void initParam() {
        this.objectList = (List<Book>) getParam("objectList");
        this.bookService = (BookService) getParam("bookService");
    }


    /**
     * 执行任务,返回false表示任务执行错误，需要回滚
     * @return
     */
    @Override
    public boolean processTask() {
        try {
        	List<Book> dataList = (List<Book>)obj;
//        	if (dataList.get(0).getBookId() > 1000) {
//        		System.out.println(dataList.get(0).getBookId()/0);
//        	}
        	bookService.batchInsert(dataList);
        	System.out.println(Thread.currentThread().getName()+":执行自己的多线程任务逻辑");
//            for (Object o : objectList) {
//            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
