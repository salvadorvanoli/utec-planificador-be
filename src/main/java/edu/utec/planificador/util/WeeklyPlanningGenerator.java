package edu.utec.planificador.util;

import edu.utec.planificador.entity.WeeklyPlanning;
import lombok.experimental.UtilityClass;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for generating WeeklyPlanning objects for courses.
 * Aligns course dates to full weeks (Monday to Sunday).
 */
@UtilityClass
public class WeeklyPlanningGenerator {

    /**
     * Generates a list of WeeklyPlanning objects for a course.
     * Each WeeklyPlanning represents a full week from Monday to Sunday.
     * 
     * @param courseStartDate The start date of the course
     * @param courseEndDate The end date of the course
     * @return List of WeeklyPlanning objects covering the course duration
     * 
     * Example: if the course runs from 2025-03-05 (Wednesday) to 2025-03-26 (Wednesday),
     * it creates 4 WeeklyPlanning objects:
     *   Week 1: 2025-03-03 (Monday) to 2025-03-09 (Sunday)
     *   Week 2: 2025-03-10 (Monday) to 2025-03-16 (Sunday)
     *   Week 3: 2025-03-17 (Monday) to 2025-03-23 (Sunday)
     *   Week 4: 2025-03-24 (Monday) to 2025-03-30 (Sunday)
     */
    public static List<WeeklyPlanning> generateWeeklyPlannings(LocalDate courseStartDate, LocalDate courseEndDate) {
        List<WeeklyPlanning> weeklyPlannings = new ArrayList<>();
        
        // Align start date to the Monday of that week
        LocalDate alignedStartDate = courseStartDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        // Align end date to the Sunday of that week
        LocalDate alignedEndDate = courseEndDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        
        // Generate weekly planning objects
        LocalDate currentWeekStart = alignedStartDate;
        int weekNumber = 1;
        
        while (!currentWeekStart.isAfter(alignedEndDate)) {
            LocalDate currentWeekEnd = currentWeekStart.plusDays(6); // Monday + 6 days = Sunday
            
            // Create WeeklyPlanning for this week
            WeeklyPlanning weeklyPlanning = new WeeklyPlanning(weekNumber, currentWeekStart, currentWeekEnd);
            weeklyPlannings.add(weeklyPlanning);
            
            // Move to next week
            currentWeekStart = currentWeekStart.plusWeeks(1);
            weekNumber++;
        }
        
        return weeklyPlannings;
    }
    
    /**
     * Calculates the Monday of the week containing the given date.
     * 
     * @param date Any date
     * @return The Monday of the week containing that date
     */
    public static LocalDate getMondayOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
    
    /**
     * Calculates the Sunday of the week containing the given date.
     * 
     * @param date Any date
     * @return The Sunday of the week containing that date
     */
    public static LocalDate getSundayOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }
}
