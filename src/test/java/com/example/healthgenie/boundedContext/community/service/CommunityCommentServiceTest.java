package com.example.healthgenie.boundedContext.community.service;

import com.example.healthgenie.base.exception.CommonException;
import com.example.healthgenie.base.exception.CommunityCommentException;
import com.example.healthgenie.base.exception.CommunityPostException;
import com.example.healthgenie.boundedContext.community.dto.CommentRequest;
import com.example.healthgenie.boundedContext.community.dto.CommentResponse;
import com.example.healthgenie.boundedContext.community.entity.CommunityComment;
import com.example.healthgenie.boundedContext.community.entity.CommunityPost;
import com.example.healthgenie.boundedContext.user.entity.Role;
import com.example.healthgenie.boundedContext.user.entity.User;
import com.example.healthgenie.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CommunityCommentServiceTest {

    @Autowired
    TestUtils testUtils;

    @Autowired
    CommunityCommentService communityCommentService;

    CommunityPost post1;
    User user1;
    CommunityComment comment1;

    @BeforeEach
    void before() {
        user1 = testUtils.createUser("test1", Role.EMPTY, "test1@test.com");
        post1 = testUtils.createPost(user1, "테스트 게시글 제목1", "테스트 게시글 내용1");
        comment1 = testUtils.createComment("기본 댓글", post1);
    }

    @Test
    @DisplayName("정상적으로 댓글 달기")
    void save() {
        // given
        testUtils.login(user1);

        CommentRequest request = testUtils.createCommentRequest("테스트 댓글1", user1.getName());

        // when
        CommentResponse savedComment = communityCommentService.save(post1.getId(), request);

        // then
        assertThat(savedComment.getContent()).isEqualTo("테스트 댓글1");
        assertThat(savedComment.getWriter()).isEqualTo("test1");
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 댓글 달기")
    void notExistPostComment() {
        // given
        testUtils.login(user1);

        CommentRequest request = testUtils.createCommentRequest("테스트 댓글1", user1.getName());

        // when

        // then
        assertThatThrownBy(() -> communityCommentService.save(999L, request))
                .isInstanceOf(CommunityPostException.class);
    }

    @Test
    @DisplayName("로그인하지 않은 사용자가 게시글에 댓글 달기")
    void notLoginComment() {
        // given
        CommentRequest request = testUtils.createCommentRequest("테스트 댓글1", user1.getName());

        // when

        // then
        assertThatThrownBy(() -> communityCommentService.save(post1.getId(), request))
                .isInstanceOf(CommonException.class);
    }

    @Test
    @DisplayName("댓글 한개 조회하기 - 개발자용")
    void findById() {
        // given
        testUtils.login(user1);

        CommentRequest request = testUtils.createCommentRequest("테스트 댓글1", user1.getName());
        CommentResponse savedComment = communityCommentService.save(post1.getId(), request);

        // when
        CommentResponse findComment = communityCommentService.findById(savedComment.getId());

        // then
        assertThat(findComment.getWriter()).isEqualTo(user1.getName());
        assertThat(findComment.getContent()).isEqualTo("테스트 댓글1");
    }

    @Test
    @DisplayName("존재하지 않는 댓글 한개 조회하기 - 개발자용")
    void notExistFindById() {
        // given

        // when

        // then
        assertThatThrownBy(() -> communityCommentService.findById(999L))
                .isInstanceOf(CommunityCommentException.class);
    }

    @Test
    @DisplayName("게시글 내 모든 댓글 조회하기")
    void allComments() {
        // given
        testUtils.login(user1);

        for(int i=1; i<=10; i++) {
            CommentRequest request = testUtils.createCommentRequest("테스트 댓글" + i, user1.getName());
            communityCommentService.save(post1.getId(), request);
        }

        // when
        List<CommentResponse> response = communityCommentService.findAllByPostId(post1.getId());

        // then
        assertThat(response.size()).isEqualTo(11);
        assertThat(response).isSortedAccordingTo(Comparator.comparingLong(CommentResponse::getId).reversed());
    }

    @Test
    @DisplayName("정상적으로 댓글 수정하기")
    void editComment() {
        // given
        testUtils.login(user1);

        CommentRequest request1 = testUtils.createCommentRequest("테스트 댓글1", user1.getName());
        CommentResponse savedComment = communityCommentService.save(post1.getId(), request1);

        CommentRequest request2 = testUtils.createCommentRequest("테스트 댓글2", user1.getName());
        communityCommentService.save(post1.getId(), request2);

        CommentRequest updateRequest = testUtils.createCommentRequest("수정된 댓글");

        // when
        CommentResponse updatedComment = communityCommentService.update(post1.getId(), savedComment.getId(), updateRequest);
        List<CommentResponse> allComments = communityCommentService.findAllByPostId(post1.getId());

        // then
        assertThat(updatedComment.getContent()).isEqualTo("수정된 댓글");
        assertThat(allComments).extracting(CommentResponse::getContent).contains("수정된 댓글");
    }

    @Test
    @DisplayName("존재하지 않는 게시글의 댓글 수정하기")
    void notExistPostEditComment() {
        // given
        testUtils.login(user1);

        CommentRequest updateRequest = testUtils.createCommentRequest("수정된 댓글");

        // when

        // then
        assertThatThrownBy(() -> communityCommentService.update(999L, 999L, updateRequest))
                .isInstanceOf(CommunityPostException.class);
    }

    @Test
    @DisplayName("존재하지 않는 댓글 수정하기")
    void notExistCommentEdit() {
        // given
        testUtils.login(user1);

        CommentRequest updateRequest = testUtils.createCommentRequest("수정된 댓글");

        // when

        // then
        assertThatThrownBy(() -> communityCommentService.update(post1.getId(), 999L, updateRequest))
                .isInstanceOf(CommunityCommentException.class);
    }

    @Test
    @DisplayName("로그인하지 않은 사용자가 댓글 수정하기")
    void notLoginEditComment() {
        // given
        CommentRequest updateRequest = testUtils.createCommentRequest("수정된 댓글");

        // when

        // then
        assertThatThrownBy(() -> communityCommentService.update(post1.getId(), comment1.getId(), updateRequest))
                .isInstanceOf(CommonException.class);
    }

    @Test
    @DisplayName("자신이 작성하지 않은 댓글 수정하기")
    void noPermissionToEdit() {
        // given
        testUtils.login(user1);

        CommentRequest updateRequest = testUtils.createCommentRequest("수정된 댓글");

        // when

        // then
        assertThatThrownBy(() -> communityCommentService.update(post1.getId(), comment1.getId(), updateRequest))
                .isInstanceOf(CommunityCommentException.class);
    }

    @Test
    @DisplayName("정상적으로 댓글 삭제하기")
    void removeComment() {
        // given
        testUtils.login(user1);

        CommentRequest request = testUtils.createCommentRequest("테스트 댓글1");
        CommentResponse savedComment = communityCommentService.save(post1.getId(), request);

        // when
        communityCommentService.deleteById(post1.getId(), savedComment.getId());

        // then
        List<CommentResponse> allComments = communityCommentService.findAllByPostId(post1.getId());
        assertThat(allComments.size()).isEqualTo(1);
        assertThat(allComments).extracting(CommentResponse::getContent).doesNotContain("테스트 댓글1");
    }

    @Test
    @DisplayName("존재하지 않는 게시글의 댓글 삭제하기")
    void notExistPostRemoveComment() {
        // given
        testUtils.login(user1);

        // when

        // then
        assertThatThrownBy(() -> communityCommentService.deleteById(999L, 999L))
                .isInstanceOf(CommunityPostException.class);
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제하기")
    void notExistCommentRemove() {
        // given
        testUtils.login(user1);

        // when

        // then
        assertThatThrownBy(() -> communityCommentService.deleteById(post1.getId(), 999L))
                .isInstanceOf(CommunityCommentException.class);
    }

    @Test
    @DisplayName("로그인하지 않은 사용자가 댓글 삭제하기")
    void notLoginRemoveComment() {
        // given

        // when

        // then
        assertThatThrownBy(() -> communityCommentService.deleteById(post1.getId(), comment1.getId()))
                .isInstanceOf(CommonException.class);
    }

    @Test
    @DisplayName("자신이 작성하지 않은 댓글 삭제하기")
    void noPermissionToRemove() {
        // given
        testUtils.login(user1);

        // when

        // then
        assertThatThrownBy(() -> communityCommentService.deleteById(post1.getId(), comment1.getId()))
                .isInstanceOf(CommunityCommentException.class);
    }
}