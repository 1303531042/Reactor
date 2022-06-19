package com.kuing.netty.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultHandler implements Runnable {

    private SelectionKey key;
    private State state;
    private ExecutorService pool;

    public MultHandler(SelectionKey key) {
        this.key = key;
        this.state = State.READ;
        pool = Executors.newFixedThreadPool(10);
    }

    @Override
    public void run() {
        switch (state) {
            case WRITE:
                pool.execute(() -> {
                    read();
                });
                break;
            case READ:
                pool.execute(() -> {
                    write();
                });
                break;
        }
    }

    private void read() {

        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            SocketChannel socketChannel = (SocketChannel) key.channel();
            int num = socketChannel.read(buffer);
            String msg = new String(buffer.array());

            key.interestOps(SelectionKey.OP_WRITE);
            this.state = State.WRITE;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write() {
        ByteBuffer buffer = ByteBuffer.wrap("hello".getBytes());
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            socketChannel.write(buffer);

            key.interestOps(SelectionKey.OP_READ);
            this.state = State.READ;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private enum State {
        READ, WRITE
    }
}
