package com.dnd8th.service;


import com.dnd8th.dao.task.TaskUpdateDao;
import com.dnd8th.dto.task.TaskCreateRequest;
import com.dnd8th.entity.Block;
import com.dnd8th.entity.Task;
import com.dnd8th.entity.User;
import com.dnd8th.error.exception.block.BlockNotFoundException;
import com.dnd8th.error.exception.task.TaskAccessDeniedException;
import com.dnd8th.error.exception.task.TaskNotFoundException;
import com.dnd8th.error.exception.user.UserNotFoundException;
import com.dnd8th.repository.BlockRepository;
import com.dnd8th.repository.TaskRepository;
import com.dnd8th.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final BlockRepository blockRepository;
    private final TaskUpdateDao taskUpdateDao;
    private final UserRepository userRepository;

    public Task createTask(TaskCreateRequest taskCreateRequest, Long blockId) {
        Block block = blockRepository.findById(blockId).orElseThrow(BlockNotFoundException::new);

        return taskRepository.save(taskCreateRequest.toEntity(block));
    }

    public void deleteTask(String email, Long taskId) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);

        User taskOwner = task.getBlock().getUser();
        if (taskOwner != user) {
            throw new TaskAccessDeniedException();
        }
        taskRepository.delete(task);
    }

    public void updateTask(String email, Long taskId, String content) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);

        User taskOwner = task.getBlock().getUser();
        if (taskOwner != user) {
            throw new TaskAccessDeniedException();
        }

        taskUpdateDao.updateContent(taskId, content);
    }

    public void toggleStatus(String email, Long taskId) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);

        User taskOwner = task.getBlock().getUser();
        if (taskOwner != user) {
            throw new TaskAccessDeniedException();
        }
        taskUpdateDao.updateStatus(taskId);
    }
}
