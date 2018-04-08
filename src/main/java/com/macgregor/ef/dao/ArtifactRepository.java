package com.macgregor.ef.dao;

import com.macgregor.ef.entity.ArtifactMetadata;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ArtifactRepository extends PagingAndSortingRepository<ArtifactMetadata, Integer> {
}
