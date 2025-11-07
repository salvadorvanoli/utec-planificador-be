package edu.utec.planificador.util;

import edu.utec.planificador.entity.WeeklyPlanning;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeeklyPlanningGeneratorTest {

    @Test
    void testGenerateWeeklyPlannings_CourseStartingAndEndingMidweek() {
        // Given: Course runs from Wednesday 2025-03-05 to Wednesday 2025-03-26
        LocalDate courseStartDate = LocalDate.of(2025, 3, 5); // Wednesday
        LocalDate courseEndDate = LocalDate.of(2025, 3, 26);  // Wednesday

        // When: Generate weekly plannings
        List<WeeklyPlanning> weeklyPlannings = WeeklyPlanningGenerator.generateWeeklyPlannings(courseStartDate, courseEndDate);

        // Then: Should create 4 weeks
        assertEquals(4, weeklyPlannings.size());

        // Week 1: Monday 2025-03-03 to Sunday 2025-03-09
        WeeklyPlanning week1 = weeklyPlannings.get(0);
        assertEquals(1, week1.getWeekNumber());
        assertEquals(LocalDate.of(2025, 3, 3), week1.getStartDate());
        assertEquals(LocalDate.of(2025, 3, 9), week1.getEndDate());
        assertEquals(DayOfWeek.MONDAY, week1.getStartDate().getDayOfWeek());
        assertEquals(DayOfWeek.SUNDAY, week1.getEndDate().getDayOfWeek());

        // Week 2: Monday 2025-03-10 to Sunday 2025-03-16
        WeeklyPlanning week2 = weeklyPlannings.get(1);
        assertEquals(2, week2.getWeekNumber());
        assertEquals(LocalDate.of(2025, 3, 10), week2.getStartDate());
        assertEquals(LocalDate.of(2025, 3, 16), week2.getEndDate());
        assertEquals(DayOfWeek.MONDAY, week2.getStartDate().getDayOfWeek());
        assertEquals(DayOfWeek.SUNDAY, week2.getEndDate().getDayOfWeek());

        // Week 3: Monday 2025-03-17 to Sunday 2025-03-23
        WeeklyPlanning week3 = weeklyPlannings.get(2);
        assertEquals(3, week3.getWeekNumber());
        assertEquals(LocalDate.of(2025, 3, 17), week3.getStartDate());
        assertEquals(LocalDate.of(2025, 3, 23), week3.getEndDate());
        assertEquals(DayOfWeek.MONDAY, week3.getStartDate().getDayOfWeek());
        assertEquals(DayOfWeek.SUNDAY, week3.getEndDate().getDayOfWeek());

        // Week 4: Monday 2025-03-24 to Sunday 2025-03-30
        WeeklyPlanning week4 = weeklyPlannings.get(3);
        assertEquals(4, week4.getWeekNumber());
        assertEquals(LocalDate.of(2025, 3, 24), week4.getStartDate());
        assertEquals(LocalDate.of(2025, 3, 30), week4.getEndDate());
        assertEquals(DayOfWeek.MONDAY, week4.getStartDate().getDayOfWeek());
        assertEquals(DayOfWeek.SUNDAY, week4.getEndDate().getDayOfWeek());
    }

    @Test
    void testGenerateWeeklyPlannings_CourseStartingOnMonday() {
        // Given: Course starts on Monday and ends on Friday
        LocalDate courseStartDate = LocalDate.of(2025, 3, 3);  // Monday
        LocalDate courseEndDate = LocalDate.of(2025, 3, 14);   // Friday

        // When: Generate weekly plannings
        List<WeeklyPlanning> weeklyPlannings = WeeklyPlanningGenerator.generateWeeklyPlannings(courseStartDate, courseEndDate);

        // Then: Should create 2 weeks
        assertEquals(2, weeklyPlannings.size());

        // Week 1: Monday 2025-03-03 to Sunday 2025-03-09
        WeeklyPlanning week1 = weeklyPlannings.get(0);
        assertEquals(1, week1.getWeekNumber());
        assertEquals(LocalDate.of(2025, 3, 3), week1.getStartDate());
        assertEquals(LocalDate.of(2025, 3, 9), week1.getEndDate());

        // Week 2: Monday 2025-03-10 to Sunday 2025-03-16
        WeeklyPlanning week2 = weeklyPlannings.get(1);
        assertEquals(2, week2.getWeekNumber());
        assertEquals(LocalDate.of(2025, 3, 10), week2.getStartDate());
        assertEquals(LocalDate.of(2025, 3, 16), week2.getEndDate());
    }

    @Test
    void testGenerateWeeklyPlannings_SingleWeek() {
        // Given: Course within a single week
        LocalDate courseStartDate = LocalDate.of(2025, 3, 5);  // Wednesday
        LocalDate courseEndDate = LocalDate.of(2025, 3, 7);    // Friday

        // When: Generate weekly plannings
        List<WeeklyPlanning> weeklyPlannings = WeeklyPlanningGenerator.generateWeeklyPlannings(courseStartDate, courseEndDate);

        // Then: Should create 1 week
        assertEquals(1, weeklyPlannings.size());

        // Week 1: Monday 2025-03-03 to Sunday 2025-03-09
        WeeklyPlanning week1 = weeklyPlannings.get(0);
        assertEquals(1, week1.getWeekNumber());
        assertEquals(LocalDate.of(2025, 3, 3), week1.getStartDate());
        assertEquals(LocalDate.of(2025, 3, 9), week1.getEndDate());
    }

    @Test
    void testGenerateWeeklyPlannings_ExactWeek() {
        // Given: Course exactly one week (Monday to Sunday)
        LocalDate courseStartDate = LocalDate.of(2025, 3, 3);  // Monday
        LocalDate courseEndDate = LocalDate.of(2025, 3, 9);    // Sunday

        // When: Generate weekly plannings
        List<WeeklyPlanning> weeklyPlannings = WeeklyPlanningGenerator.generateWeeklyPlannings(courseStartDate, courseEndDate);

        // Then: Should create 1 week
        assertEquals(1, weeklyPlannings.size());

        WeeklyPlanning week1 = weeklyPlannings.get(0);
        assertEquals(1, week1.getWeekNumber());
        assertEquals(LocalDate.of(2025, 3, 3), week1.getStartDate());
        assertEquals(LocalDate.of(2025, 3, 9), week1.getEndDate());
    }

    @Test
    void testGetMondayOfWeek() {
        // Test various days of the week
        assertEquals(LocalDate.of(2025, 3, 3), WeeklyPlanningGenerator.getMondayOfWeek(LocalDate.of(2025, 3, 3))); // Monday
        assertEquals(LocalDate.of(2025, 3, 3), WeeklyPlanningGenerator.getMondayOfWeek(LocalDate.of(2025, 3, 5))); // Wednesday
        assertEquals(LocalDate.of(2025, 3, 3), WeeklyPlanningGenerator.getMondayOfWeek(LocalDate.of(2025, 3, 9))); // Sunday
    }

    @Test
    void testGetSundayOfWeek() {
        // Test various days of the week
        assertEquals(LocalDate.of(2025, 3, 9), WeeklyPlanningGenerator.getSundayOfWeek(LocalDate.of(2025, 3, 3))); // Monday
        assertEquals(LocalDate.of(2025, 3, 9), WeeklyPlanningGenerator.getSundayOfWeek(LocalDate.of(2025, 3, 5))); // Wednesday
        assertEquals(LocalDate.of(2025, 3, 9), WeeklyPlanningGenerator.getSundayOfWeek(LocalDate.of(2025, 3, 9))); // Sunday
    }

    @Test
    void testGenerateWeeklyPlannings_LongCourse() {
        // Given: 3-month course
        LocalDate courseStartDate = LocalDate.of(2025, 3, 10);  // Monday
        LocalDate courseEndDate = LocalDate.of(2025, 6, 6);     // Friday

        // When: Generate weekly plannings
        List<WeeklyPlanning> weeklyPlannings = WeeklyPlanningGenerator.generateWeeklyPlannings(courseStartDate, courseEndDate);

        // Then: Should create appropriate number of weeks
        assertTrue(weeklyPlannings.size() >= 12); // At least 12 weeks

        // Verify first week
        WeeklyPlanning firstWeek = weeklyPlannings.get(0);
        assertEquals(1, firstWeek.getWeekNumber());
        assertEquals(DayOfWeek.MONDAY, firstWeek.getStartDate().getDayOfWeek());
        assertEquals(DayOfWeek.SUNDAY, firstWeek.getEndDate().getDayOfWeek());

        // Verify last week
        WeeklyPlanning lastWeek = weeklyPlannings.get(weeklyPlannings.size() - 1);
        assertEquals(weeklyPlannings.size(), lastWeek.getWeekNumber());
        assertEquals(DayOfWeek.MONDAY, lastWeek.getStartDate().getDayOfWeek());
        assertEquals(DayOfWeek.SUNDAY, lastWeek.getEndDate().getDayOfWeek());

        // Verify consecutive weeks
        for (int i = 1; i < weeklyPlannings.size(); i++) {
            WeeklyPlanning previousWeek = weeklyPlannings.get(i - 1);
            WeeklyPlanning currentWeek = weeklyPlannings.get(i);
            
            // Current week should start the day after previous week ends
            assertEquals(previousWeek.getEndDate().plusDays(1), currentWeek.getStartDate());
        }
    }
}
