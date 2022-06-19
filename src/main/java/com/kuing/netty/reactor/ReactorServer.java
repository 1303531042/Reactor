package com.kuing.netty.reactor;


import io.netty.channel.ServerChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ReactorServer {
    private Selector selector;
    private ServerSocketChannel serverChannel;

    public ReactorServer() {
        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();

            serverChannel.socket().bind(new InetSocketAddress(8888));

            //注册连接事件的同时 声明一个acceptor和事件绑定
            serverChannel.configureBlocking(false);
            SelectionKey selectionKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            //创建一个acceptor
            Acceptor acceptor = new Acceptor(serverChannel, selector);
            //acceptor 作为一个附加对象进行绑定
            selectionKey.attach(acceptor);

            while (true) {
                int num = selector.select();
                if (num == 0) {continue;}
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    //拿到之前存储的附加对象
                    //如果是接受事件 分发给绑定的acceptor
                    //如果是读写事件 分发给绑定的handler
                    Runnable runnable = (Runnable) key.attachment();
                    runnable.run();
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
