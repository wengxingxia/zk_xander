package com.xander.zk;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

/**
 * Description: 异步创建节点
 *
 * @author Xander
 * datetime: 2020-12-28 11:02
 */
public class ZkCreateASync implements Watcher {

    //倒数门闩，计数为1
    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        // 创建一个最基本的ZooKeeper对象实例
        ZooKeeper zookeeper = new ZooKeeper("k8smaster:2181",//zookeeper服务器
                5000, //会话超时时间
                new ZkCreateASync());// 注册Watcher
        System.out.println("当前zookeeper状态：" + zookeeper.getState());
        //阻塞，直到countdown计数等于0
        countDownLatch.await();
        System.out.println("zookeeper 会话建立");

        zookeeper.create("/zk-test-ephemeral-", "".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL,
                new IStringCallback(), "I am context.");

        zookeeper.create("/zk-test-ephemeral-", "".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL,
                new IStringCallback(), "I am context.");

        zookeeper.create("/zk-test-ephemeral-", "".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL,
                new IStringCallback(), "I am context.");
        Thread.sleep( Integer.MAX_VALUE );
    }

    public void process(WatchedEvent event) {
        System.out.println("接收到监听事件：" + event);
        if (Event.KeeperState.SyncConnected == event.getState()) {
            // 倒数，计数-1
            countDownLatch.countDown();
        }
    }
}

/**
 * 回调
 */
class IStringCallback implements AsyncCallback.StringCallback {
    /**
     * 创建结果回调
     *
     * @param rc   服务端响应码 ，
     *             0 (Ok):接口调用成功；
     *             -4 (ConnectionLoss) : 客户端和服务端连接已断开；
     *             -110 (NodeExists) : 指定节点已存在
     *             112 (SessionExpired) : 会话已过期
     * @param path 接口调用时传入API的数据节点的节点路径参数值
     * @param ctx  接口调用时传入API的Ctx参数值
     * @param name 实际路径，如果不是顺序节点则 path和name一样
     */
    public void processResult(int rc, String path, Object ctx, String name) {
        System.out.println("Create path result: [" + rc + ", " + path + ", "
                + ctx + ", real path name: " + name);
    }
}
