package com.yy.lock;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * zookeeper锁实现
 * Created by luyuanyuan on 2017/9/22.
 */
public class ZookeeperLock implements Lock {

    //private static final String ZK_IP_PORT = "139.224.119.167:2181";
    private static final String ZK_IP_PORT = "192.168.0.89:2181";
    private static final String LOCK_NODE = "/LOCK";

    private  ZkClient zkClient = new ZkClient(ZK_IP_PORT);

    private CountDownLatch cdl = null;

    private String beforePath;//当前请求的节点前一个节点

    private String currentPath;//当前请求的节点

    public ZookeeperLock(){
        if(!zkClient.exists(LOCK_NODE)){
            this.zkClient.createPersistent(LOCK_NODE);
        }
    }
    //阻塞对的方式去获取锁
    @Override
    public void lock() {
        if(tryLock()){
            System.out.println("获取锁成功");
        }else{
            waitForLock();
            lock();
        }

    }

    private void waitForLock() {
        //1.创建一个监听
        IZkDataListener listener = new IZkDataListener() {
            @Override
            public void handleDataChange(String s, Object o) throws Exception {

            }

            @Override
            public void handleDataDeleted(String s) throws Exception {
                //3.当其他线程放锁，抛出事件，让其他线程重新竞争锁
                System.out.println("捕捉到节点删除事件");
                if (cdl != null) {
                    cdl.countDown();
                }
            }
        };
        zkClient.subscribeDataChanges(beforePath, listener);

        //2.如果节点存在，让线程阻塞
        if(zkClient.exists(beforePath)){
            cdl = new CountDownLatch(1);
            try {
                cdl.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        zkClient.unsubscribeDataChanges(beforePath,listener);
    }

    //通过新建节点的方式尝试去加锁，非阻塞
    @Override
    public boolean tryLock() {

        //如果currentPath为空则为第一次尝试加锁，第一次加锁赋值如果currentPath为空则为第一次尝试加锁
        if(currentPath == null || currentPath.length() <= 0){
            //创建一个临时顺序节点
            currentPath = this.zkClient.createEphemeralSequential(LOCK_NODE + '/',"lock");
        }

        //获取所有临时节点并排序，临时节点名称为自增长的字符串：000000400
        List<String> childrens = this.zkClient.getChildren(LOCK_NODE);
        Collections.sort(childrens);

        if(currentPath.equals(LOCK_NODE + '/' + childrens.get(0))){
            return true;
        }else{
            //如果当前节点在所有节点中排名不是第一名，则获取前面的节点名称，并赋值给beforePath
            int wz = Collections.binarySearch(childrens,currentPath.substring(6));
            beforePath = LOCK_NODE + '/' + childrens.get(wz - 1);
        }
       /* try {
            zkClient.createPersistent(LOCK_NODE);
            return true;
        } catch (ZkNodeExistsException e) {
            return false;
        }*/
       return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        zkClient.delete(currentPath);
    }


    @Override
    public Condition newCondition() {
        return null;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }
}
