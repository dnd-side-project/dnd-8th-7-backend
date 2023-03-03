package com.dnd8th.service;

import com.dnd8th.dao.block.BlockFindDao;
import com.dnd8th.dao.keep.KeepFindDao;
import com.dnd8th.dto.keep.KeepBlockResponse;
import com.dnd8th.entity.Block;
import com.dnd8th.entity.Keep;
import com.dnd8th.entity.Task;
import com.dnd8th.entity.User;
import com.dnd8th.error.exception.block.BlockNotFoundException;
import com.dnd8th.error.exception.keep.KeepAccessDeniedException;
import com.dnd8th.error.exception.keep.KeepNotFoundException;
import com.dnd8th.error.exception.user.UserNotFoundException;
import com.dnd8th.repository.BlockRepository;
import com.dnd8th.repository.KeepRepository;
import com.dnd8th.repository.TaskRepository;
import com.dnd8th.repository.UserRepository;
import com.dnd8th.util.DateParser;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class KeepService {

    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final KeepRepository keepRepository;
    private final TaskRepository taskRepository;
    private final KeepFindDao keepFindDao;
    private final BlockFindDao blockFindDao;
    private final DateParser dateParser;

    public void createKeepBlock(Long blockId, String email) {
        Block block = blockRepository.findById(blockId).orElseThrow(BlockNotFoundException::new);
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        List<Task> tasks = blockFindDao.getDailyTask(blockId);
        Keep keep = Keep.builder()
                .block(block)
                .user(user)
                .blockColor(block.getBlockColor())
                .emotion(block.getEmotion())
                .title(block.getTitle())
                .sumOfTask(tasks.size()).build();
        keepRepository.save(keep);
    }

    public List<KeepBlockResponse> getKeepBlockList(String email) {
        List<Keep> keeps = keepFindDao.findByEmail(email);
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        List<KeepBlockResponse> keepBlocks = new ArrayList<>();
        for (Keep keep : keeps) {
            User keepOwner = keep.getUser();
            if (keepOwner != user) {
                throw new KeepAccessDeniedException();
            }
            KeepBlockResponse keepBlock = KeepBlockResponse.builder()
                    .blockId(keep.getId())
                    .backgroundColor(keep.getBlockColor())
                    .emoji(keep.getEmotion())
                    .title(keep.getTitle())
                    .numOfTasks(keep.getSumOfTask()).build();
            keepBlocks.add(keepBlock);
        }
        return keepBlocks;
    }

    public void deleteKeepBlock(Long blockId, String email) {
        Keep keep = keepRepository.findById(blockId).orElseThrow(KeepNotFoundException::new);
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        User keepOwner = keep.getUser();
        if (keepOwner != user) {
            throw new KeepAccessDeniedException();
        }
        keepRepository.delete(keep);
    }

    public void createKeepBlockOnADate(List<Long> blockIds, String email, String date) {
        Date targetDate = dateParser.parseDate(date);
        for (Long blockId : blockIds) {
            Keep keep = keepRepository.findById(blockId).orElseThrow(KeepNotFoundException::new);
            Block block = blockRepository.findById(keep.getBlock().getId())
                    .orElseThrow(BlockNotFoundException::new);
            User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
            Block newBlock = Block.builder()
                    .blockLock(false)
                    .title(block.getTitle())
                    .blockColor(block.getBlockColor())
                    .date(targetDate)
                    .emotion(block.getEmotion())
                    .user(user).build();
            Block createdBlock = blockRepository.save(newBlock);
            setTask(createdBlock, blockId);
        }
    }

    public void setTask(Block block, Long originBlockId) {
        List<Task> tasks = taskRepository.findByBlock_Id(originBlockId);
        for (Task task : tasks) {
            Task newTask = Task.builder()
                    .status(false)
                    .contents(task.getContents())
                    .block(block).build();
            taskRepository.save(newTask);
        }
    }
}
