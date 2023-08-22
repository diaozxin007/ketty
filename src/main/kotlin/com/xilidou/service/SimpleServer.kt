package com.xilidou.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel


fun main(){
    SimpleServer().run()
}

class SimpleServer {
    fun run() {
        val serverSocketChannel = ServerSocketChannel.open()
        serverSocketChannel.configureBlocking(false)
        val selector = Selector.open()
        val selectionKey = serverSocketChannel.register(selector, 0, serverSocketChannel)
        selectionKey.interestOps(SelectionKey.OP_ACCEPT)
        serverSocketChannel.bind(InetSocketAddress(8080))
        val nioLoop = NioEventLoop()
        while (true) {
            logger.info("main函数阻塞在这里吧。。。。。。。")
            selector.select()
            val selectedKeys = selector.selectedKeys()
            val keyIterator = selectedKeys.iterator()
            while (keyIterator.hasNext()) {
                val key = keyIterator.next()
                keyIterator.remove()
                if (key.isAcceptable) {
                    val channel = key.channel() as ServerSocketChannel
                    val socketChannel = channel.accept()
                    socketChannel.configureBlocking(false)
                    nioLoop.register(socketChannel,nioLoop)
                    //给客户端的channel设置可读事件
                    logger.info("客户端在main函数中连接成功！")
                    //连接成功之后，用客户端的channel写回一条消息
                    socketChannel.write(ByteBuffer.wrap("我发送成功了".toByteArray()))
                    logger.info("main函数服务器向客户端发送数据成功！")
                }
            }
        }
    }



    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Work::class.java)
    }

}