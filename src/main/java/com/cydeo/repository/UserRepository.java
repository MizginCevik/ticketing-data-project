package com.cydeo.repository;

import com.cydeo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface UserRepository extends JpaRepository<User,Long> {

    List<User> findAllByIsDeletedOrderByFirstNameDesc(Boolean deleted); // if I pass true -> return deleted users, false -> non-deleted users
    User findByUserNameAndIsDeleted(String username,Boolean deleted);
    @Transactional // when you write a query if there is insert, delete, update transactions, spring requests this annotation to be used
    void deleteByUserName(String username);
    List<User> findByRoleDescriptionIgnoreCaseAndIsDeleted(String description,Boolean deleted);

}
