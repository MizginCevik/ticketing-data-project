package com.cydeo.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // whenever something is deleted, this field needs to be true
    private Boolean isDeleted = false; // it's going to effect all object so that it is created in BaseEntity
    @Column(nullable = false,updatable = false) // when update the object this field becomes null, to solve this issue added annotation
    private LocalDateTime insertDateTime;
    @Column(nullable = false,updatable = false) // it can not be null and updatable, keep the old information
    private Long insertUserId;
    @Column(nullable = false)
    private LocalDateTime lastUpdateDateTime;
    @Column(nullable = false)
    private Long lastUpdateUserId;

    @PrePersist
    private void onPrePersist(){ // method name can be anything
        this.insertDateTime = LocalDateTime.now();
        this.lastUpdateDateTime = LocalDateTime.now();
        this.insertUserId = 1L; // info about who logs in the system, this will be connected in security part
        this.lastUpdateUserId = 1L;
    }
    // this method is created to initialize (set) the fields like constructor
    // onPrePersist() executes when create a new user -> save
    // onPreUpdate() executes when update the user -> update
    // purpose of these two methods is to assign the fields
    // Spring will know by annotations

    @PreUpdate
    private void onPreUpdate(){
        this.lastUpdateDateTime = LocalDateTime.now();
        this.lastUpdateUserId = 1L;
    }

}
