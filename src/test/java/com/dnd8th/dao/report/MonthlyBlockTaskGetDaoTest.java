package com.dnd8th.dao.report;

import static org.assertj.core.api.Assertions.assertThat;

import com.dnd8th.common.ReportTest;
import com.dnd8th.dto.report.MonthlyBlockAndTaskGetDTO;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MonthlyBlockTaskGetDaoTest extends ReportTest {

    @Test
    @DisplayName("월별 블록을 정상적으로 조회할 수 있다.")
    void getMonthlyBlock() {
        //given
        String userEmail = "test@gmail.com";
        Integer year = 2023;
        Integer month = 3;

        //when
        List<MonthlyBlockAndTaskGetDTO> monthlyBlock = monthlyBlockTaskGetDao.getMonthlyBlockAndTask(
                userEmail,
                year,
                month);

        //then
        assertThat(monthlyBlock.size()).isEqualTo(8);
    }

    @Test
    @DisplayName("월별 블록이 없는 경우, 빈 리스트를 반환한다.")
    void getMonthlyBlockWhenNoBlock() {
        //given
        String userEmail = "test@gmail.com";
        Integer month = 2;
        Integer year = 2023;

        //when
        List<MonthlyBlockAndTaskGetDTO> monthlyBlock = monthlyBlockTaskGetDao.getMonthlyBlockAndTask(
                userEmail,
                year,
                month);

        //then
        assertThat(monthlyBlock.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("특정 년월의 특정 일까지의 블록을 정상적으로 조회할 수 있다.")
    void getMonthlyBlockUntilDate() {
        //given
        String userEmail = "test@gmail.com";
        Integer month = 3;
        Integer year = 2023;
        Integer day = 7;

        //when
        List<MonthlyBlockAndTaskGetDTO> monthlyBlock = monthlyBlockTaskGetDao.getMonthlyBlockAndTaskWithDate(
                userEmail,
                year,
                month,
                day);

        //then
        assertThat(monthlyBlock.size()).isEqualTo(7);
    }

    @Test
    @DisplayName("특정 년월의 특정 일까지의 블록이 없는 경우, 빈 리스트를 반환한다.")
    void getMonthlyBlockUntilDateWhenNoBlock() {
        //given
        String userEmail = "test@gmail.com";
        Integer month = 4;
        Integer year = 2023;
        Integer day = 7;

        //when
        List<MonthlyBlockAndTaskGetDTO> monthlyBlock = monthlyBlockTaskGetDao.getMonthlyBlockAndTaskWithDate(
                userEmail,
                year,
                month,
                day);

        //then
        assertThat(monthlyBlock.size()).isEqualTo(0);
    }
}
