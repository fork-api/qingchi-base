package com.qingchi.base.model.report;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.qingchi.base.model.BaseModelParentDO;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 举报信息
 */
@Entity
@Table(name = "report")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ReportDO implements BaseModelParentDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //被举报的用户
    private Integer receiveUserId;

    //加入一列作为外键
    private Long messageId;

    //加入一列作为外键
    private Integer commentId;

    //加入一列作为外键
    private Integer userImgId;

    //加入一列作为外键
    private Integer talkId;

    private Integer contentId;

    //可以级联保存
    // 试试不加这行能不能级联保存 , cascade = CascadeType.PERSIST

    private Date createTime;

    private Date updateTime;

    /**
     * 关联类型，关联的是说说，评论，匹配，用户信息
     */
    private String reportContentType;

    /**
     * 关联信息的版本号
     */
    private Integer infoVersionNo;

    /**
     * 支持比例，认为违规比例
     */
    private Integer supportRatio;

    /**
     * 反对比例，不违规比例
     */
    private Integer opposeRatio;


    //是否对举报结果进行了检查
    @Column(columnDefinition = "bit default false")
    private Boolean hasReview;

    /**
     * 举报时的备注信息
     */
    private String cause;

    /**
     * 被举报的内容，用户昵称，因用户昵称可修改，所以需要记录
     */
    private String reportContent;

    /**
     * 举报的状态，待审核，已通过，未通过,无效的举报（已经被封禁了，还举报）
     */
    private String status;

    /**
     * 审核的时候给的备注
     */
    private String auditNote;
    /**
     * 最终的审核类型
     */
    private String auditType;

    /**
     * 违规结果
     */
    private String violationResult;
    //举报是否有效
    private Boolean valid;

    //举报来源类型，用户举报还是系统自动审核
    private String reportSourceType;
    private Integer antispamId;

    public ReportDO() {
    }
}
