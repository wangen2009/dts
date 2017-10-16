package io.dts.resourcemanager.network.processor;

import io.dts.common.protocol.ResultCode;
import io.dts.common.protocol.header.BranchCommitMessage;
import io.dts.common.protocol.header.BranchCommitResultMessage;
import io.dts.common.protocol.header.BranchRollBackMessage;
import io.dts.common.protocol.header.BranchRollbackResultMessage;
import io.dts.resourcemanager.handler.IBranchTransProcessHandler;


public class RmMessageListener {

  private IBranchTransProcessHandler branchTransProcessHandler;

  public RmMessageListener(final IBranchTransProcessHandler branchTransProcessHandler) {
    this.branchTransProcessHandler = branchTransProcessHandler;
  }

  public void handleMessage(final String serverAddressIp, final BranchCommitMessage commitMessage,
      final BranchCommitResultMessage resultMessage) {
    Long branchId = commitMessage.getBranchId();
    Long tranId = commitMessage.getTranId();
    String servAddr = commitMessage.getServerAddr();
    String dbName = commitMessage.getDbName();
    String udata = commitMessage.getUdata();
    int commitMode = commitMessage.getCommitMode();
    String retrySql = commitMessage.getRetrySql();

    resultMessage.setBranchId(branchId);
    resultMessage.setTranId(tranId);
    try {
      branchTransProcessHandler.branchCommit(servAddr + ":" + tranId, branchId, dbName, udata, commitMode, retrySql);
      resultMessage.setResult(ResultCode.OK.getValue());
    } catch (Exception e) {
      resultMessage.setResult(ResultCode.SYSTEMERROR.getValue());
    }
  }

  public void handleMessage(final String serverAddressIP, final BranchRollBackMessage rollBackMessage,
      final BranchRollbackResultMessage resultMessage) {
    Long branchId = rollBackMessage.getBranchId();
    Long tranId = rollBackMessage.getTranId();
    String servAddr = rollBackMessage.getServerAddr();
    String dbName = rollBackMessage.getDbName();
    String udata = rollBackMessage.getUdata();
    int commitMode = rollBackMessage.getCommitMode();

    resultMessage.setBranchId(branchId);
    resultMessage.setTranId(tranId);
    try {
      branchTransProcessHandler.branchRollback(servAddr + ":" + tranId, branchId, dbName, udata, commitMode);
      resultMessage.setResult(ResultCode.OK.getValue());
    } catch (Exception e) {
      resultMessage.setResult(ResultCode.SYSTEMERROR.getValue());
    }
  }



}