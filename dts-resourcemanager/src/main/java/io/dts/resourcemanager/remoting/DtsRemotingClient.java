package io.dts.resourcemanager.remoting;

import com.google.common.collect.Queues;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.dts.common.ThreadFactoryImpl;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.RequestCode;
import io.dts.remoting.RemotingClient;
import io.dts.remoting.exception.RemotingConnectException;
import io.dts.remoting.exception.RemotingSendRequestException;
import io.dts.remoting.exception.RemotingTimeoutException;
import io.dts.remoting.netty.NettyClientConfig;
import io.dts.remoting.netty.NettyRemotingClient;
import io.dts.remoting.netty.NettyRequestProcessor;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.resourcemanager.remoting.receiver.RmMessageProcessor;

/**
 * Created by guoyubo on 2017/9/20.
 */
public class DtsRemotingClient {

  private RemotingClient remotingClient;

  public DtsRemotingClient(NettyClientConfig nettyClientConfig, final List<String> serverAddressList) {
    this.remotingClient = new NettyRemotingClient(nettyClientConfig);
    this.remotingClient.updateNameServerAddressList(serverAddressList);
    NettyRequestProcessor messageProcessor = new RmMessageProcessor();
    BlockingQueue<Runnable> clientThreadPoolQueue =
        Queues.newLinkedBlockingDeque(100);
    ExecutorService clientMessageExecutor =
        new ThreadPoolExecutor(nettyClientConfig.getClientCallbackExecutorThreads(),
            nettyClientConfig.getClientCallbackExecutorThreads(), 1000 * 60, TimeUnit.MILLISECONDS,
            clientThreadPoolQueue, new ThreadFactoryImpl("ResourceMessageThread_"));
    this.remotingClient.registerProcessor(RequestCode.HEADER_REQUEST, messageProcessor,
        clientMessageExecutor);
    this.remotingClient.registerProcessor(RequestCode.BODY_REQUEST, messageProcessor,
        clientMessageExecutor);
  }



  @PostConstruct
  public void start() {
    remotingClient.start();
  }

  @PreDestroy
  public void shutdown() {
    remotingClient.shutdown();
  }

  public RemotingCommand invokeSync(final String serverAddress, final RemotingCommand request, final long timeout){
    try {
      return remotingClient.invokeSync(serverAddress, request, timeout);
    } catch (InterruptedException e) {
      throw new DtsException(e, "internal error");
    } catch (RemotingConnectException e) {
      throw new DtsException(e, "connect remote server error");
    } catch (RemotingSendRequestException e) {
      throw new DtsException(e, "send request error");
    } catch (RemotingTimeoutException e) {
      throw new DtsException(e, "remote timeout");
    }
  }
}
