package com.kuing.netty.reactor.masterSlave;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class HandlerLoop implements Runnable {
    private Selector selector;

    public HandlerLoop(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                int select = selector.select();
                if (select == 0) continue;
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    //读写事件 分发给绑定的handler
                    Runnable runnable = (Runnable) key.attachment();
                    runnable.run();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}

