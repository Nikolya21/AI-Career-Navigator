// com.aicareer.module.course.AssemblePlan
package com.aicareer.repository.course;

import com.aicareer.core.dto.courseDto.CourseRequest;
import com.aicareer.core.dto.courseDto.ResponseByWeek;

public interface AssemblePlan {
  ResponseByWeek assemblePlan(CourseRequest request);
}