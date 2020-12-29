package com.xander.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

/**
 * Description: 创建节点并授权
 *
 * @author Xander
 * datetime: 2020-12-28 11:02
 */
public class ZkAuth {

    public static void main(String[] args) throws Exception {
        // 创建一个最基本的ZooKeeper对象实例
        ZooKeeper zookeeper = new ZooKeeper("k8smaster:2181",//zookeeper服务器
                5000, //会话超时时间
                null);
        String path = "/zkAuth";
        zookeeper.addAuthInfo("digest", "foo:true".getBytes());
        zookeeper.create( path, "init".getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.EPHEMERAL );
        Thread.sleep( Integer.MAX_VALUE );
    }
}
