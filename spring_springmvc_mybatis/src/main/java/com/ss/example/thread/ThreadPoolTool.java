package com.ss.example.thread;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * @description:线程池
 */
public class ThreadPoolTool<T> {
	private Logger log = LoggerFactory.getLogger(this.getClass());
    /**
     * 多线程任务
     * @param transactionManager 数据库事务管理
     * @param data 需要执行的数据集合
     * @param threadCount 核心线程数
     * @param params 其他资源参数
     * @param clazz 具体执行任务的类
     */
    public void excuteTask(DataSourceTransactionManager transactionManager, List data, int threadCount, Map<String, Object> params, Class clazz) {
        if (data == null || data.size() == 0) {
            return;
        }
        int batch = 0;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        //监控子线程的任务执行
        CountDownLatch childMonitor = new CountDownLatch(threadCount);
        //监控主线程，是否需要回滚
        CountDownLatch mainMonitor = new CountDownLatch(1);
        //存储任务的返回结果，返回true表示不需要回滚，反之，则回滚
        BlockingDeque<Boolean> results = new LinkedBlockingDeque<Boolean>(threadCount);
        RollBack rollback = new RollBack(false);

        try {
            LinkedBlockingQueue<List<Object>> queue = splitQueue(data, threadCount);
            while (true) {
                List list = queue.poll();
                if (list == null) {
                    break;
                }
                System.out.println(list.size());
                batch++;
                params.put("batch", batch);
                Constructor constructor = clazz.getConstructor(new Class[]{CountDownLatch.class, CountDownLatch.class, BlockingDeque.class, RollBack.class, DataSourceTransactionManager.class, Object.class, Map.class});

                ThreadTask task = (ThreadTask) constructor.newInstance(childMonitor, mainMonitor, results, rollback, transactionManager, list, params);
                executor.execute(task);
            }

            //   1、主线程将任务分发给子线程，然后使用childMonitor.await();阻塞主线程，等待所有子线程处理向数据库中插入的业务。
            childMonitor.await();
            System.out.println("主线程开始执行任务");

            //根据返回结果来确定是否回滚
            for (int i = 0; i < threadCount; i++) {
                Boolean result = results.take();
                if (!result) {
                    //有线程执行异常，需要回滚子线程
                    rollback.setRollBack(true);
                }
            }
            //  3、主线程检查子线程执行任务的结果，若有失败结果出现，主线程标记状态告知子线程回滚，然后使用mainMonitor.countDown();将程序控制权再次交给子线程，子线程检测回滚标志，判断是否回滚。
            mainMonitor.countDown();

        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            //关闭线程池，释放资源
            executor.shutdown();
        }

    }

    /**
     * 队列拆分
     *
     * @param data 需要执行的数据集合
     * @param threadCount 核心线程数
     * @return
     */
    private LinkedBlockingQueue<List<Object>> splitQueue(List<Object> data, int threadCount) {
        LinkedBlockingQueue<List<Object>> queueBatch = new LinkedBlockingQueue();
        int total = data.size();
        int oneSize = total / threadCount;
        int start;
        int end;

        for (int i = 0; i < threadCount; i++) {
            start = i * oneSize;
            end = (i + 1) * oneSize;
            if (i < threadCount - 1) {
                queueBatch.add(data.subList(start, end));
            } else {
                queueBatch.add(data.subList(start, data.size()));
            }
        }
        return queueBatch;
    }
}
