/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.quancheng.dts.message;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.quancheng.dts.message.request.BeginMessage;
import com.quancheng.dts.message.request.BeginRetryBranchMessage;
import com.quancheng.dts.message.request.BranchCommitMessage;
import com.quancheng.dts.message.request.BranchRollbackMessage;
import com.quancheng.dts.message.request.GlobalCommitMessage;
import com.quancheng.dts.message.request.GlobalRollbackMessage;
import com.quancheng.dts.message.request.QueryLockMessage;
import com.quancheng.dts.message.request.RegisterMessage;
import com.quancheng.dts.message.request.ReportStatusMessage;
import com.quancheng.dts.message.request.ReportUdataMessage;
import com.quancheng.dts.message.response.BeginResultMessage;
import com.quancheng.dts.message.response.BeginRetryBranchResultMessage;
import com.quancheng.dts.message.response.BranchCommitResultMessage;
import com.quancheng.dts.message.response.BranchRollbackResultMessage;
import com.quancheng.dts.message.response.GlobalCommitResultMessage;
import com.quancheng.dts.message.response.GlobalRollbackResultMessage;
import com.quancheng.dts.message.response.QueryLockResultMessage;
import com.quancheng.dts.message.response.RegisterResultMessage;
import com.quancheng.dts.message.response.ReportStatusResultMessage;
import com.quancheng.dts.message.response.ReportUdataResultMessage;
import com.quancheng.dts.message.response.ResultMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author liushiming
 * @version DtsMessage.java, v 0.0.1 2017年7月5日 下午5:07:15 liushiming
 * @since JDK 1.8
 */
public abstract class DtsMessage implements DtsMsgVistor, DtsCodec, Serializable {

  private static final long serialVersionUID = -1591010533701042512L;
  public static final Charset UTF8 = Charset.forName("utf-8");


  public static final short TYPE_BEGIN = 1;
  public static final short TYPE_BEGIN_RESULT = 2;

  public static final short TYPE_BRANCH_COMMIT = 3;
  public static final short TYPE_BRANCH_COMMIT_RESULT = 4;

  public static final short TYPE_BRANCH_ROLLBACK = 5;
  public static final short TYPE_BRANCH_ROLLBACK_RESULT = 6;

  public static final short TYPE_GLOBAL_COMMIT = 7;
  public static final short TYPE_GLOBAL_COMMIT_RESULT = 8;

  public static final short TYPE_GLOBAL_ROLLBACK = 9;
  public static final short TYPE_GLOBAL_ROLLBACK_RESULT = 10;

  public static final short TYPE_REGIST = 11;
  public static final short TYPE_REGIST_RESULT = 12;

  public static final short TYPE_REPORT_STATUS = 13;
  public static final short TYPE_REPORT_STATUS_RESULT = 14;

  public static final short TYPE_BEGIN_RETRY_BRANCH = 15;
  public static final short TYPE_BEGIN_RETRY_BRANCH_RESULT = 16;

  public static final short TYPE_REPORT_UDATA = 17;
  public static final short TYPE_REPORT_UDATA_RESULT = 18;

  public static final short TYPE_DTS_MERGE = 19;
  public static final short TYPE_DTS_MERGE_RESULT = 20;

  public static final short TYPE_QUERY_LOCK = 21;
  public static final short TYPE_QUERY_LOCK_RESULT = 22;



  protected static final Map<Short, String> typeMap = new HashMap<Short, String>();

  static {
    typeMap.put(TYPE_BEGIN, BeginMessage.class.getName());
    typeMap.put(TYPE_BEGIN_RESULT, BeginResultMessage.class.getName());
    typeMap.put(TYPE_BRANCH_COMMIT, BranchCommitMessage.class.getName());
    typeMap.put(TYPE_BRANCH_COMMIT_RESULT, BranchCommitResultMessage.class.getName());
    typeMap.put(TYPE_BRANCH_ROLLBACK, BranchRollbackMessage.class.getName());
    typeMap.put(TYPE_BRANCH_ROLLBACK_RESULT, BranchRollbackResultMessage.class.getName());
    typeMap.put(TYPE_GLOBAL_COMMIT, GlobalCommitMessage.class.getName());
    typeMap.put(TYPE_GLOBAL_COMMIT_RESULT, GlobalCommitResultMessage.class.getName());
    typeMap.put(TYPE_GLOBAL_ROLLBACK, GlobalRollbackMessage.class.getName());
    typeMap.put(TYPE_GLOBAL_ROLLBACK_RESULT, GlobalRollbackResultMessage.class.getName());
    typeMap.put(TYPE_REGIST, RegisterMessage.class.getName());
    typeMap.put(TYPE_REGIST_RESULT, RegisterResultMessage.class.getName());
    typeMap.put(TYPE_REPORT_STATUS, ReportStatusMessage.class.getName());
    typeMap.put(TYPE_REPORT_STATUS_RESULT, ReportStatusResultMessage.class.getName());
    typeMap.put(TYPE_BEGIN_RETRY_BRANCH, BeginRetryBranchMessage.class.getName());
    typeMap.put(TYPE_BEGIN_RETRY_BRANCH_RESULT, BeginRetryBranchResultMessage.class.getName());
    typeMap.put(TYPE_REPORT_UDATA, ReportUdataMessage.class.getName());
    typeMap.put(TYPE_REPORT_UDATA_RESULT, ReportUdataResultMessage.class.getName());
    typeMap.put(TYPE_DTS_MERGE, DtsMergeMessage.class.getName());
    typeMap.put(TYPE_DTS_MERGE_RESULT, DtsMergeResultMessage.class.getName());
    typeMap.put(TYPE_QUERY_LOCK, QueryLockMessage.class.getName());
    typeMap.put(TYPE_QUERY_LOCK_RESULT, QueryLockResultMessage.class.getName());


  }

  private ChannelHandlerContext ctx;

  private DtsMsgHandler handler;

  @Override
  public boolean decode(ByteBuf in) {
    return false;
  }

  @Override
  public void setChannelHandlerContext(ChannelHandlerContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public ChannelHandlerContext getChannelHandlerContext() {
    return this.ctx;
  }

  public DtsMsgHandler getHandler() {
    return handler;
  }

  public void setHandler(DtsMsgHandler handler) {
    this.handler = handler;
  }


  @Override
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      DtsMessage message, ResultMessage[] results, int idx) {
    // do nothing
  }

  protected void intToBytes(int i, byte[] bytes, int offset) {
    bytes[offset] = (byte) ((i >> 24) & 0xFF);
    bytes[offset + 1] = (byte) ((i >> 16) & 0xFF);
    bytes[offset + 2] = (byte) ((i >> 8) & 0xFF);
    bytes[offset + 3] = (byte) (i & 0xFF);
  }

  protected int bytesToInt(byte[] bytes, int offset) {
    int ret = 0;
    for (int i = 0; i < 4 && i + offset < bytes.length; i++) {
      ret <<= 8;
      ret |= (int) bytes[i + offset] & 0xFF;
    }
    return ret;
  }
}
