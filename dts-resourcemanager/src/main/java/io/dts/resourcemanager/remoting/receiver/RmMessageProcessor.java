package io.dts.resourcemanager.remoting.receiver;

import io.dts.common.protocol.RequestCode;
import io.dts.common.protocol.RequestMessage;
import io.dts.common.protocol.ResponseCode;
import io.dts.common.protocol.header.BeginResultMessage;
import io.dts.common.protocol.header.BranchCommitMessage;
import io.dts.common.protocol.header.BranchCommitResultMessage;
import io.dts.common.protocol.header.BranchRollBackMessage;
import io.dts.common.protocol.header.BranchRollbackResultMessage;
import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.netty.NettyRequestProcessor;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.remoting.protocol.RemotingSerializable;
import io.dts.util.NetUtil;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by guoyubo on 2017/9/15.
 */
public class RmMessageProcessor implements NettyRequestProcessor {

  @Override
  public RemotingCommand processRequest(final ChannelHandlerContext ctx, final RemotingCommand request)
      throws Exception {
    final String clientIp = NetUtil.toStringAddress(ctx.channel().remoteAddress());
    switch (request.getCode()) {
      case RequestCode.HEADER_REQUEST:
        final RequestMessage headerMessage =
            (RequestMessage) request.decodeCommandCustomHeader(CommandCustomHeader.class);
        return processDtsMessage(clientIp, headerMessage);
      case RequestCode.BODY_REQUEST:
        final byte[] body = request.getBody();
        RequestMessage bodyMessage = RemotingSerializable.decode(body, RequestMessage.class);
        return processDtsMessage(clientIp, bodyMessage);
      default:
        break;
    }
    final RemotingCommand response = RemotingCommand
        .createResponseCommand(ResponseCode.REQUEST_CODE_NOT_SUPPORTED, "No request Code");
    return response;
  }



  private RemotingCommand processDtsMessage(String clientIp, RequestMessage dtsMessage) {
    RemotingCommand response = RemotingCommand.createResponseCommand(null);
    CommandCustomHeader responseHeader;
    try {
      if (dtsMessage instanceof BranchCommitMessage) {
        // 提交分支事务
        response = RemotingCommand.createResponseCommand(BeginResultMessage.class);
        responseHeader = response.readCustomHeader();
        createMessageHandler().handleMessage(clientIp, (BranchCommitMessage) dtsMessage,
            (BranchCommitResultMessage) responseHeader);
        response.setCode(ResponseCode.SUCCESS);
        return response;
      } else  if (dtsMessage instanceof BranchRollBackMessage) {
        // 回滚分支事务
        response = RemotingCommand.createResponseCommand(BranchRollbackResultMessage.class);
        responseHeader = response.readCustomHeader();
        createMessageHandler().handleMessage(clientIp, (BranchRollBackMessage) dtsMessage,
            (BranchRollbackResultMessage) responseHeader);
        response.setCode(ResponseCode.SUCCESS);
        return response;
      }
    } catch (Throwable e) {
      response.setCode(ResponseCode.SYSTEM_ERROR);
      response.setRemark(e.getMessage());
      return response;
    }
    response.setCode(ResponseCode.REQUEST_CODE_NOT_SUPPORTED);
    response.setRemark("not found request message proccessor");
    return response;
  }

  private DtsRmMessageHandler createMessageHandler() {
    return new DtsRmMessageHandler();
  }
}
