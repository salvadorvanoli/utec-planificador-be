package edu.utec.planificador.mapper;

import edu.utec.planificador.dto.aiagent.AIReportRequest;
import edu.utec.planificador.entity.Activity;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.WeeklyPlanning;
import edu.utec.planificador.enumeration.DeliveryFormat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CourseStatisticsMapper {

    public AIReportRequest.CourseStatisticsDto calculateStatistics(Course course) {
        if (course == null || course.getWeeklyPlannings() == null) {
            return AIReportRequest.CourseStatisticsDto.builder().build();
        }

        List<Activity> allActivities = getAllActivities(course);

        return AIReportRequest.CourseStatisticsDto.builder()
                .cognitiveProcesses(calculateCognitiveProcesses(allActivities))
                .transversalCompetencies(calculateTransversalCompetencies(allActivities))
                .learningModalities(calculateLearningModalities(allActivities))
                .teachingStrategies(calculateTeachingStrategies(allActivities))
                .mostUsedResources(calculateMostUsedResources(allActivities))
                .linkedSDGs(calculateLinkedSDGs(course))
                .averageActivityDurationInMinutes(calculateAverageActivityDuration(allActivities))
                .totalWeeks(course.getWeeklyPlannings().size())
                .totalInPersonHours(course.getHoursPerDeliveryFormat().getOrDefault(DeliveryFormat.IN_PERSON, 0))
                .totalVirtualHours(course.getHoursPerDeliveryFormat().getOrDefault(DeliveryFormat.VIRTUAL, 0))
                .totalHybridHours(course.getHoursPerDeliveryFormat().getOrDefault(DeliveryFormat.HYBRID, 0))
                .build();
    }

    private List<Activity> getAllActivities(Course course) {
        List<Activity> activities = new ArrayList<>();

        for (WeeklyPlanning wp : course.getWeeklyPlannings()) {
            // Activities in programmatic contents
            if (wp.getProgrammaticContents() != null) {
                wp.getProgrammaticContents().forEach(pc -> {
                    if (pc.getActivities() != null) {
                        activities.addAll(pc.getActivities());
                    }
                });
            }
        }

        return activities;
    }

    private Map<String, Integer> calculateCognitiveProcesses(List<Activity> activities) {
        Map<String, Integer> counts = new HashMap<>();

        activities.forEach(activity -> {
            if (activity.getCognitiveProcesses() != null) {
                activity.getCognitiveProcesses().forEach(cp -> {
                    String key = cp.getDisplayValue();
                    counts.put(key, counts.getOrDefault(key, 0) + 1);
                });
            }
        });

        return calculatePercentages(counts);
    }

    private Map<String, Integer> calculateTransversalCompetencies(List<Activity> activities) {
        Map<String, Integer> counts = new HashMap<>();

        activities.forEach(activity -> {
            if (activity.getTransversalCompetencies() != null) {
                activity.getTransversalCompetencies().forEach(tc -> {
                    String key = tc.getDisplayValue();
                    counts.put(key, counts.getOrDefault(key, 0) + 1);
                });
            }
        });

        return calculatePercentages(counts);
    }

    private Map<String, Integer> calculateLearningModalities(List<Activity> activities) {
        Map<String, Integer> counts = new HashMap<>();

        activities.forEach(activity -> {
            if (activity.getLearningModality() != null) {
                String key = activity.getLearningModality().getDisplayValue();
                counts.put(key, counts.getOrDefault(key, 0) + 1);
            }
        });

        return calculatePercentages(counts);
    }

    private Map<String, Integer> calculateTeachingStrategies(List<Activity> activities) {
        Map<String, Integer> counts = new HashMap<>();

        activities.forEach(activity -> {
            if (activity.getTeachingStrategies() != null) {
                activity.getTeachingStrategies().forEach(ts -> {
                    String key = ts.getDisplayValue();
                    counts.put(key, counts.getOrDefault(key, 0) + 1);
                });
            }
        });

        return calculatePercentages(counts);
    }

    private List<String> calculateMostUsedResources(List<Activity> activities) {
        Map<String, Integer> counts = new HashMap<>();

        activities.forEach(activity -> {
            if (activity.getLearningResources() != null) {
                activity.getLearningResources().forEach(lr -> {
                    String key = lr.getDisplayValue();
                    counts.put(key, counts.getOrDefault(key, 0) + 1);
                });
            }
        });

        // Return top 5 most used resources
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Map<String, Integer> calculateLinkedSDGs(Course course) {
        if (course.getSustainableDevelopmentGoals() == null || course.getSustainableDevelopmentGoals().isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Integer> sdgs = new HashMap<>();
        int totalSDGs = course.getSustainableDevelopmentGoals().size();
        int percentagePerSDG = 100 / totalSDGs;

        course.getSustainableDevelopmentGoals().forEach(sdg -> {
            sdgs.put(sdg.getDisplayValue(), percentagePerSDG);
        });

        return sdgs;
    }

    private Integer calculateAverageActivityDuration(List<Activity> activities) {
        if (activities.isEmpty()) {
            return 0;
        }

        int totalDuration = activities.stream()
                .filter(a -> a.getDurationInMinutes() != null)
                .mapToInt(Activity::getDurationInMinutes)
                .sum();

        long count = activities.stream()
                .filter(a -> a.getDurationInMinutes() != null)
                .count();

        return count > 0 ? (int) (totalDuration / count) : 0;
    }

    private Map<String, Integer> calculatePercentages(Map<String, Integer> counts) {
        if (counts.isEmpty()) {
            return counts;
        }

        int total = counts.values().stream().mapToInt(Integer::intValue).sum();

        if (total == 0) {
            return counts;
        }

        Map<String, Integer> percentages = new HashMap<>();
        counts.forEach((key, value) -> {
            int percentage = (int) Math.round((value * 100.0) / total);
            percentages.put(key, percentage);
        });

        return percentages;
    }
}

