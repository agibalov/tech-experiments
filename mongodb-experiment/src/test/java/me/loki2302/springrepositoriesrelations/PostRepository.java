package me.loki2302.springrepositoriesrelations;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface PostRepository extends PagingAndSortingRepository<Post, String> {
}
