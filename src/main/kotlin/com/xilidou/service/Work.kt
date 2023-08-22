package com.xilidou.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.nio.channels.spi.SelectorProvider
import kotlin.concurrent.Volatile


class Work(): Runnable {
    @Volatile
    private var start = false

    private lateinit var provider:SelectorProvider

    private var selector: Selector = Selector.open()
    private var thread: Thread = Thread(this)
    private val selectedKey: SelectionKey? = null
    private lateinit var socketChannel:SocketChannel

    fun Work() {
        provider = SelectorProvider.provider()
        selector = openSelector()
        thread = Thread(this)
    }

    fun register(socketChannel:SocketChannel){

        try{
            this.socketChannel = socketChannel;
            socketChannel.configureBlocking(false)
            socketChannel.register(selector,SelectionKey.OP_READ)
            start()
        }catch (e:IOException){
            logger.error("error",e)
        }

    }

    private fun openSelector():Selector{
        val openSelector = provider.openSelector()
        return openSelector;
    }



    fun getSelector(): Selector {
        return selector
    }


    fun start() {
        if (start) {
            return
        }
        start = true
        thread.start()
    }

    override fun run() {
        logger.info("新线程阻塞在这里吧。。。。。。。")
        try {
            selector.select()
            val iterator: MutableIterator<SelectionKey> = selector.selectedKeys().iterator()
            while (iterator.hasNext()) {
                val selectionKey = iterator.next()
                iterator.remove()
                if (selectionKey.isReadable) {
                    val channel = selectionKey.channel() as SocketChannel
                    val byteBuffer: ByteBuffer = ByteBuffer.allocate(1024)
                    val len: Int = channel.read(byteBuffer)
                    if (len == -1) {
                        logger.info("客户端通道要关闭！")
                        channel.close()
                        break
                    }
                    val bytes = ByteArray(len)
                    byteBuffer.flip()
                    byteBuffer.get(bytes)
                    logger.info("收到客户端发送的数据:{}", String(bytes))
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Work::class.java)
    }
}
