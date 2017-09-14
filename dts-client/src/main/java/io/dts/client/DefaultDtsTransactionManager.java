package io.dts.client;

import io.dts.client.exception.DtsTransactionException;
import io.dts.client.transport.DtsClientMessageSenderImpl;
import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.common.ResultCode;
import io.dts.common.common.TxcXID;
import io.dts.common.context.DtsContext;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.RequestCode;
import io.dts.common.protocol.header.BeginMessage;
import io.dts.common.protocol.header.BeginResultMessage;
import io.dts.common.protocol.header.GlobalCommitMessage;
import io.dts.common.protocol.header.GlobalCommitResultMessage;
import io.dts.common.protocol.header.GlobalRollbackMessage;
import io.dts.common.protocol.header.GlobalRollbackResultMessage;
import io.dts.remoting.netty.NettyClientConfig;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.remoting.protocol.RemotingSerializable;

/**
 * Created by guoyubo on 2017/8/24.
 */
public class DefaultDtsTransactionManager implements DtsTransactionManager {

  private DtsClientMessageSender dtsClient;

  public DefaultDtsTransactionManager(final DtsClientMessageSender dtsClient) {
    this.dtsClient = dtsClient;
  }

  @Override
  public void begin(final long timeout) throws DtsTransactionException {
    final BeginMessage beginMessage = new BeginMessage();
    beginMessage.setTimeout(timeout);
    RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.HEADER_REQUEST, beginMessage);
    try {
      RemotingCommand response = (RemotingCommand) dtsClient.invoke(request, timeout);
      if (response.getCode() == ResultCode.OK.getValue()) {
        BeginResultMessage beginResultMessage = RemotingSerializable.decode(response.getBody(), BeginResultMessage.class);
        System.out.println(beginResultMessage);
        DtsContext.bind(beginResultMessage.getXid(), beginResultMessage.getNextSvrAddr());
      }
      throw new DtsTransactionException("transaction begin fail");
    } catch (DtsException e) {
      throw new DtsTransactionException("transaction begin fail", e);
    }
  }

  @Override
  public void commit() throws DtsException {
    this.commit(0);
  }

  @Override
  public void commit(final int retryTimes) throws DtsTransactionException {
    GlobalCommitMessage commitMessage = new GlobalCommitMessage();
    commitMessage.setTranId(TxcXID.getTransactionId(DtsContext.getCurrentXid()));
    RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.HEADER_REQUEST, commitMessage);
    request.setBody(RemotingSerializable.encode(commitMessage));
    try {
      RemotingCommand response = (RemotingCommand) dtsClient.invoke(request, 3000l);
      if (response.getCode() == ResultCode.OK.getValue()) {
        GlobalCommitResultMessage commitResultMessage = RemotingSerializable.decode(response.getBody(), GlobalCommitResultMessage.class);
        System.out.println(commitResultMessage);
        DtsContext.unbind();
      }
      throw new DtsTransactionException("transaction commit fail");
    } catch (DtsException e) {
      throw new DtsTransactionException("transaction commit fail", e);
    }
  }

  @Override
  public void rollback() throws DtsException {
    this.rollback(0);
  }

  @Override
  public void rollback(final int retryTimes) throws DtsTransactionException {
    GlobalRollbackMessage rollbackMessage = new GlobalRollbackMessage();
    rollbackMessage.setTranId(TxcXID.getTransactionId(DtsContext.getCurrentXid()));
    rollbackMessage.setRealSvrAddr(TxcXID.getServerAddress(DtsContext.getCurrentXid()));

    RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.HEADER_REQUEST, rollbackMessage);
    request.setBody(RemotingSerializable.encode(rollbackMessage));
    try {
      RemotingCommand response = (RemotingCommand) dtsClient.invoke(request, 3000l);
      if (response.getCode() == ResultCode.OK.getValue()) {
        GlobalRollbackResultMessage rollbackResultMessage = RemotingSerializable.decode(response.getBody(), GlobalRollbackResultMessage.class);
        System.out.println(rollbackResultMessage);
        DtsContext.unbind();
      }
      throw new DtsTransactionException("transaction rollback fail");
    } catch (DtsException e) {
      throw new DtsTransactionException("transaction rollback fail", e);
    }
  }

  public static void main(String[] args) {
    NettyClientConfig nettyClientConfig = new NettyClientConfig();
    nettyClientConfig.setConnectTimeoutMillis(3000);
    DtsClientMessageSenderImpl dtsClient = new DtsClientMessageSenderImpl(nettyClientConfig);
//    dtsClient.setAddressManager(new ZookeeperAddressManager("localhost:2181", "/dts"));
//    dtsClient.setGroup("Default");
//    dtsClient.setAppName("Demo");
    dtsClient.init();
    try {
      DefaultDtsTransactionManager transactionManager = new DefaultDtsTransactionManager(dtsClient);
      transactionManager.begin(3000L);
      System.out.println(DtsContext.getCurrentXid());
    } catch (DtsTransactionException e) {
      e.printStackTrace();
    } finally {
      dtsClient.destroy();
    }
  }
}