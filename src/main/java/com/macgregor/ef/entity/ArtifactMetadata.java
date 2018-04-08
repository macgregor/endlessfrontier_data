package com.macgregor.ef.entity;


import javax.persistence.*;

@Entity
@Table(name = "artifact")
public class ArtifactMetadata {

    @Id
    @Column(name="id", nullable = false)
    private Integer id;

    @Column(name="version", nullable = false)
    private String version;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "desc", nullable = false)
    private String desc;

    @Column(name="rank", nullable = false)
    private Integer rank;

    @Lob
    @Column(name = "raw_data", nullable = false)
    public String rawData;

    @Lob
    @Column(name = "processed_data", nullable = false)
    public String processedData;
}
