package com.example.healthgenie.repository;

import com.example.healthgenie.entity.CommunityPost;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost,Long> {
//     List<CommunityPost> findAllOrderByDateDesc(Pageable pageable);
}
