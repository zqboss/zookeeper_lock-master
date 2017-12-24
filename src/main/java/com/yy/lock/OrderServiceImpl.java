package com.yy.lock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

/**
 * Created by luyuanyuan on 2017/9/22.
 */
public class OrderServiceImpl implements Runnable {

    //静态
    private static OrderCodeGenerator ocg = new OrderCodeGenerator();

    private static final int NUM = 50;

    private static CountDownLatch cdl = new CountDownLatch(NUM);

//    private static Lock lock = new ReentrantLock();

    //这个不能用static
    private  Lock lock = new ZookeeperLock();

    //创建订单
    private void createOrder(){
        lock.lock();
        try {
            String orderCode = ocg.getOrderCode();
            System.out.println(Thread.currentThread().getName()+"================>>>"+orderCode);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void run() {
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        createOrder();
    }

    public static void main(String[] args) {
        for (int i=0;i < NUM;i++){
            new Thread(new OrderServiceImpl()).start();
            cdl.countDown();
        }
    }
}
