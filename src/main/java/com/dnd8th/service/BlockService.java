package com.dnd8th.service;

import com.dnd8th.dao.block.BlockFindDao;
import com.dnd8th.dao.block.BlockUpdateDao;
import com.dnd8th.dao.review.ReviewFindDao;
import com.dnd8th.dto.block.BlockCreateRequest;
import com.dnd8th.dto.block.BlockGetResponse;
import com.dnd8th.dto.block.BlockMainGetResponse;
import com.dnd8th.dto.block.BlockPartDto;
import com.dnd8th.dto.block.BlockSumDto;
import com.dnd8th.dto.block.BlockUpdateRequest;
import com.dnd8th.dto.block.BlockWeekPartResponse;
import com.dnd8th.dto.task.TaskPartDto;
import com.dnd8th.dto.task.TaskSumDto;
import com.dnd8th.entity.Block;
import com.dnd8th.entity.Task;
import com.dnd8th.entity.User;
import com.dnd8th.error.exception.block.BlockAccessDeniedException;
import com.dnd8th.error.exception.block.BlockNotFoundException;
import com.dnd8th.error.exception.user.UserNotFoundException;
import com.dnd8th.repository.BlockRepository;
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
public class BlockService {

    private final BlockFindDao blockFindDao;
    private final BlockUpdateDao blockUpdateDao;
    private final ReviewFindDao reviewFindDao;
    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final DateParser dateParser;

    public void createBlock(BlockCreateRequest blockCreateRequest, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);
        Date date = dateParser.parseDate(blockCreateRequest.getDate());

        blockRepository.save(blockCreateRequest.toEntity(user, date));
    }

    public BlockGetResponse getBlock(String userEmail, Long blockId) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);
        Block block = blockRepository.findById(blockId).orElseThrow(BlockNotFoundException::new);

        User blockOwner = block.getUser();
        if (user != blockOwner) {
            throw new BlockAccessDeniedException();
        }
        String date = dateParser.toString(block.getDate());
        BlockGetResponse blockGetResponse = BlockGetResponse.builder()
                .backgroundColor(block.getBlockColor())
                .secret(block.getBlockLock())
                .emoji(block.getEmotion())
                .title(block.getTitle())
                .date(date).build();
        return blockGetResponse;
    }

    public void updateBlock(BlockUpdateRequest blockUpdateRequest, String userEmail, Long blockId) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);
        Block block = blockRepository.findById(blockId).orElseThrow(BlockNotFoundException::new);

        User blockOwner = block.getUser();
        if (user != blockOwner) {
            throw new BlockAccessDeniedException();
        }

        blockUpdateDao.updateBlock(blockId, blockUpdateRequest);
    }

    public void deleteBlock(Long blockId, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);
        Block block = blockRepository.findById(blockId).orElseThrow(BlockNotFoundException::new);

        User blockOwner = block.getUser();
        if (user != blockOwner) {
            throw new BlockAccessDeniedException();
        }
        blockRepository.delete(block);
    }

    public List<BlockWeekPartResponse> getBlockWeek(String email, String date) {
        List<BlockWeekPartResponse> blockWeekPartResponse = new ArrayList<>();
        Date targetDate = dateParser.parseDate(date);
        targetDate.setDate(targetDate.getDate() - 4);
        for (int i = 0; i < 7; i++) {
            targetDate.setDate(targetDate.getDate() + 1);
            List<String> color = blockFindDao.findByEmailAndDate(email, targetDate);
            BlockWeekPartResponse week = convertToWeekDTO(color, targetDate);
            blockWeekPartResponse.add(week);
        }
        return blockWeekPartResponse;
    }

    public BlockMainGetResponse getBlockDetail(String email, String date) {
        Date targetDate = dateParser.parseDate(date);

        List<Block> blocks = blockFindDao.getDailyBlock(email, targetDate);
        BlockSumDto blockSumDto = convertToSumBlock(blocks);
        Long reviewId = reviewFindDao.findByEmailAndDate(email, targetDate);
        return BlockMainGetResponse.builder()
                .date(dateParser.toString(targetDate))
                .numOfTotalBlocks(blockSumDto.getNumOfTotalBlocks())
                .numOfTotalTasks(blockSumDto.getNumOfTotalTasks())
                .blocks(blockSumDto.getBlocks())
                .reviewId(reviewId).build();
    }

    private BlockWeekPartResponse convertToWeekDTO(List<String> colors, Date date) {
        return new BlockWeekPartResponse(dateParser.toString(date), colors);
    }

    private BlockSumDto convertToSumBlock(List<Block> blocks) {
        Integer totalTask = 0;
        List<BlockPartDto> blocksDto = new ArrayList<>();
        for (Block block : blocks) {
            TaskSumDto task = convertToSumTask(block.getId());
            BlockPartDto blockdto = BlockPartDto.builder()
                    .blockId(block.getId())
                    .title(block.getTitle())
                    .backgroundColor(block.getBlockColor())
                    .emoji(block.getEmotion())
                    .numOfTasks(task.getNumOfTasks())
                    .numOfDoneTask(task.getNumOfDoneTask())
                    .tasks(task.getTasks()).build();
            blocksDto.add(blockdto);
            totalTask = totalTask + task.getNumOfTasks();
        }
        return new BlockSumDto(blocksDto.size(), totalTask, blocksDto);
    }

    private TaskSumDto convertToSumTask(long blockId) {
        List<TaskPartDto> tasksDto = new ArrayList<>();
        List<Task> tasks = blockFindDao.getDailyTask(blockId);
        Integer done = 0;
        for (Task task : tasks) {
            TaskPartDto taskPartDto = TaskPartDto.builder()
                    .taskId(task.getId())
                    .done(task.getStatus())
                    .task(task.getContents()).build();
            tasksDto.add(taskPartDto);
            if (task.getStatus()) {
                done = done + 1;
            }
        }
        return new TaskSumDto(tasks.size(), done, tasksDto);
    }

}

