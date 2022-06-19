package com.kuing.netty.reactor.masterSlave;

import com.kuing.netty.reactor.Acceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MultReactorServer {
    //对应角色为 reactor 反应器 通知器 监视器
    private Selector mainSelector;
    private Selector slaveSelector;
    private ServerSocketChannel serverChannel;

    public MultReactorServer() {
        try {
            mainSelector = Selector.open();
            slaveSelector = Selector.open();

            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);

            SocketAddress address = new InetSocketAddress(8899);
            serverChannel.socket().bind(address);

            //注册连接事件的同时 声明一个acceptor 和事件绑定
            SelectionKey key = serverChannel.register(mainSelector, SelectionKey.OP_ACCEPT);
            //这里给的是从选择器，将进来的客户端与从选择器绑定，主选择器只监听连接事件
            Acceptor acceptor = new Acceptor(serverChannel, slaveSelector);
            key.attach(acceptor);

            new Thread(new HandlerLoop(slaveSelector)).start();


            while (true) {
                int num = mainSelector.select();
                if (num == 0) continue;
                Set<SelectionKey> selectionKeys = mainSelector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();

                    //拿到之前存储的附加对象
                    //如果是接受事件 分发给绑定的acceptor
                    //如果是读写事件 分发给绑定的handler
                    Runnable runnable = (Runnable) selectionKey.attachment();
                    runnable.run();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
