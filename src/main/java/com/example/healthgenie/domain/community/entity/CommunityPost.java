package com.example.healthgenie.domain.community.entity;

import com.example.healthgenie.domain.user.entity.User;
import com.example.healthgenie.global.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@AllArgsConstructor
@Table(name = "COMMUNITY_POST_TB")
@Builder(toBuilder = true)
public class CommunityPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="community_post_id")
    private Long id;

    @NotNull
    @Column(name = "title")
    private String title;

    @NotNull
    @Column(name ="post_content")
    private String content;

    @Builder.Default
    @OneToMany(mappedBy = "post",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<CommunityComment> commentList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<CommunityPostPhoto> communityPostPhotos = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User member;

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}