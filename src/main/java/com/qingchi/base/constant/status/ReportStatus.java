package com.qingchi.base.constant.status;

import java.util.Arrays;
import java.util.List;

/**
 * 会话聊天的状态，暂不使用，全局使用一样的，全局统一，避免出现文字不一致问题
 */
public class ReportStatus {
    //预审核，为什么要有预审核？举报的
    public static final String preAudit = ConstStatus.preAudit;

    //待审核
    public static final String audit = ConstStatus.audit;

    //违规
    public static final String violation = ConstStatus.violation;

    //不违规
    public static final String noViolation = ConstStatus.noViolation;

    public static final List<String> auditStatus = Arrays.asList(ContentStatus.preAudit, ContentStatus.audit);
}





