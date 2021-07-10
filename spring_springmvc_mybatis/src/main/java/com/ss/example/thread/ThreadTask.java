package com.ss.example.thread;

import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * @description:线程
 */
public abstract class ThreadTask implements Runnable {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	/**
     * 监控子任务的执行
     */
    private CountDownLatch childMonitor;
    /**
     * 监控主线程
     */
    private CountDownLatch mainMonitor;
    /**
     * 存储线程的返回结果
     */
    private BlockingDeque<Boolean> resultList;
    /**
     * 回滚类
     */
    private RollBack rollback;


    private Map<String,Object> params;

    protected Object obj;
    protected DataSourceTransactionManager transactionManager;
    protected TransactionStatus status;

    public ThreadTask(CountDownLatch childCountDown, CountDownLatch mainCountDown, BlockingDeque<Boolean> result, RollBack rollback, DataSourceTransactionManager transactionManager, Object obj,Map<String,Object> params) {
        this.childMonitor = childCountDown;
        this.mainMonitor = mainCountDown;
        this.resultList = result;
        this.rollback = rollback;
        this.transactionManager = transactionManager;
        this.obj = obj;
        this.params = params;
        initParam();
    }

    /**
     * 事务回滚
     */
    private void rollBack() {
        System.out.println(Thread.currentThread().getName()+"开始回滚");
        transactionManager.rollback(status);
    }

    /**
     * 事务提交
     */
    private void submit() {
        System.out.println(Thread.currentThread().getName()+"提交事务");
        transactionManager.commit(status);
    }

    protected Object getParam(String key){
        return params.get(key);
    }
    /**      * 初始化方法：作用是把线程池工具任务执行类所需的外部资源通过 ThreadTask.class的构造方法中 Map<String,Obejct> params参数进行初始化传递进来      */
    public abstract void initParam();

    /**
     * 执行任务,返回false表示任务执行错误，需要回滚
     * @return
     */
    public abstract boolean processTask();

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()+"子线程开始执行任务");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        status = transactionManager.getTransaction(def);

        Boolean result = processTask();
        //向队列中添加处理结果
        resultList.add(result);
        //2、使用childMonitor.countDown()释放子线程锁定，同时使用mainMonitor.await();阻塞子线程，将程序的控制权交还给主线程。
        childMonitor.countDown();
        try {
            //等待主线程的判断逻辑执行完，执行下面的是否回滚逻辑
            mainMonitor.await();
        } catch (Exception e) {
           log.error(e.getMessage());
        }

        System.out.println(Thread.currentThread().getName()+"子线程执行剩下的任务");
        //3、主线程检查子线程执行任务的结果，若有失败结果出现，主线程标记状态告知子线程回滚，然后使用mainMonitor.countDown();将程序控制权再次交给子线程，子线程检测回滚标志，判断是否回滚。
        if (rollback.getRollBack()) {
            rollBack();
        }else{
            //事务提交
            submit();
        }
    }
}
