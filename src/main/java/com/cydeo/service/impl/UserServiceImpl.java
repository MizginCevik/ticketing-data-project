package com.cydeo.service.impl;

import com.cydeo.dto.ProjectDTO;
import com.cydeo.dto.TaskDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.entity.User;
import com.cydeo.mapper.UserMapper;
import com.cydeo.repository.UserRepository;
import com.cydeo.service.ProjectService;
import com.cydeo.service.TaskService;
import com.cydeo.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ProjectService projectService;
    private final TaskService taskService;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, @Lazy ProjectService projectService, @Lazy TaskService taskService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.projectService = projectService;
        this.taskService = taskService;
    }

    @Override
    public List<UserDTO> listAllUsers() {
        List<User> userList = userRepository.findAll(Sort.by("firstName"));
        return userList.stream().map(userMapper::convertToDto).collect(Collectors.toList());
    }

    @Override
    public UserDTO findByUserName(String username) {
        User user = userRepository.findByUserName(username);
        return userMapper.convertToDto(user);
    }

    @Override
    public void save(UserDTO user) {
        userRepository.save(userMapper.convertToEntity(user));
    }

    @Override
    public void deleteByUserName(String username) {
        userRepository.deleteByUserName(username); // it's removing data from database, that's not a proper way
    }

    @Override
    public UserDTO update(UserDTO user) {
        User user1 = userRepository.findByUserName(user.getUserName()); // Find current user
        User convertedUser = userMapper.convertToEntity(user); // Map update user dto to entity object
        convertedUser.setId(user1.getId()); // set id to the converted object
        userRepository.save(convertedUser); // save the updated user in the db
        return findByUserName(user.getUserName());
    }

    @Override
    public void delete(String username) { //delete it from UserInterface, keep it in the table
        User user = userRepository.findByUserName(username); // go to db and get that user with username

        if (checkIfUserCanBeDeleted(user)) {
            user.setIsDeleted(true); // change the isDeleted field to true
            userRepository.save(user); // save the object in the db
        }

    }
    // Syntax of findAll in listAllUsers implementation is SELECT * FROM table. It's unchangeable.
    // Go to repo and write your own query to give condition that is which user to be showed or not in the UI
    // But it's not solving the problem, other queries still scan the deleted users when they are in database
    // Spring provides @Where annotation.
    // @Where(clause = "is_deleted=false") it means this condition is added automatically to all queries in repository which is using that entity
    // Which means that all methods and queries ignore the objects when this condition is true

    @Override
    public List<UserDTO> listAllByRole(String role) {
        List<User> users = userRepository.findByRoleDescriptionIgnoreCase(role); // go to db and bring all the users with specific
        return users.stream().map(userMapper::convertToDto).collect(Collectors.toList());
    }

    private boolean checkIfUserCanBeDeleted(User user) { // pass either User or UserDTO
        switch (user.getRole().getDescription()) {
            case "Manager":
                List<ProjectDTO> projectDTOList = projectService.listAllNonCompletedByAssignedManager(userMapper.convertToDto(user));
                return projectDTOList.size() == 0;
            case "Employee":
                List<TaskDTO> taskDTOList = taskService.listAllNonCompletedByAssignedEmployee(userMapper.convertToDto(user));
                return taskDTOList.size() == 0;
            default:
                return true;
        }
    }

}
