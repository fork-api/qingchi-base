package com.qingchi.base.repository.report;

import com.qingchi.base.model.report.ReportDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author qinkaiyuan
 * @date 2019-09-27 23:21
 */
public interface ReportRepository extends JpaRepository<ReportDO, Integer> {
    Optional<ReportDO> findByReceiveUserIdAndInfoVersionNo(Integer receiveUserId, Integer infoVersionNo);

    Optional<ReportDO> findFirstOneByTalkId(Integer talkId);

    Optional<ReportDO> findFirstOneByCommentId(Integer commentId);

    Optional<ReportDO> findFirstOneByUserImgId(Integer userImgId);

    Optional<ReportDO> findFirstOneByMessageId(Long messageId);

//    List<ReportDO> findTop5ByStatusAndTalkStatusInOrStatusAndUserImgStatusInOrStatusAndCommentStatusInOrStatusAndMessageStatusInOrderByCreateTimeAsc(String status, List<String> talkStatus, String status1, List<String> userImgStatus, String status2, List<String> commentStatus, String status3, List<String> messageStatus);

    List<ReportDO> findTop10ByStatusInOrderByCreateTimeAsc(List<String> status);

//    List<ReportDO> findTop20ByStatusAndTalkStatusInOrStatusAndUserImgStatusInOrStatusAndCommentStatusInOrStatusAndMessageStatusInOrderByCreateTimeAsc(String status, List<String> talkStatus, String status1, List<String> userImgStatus, String status2, List<String> commentStatus, String status3, List<String> messageStatus);

//    List<ReportDO> findTop30ByStatusAndTalkStatusInOrStatusAndUserImgStatusInOrStatusAndCommentStatusInOrStatusAndMessageStatusInOrderByCreateTimeAsc(String status, List<String> talkStatus, String status1, List<String> userImgStatus, String status2, List<String> commentStatus, String status3, List<String> messageStatus);

    List<ReportDO> findTop10ByReceiveUserIdAndStatusOrderByUpdateTimeDesc(Integer userId, String status);

//    Page<ReportDO> findByStatusAndAuditTypeAndReceiveUserStatusNotOrderByIdDesc(Pageable pageable, String status, String auditType, String userStatus);

    Page<ReportDO> findByStatusAndAuditTypeAndHasReviewFalseOrderByIdDesc(Pageable pageable, String status, String auditType);

    Page<ReportDO> findByStatusOrderByIdDesc(Pageable pageable, String status);

}
